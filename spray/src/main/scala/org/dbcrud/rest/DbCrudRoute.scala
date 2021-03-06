package org.dbcrud.rest

import java.util.logging.{Level, Logger}

import com.typesafe.config.{ConfigFactory, Config}
import org.dbcrud._
import org.json4s.JsonAST.{JField, JObject}
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}
import spray.http.StatusCodes
import spray.httpx.Json4sSupport

import scala.util.{Success, Failure}

object DbCrudRoute{

  implicit def json4sFormats = DefaultFormats

  class RowSerializer extends CustomSerializer[Row](format=>(
    {
      case JObject(fields) => new Row {
        val fieldToValue = fields.map(t=>Symbol(t._1) -> t._2.values.asInstanceOf[Any]).toMap
        override def apply[T](column: Symbol): T = fieldToValue(column).asInstanceOf[T]
        override def apply[T](column: String): T = fieldToValue(Symbol(column)).asInstanceOf[T]
        override def foreach[U](f: ((Symbol, Any)) => U): Unit = fieldToValue.foreach[U](f)
      }
    },
    {
      case r:Row =>
        JObject(r.map(t=>JField(t._1.name, Extraction.decompose(t._2))).toList)
    }
    ))

}

import org.dbcrud.ColumnOps._
import org.dbcrud.rest.DbCrudRoute._
/**
 * Created by julio on 9/01/15.
 */
class DbCrudRoute(dbCrud:DataCrud, config:Config = ConfigFactory.load()) extends spray.routing.Directives with Json4sSupport {
  private val logger = Logger.getLogger(classOf[DbCrudRoute].getName)
  private val settings = new Settings(config)
  private val aliasToTable = settings.restAliases
  private val tableToAlias = aliasToTable.map(_.swap)

  implicit def json4sFormats = DefaultFormats + new RowSerializer

  def isValidEntity(entity:Symbol):Boolean = dbCrud.tableNames.exists(_ == entity)

  private def parsePredicate(entity:Symbol, params:Map[String,String]):Either[Seq[Failure[_]],Predicate]={
    val tableDef = dbCrud.tableDef(entity)
    val (failures, successes) = params.foldLeft((Nil:List[Failure[_]],Nil:List[(Symbol,Any)])){case (acc, (field, value)) =>
      tableDef.coerce[Any](Symbol(field), value) match{
        case Some(Success(coerced)) => acc._1 -> ((Symbol(field)->coerced)::acc._2)
        case Some(f:Failure[_]) => (f::acc._1) -> acc._2
        case _ => acc
      }
    }

    if(!failures.isEmpty) Left(failures)
    else if(!successes.isEmpty) Right(successes)
    else Right(EmptyPredicate)
  }

  def routes = pathPrefix(settings.restPrefix) {
    path("resources"){
      get{
        complete(dbCrud.tableNames.map(t=>tableToAlias.getOrElse(t.name, t.name)))
      }
    } ~ pathPrefix(Segment) {entity =>
      handleResourceRequest(entity)
    }
  }

  private def handleResourceRequest(entity: String) = {
    val tableName = Symbol(aliasToTable.getOrElse(entity, entity))
    if (!isValidEntity(tableName))
      complete(StatusCodes.NotFound, s"resource '$entity' not found")
    else {
      pathEnd {
        post {
          createResource(tableName)
        } ~
          get {
            queryResources(tableName)
          }
      } ~
        pathPrefix(Segment) { id =>
          handleResourceAction(tableName, id)
        }
    }
  }

  private def handleResourceAction(tableName:Symbol, id:String) = pathEnd{
    val tableDef = dbCrud.tableDef(tableName)
    tableDef.coerce[Any](tableDef.primaryKey.head, id) match {
      case Some(Success(resId)) =>
        put {
          putResource(tableName, resId)
        } ~
          get {
            getResource(tableName, resId)
          } ~
          delete {
            deleteResource(tableName, resId)
          }
      case Some(Failure(e)) =>
        logger.log(Level.WARNING, s"failed coercing resource id $id", e)
        complete(StatusCodes.BadRequest, s"invalid resource id $id type")
      case _ =>
        logger.severe(s"unexpected coercion failure")
        reject
    }
  }


  private def createResource(tableName:Symbol)=entity(as[Map[Symbol,Any]]){values =>
    val id = dbCrud.insert(tableName, values.toSeq :_*)
    getResource(tableName, id)
  }

  private def putResource(tableName:Symbol, id:Any)= entity(as[Map[Symbol, Any]]){values=>
    logger.info(s"put resource $tableName with id $id")
    dbCrud.update(tableName, id, values.toSeq :_*)
    getResource(tableName, id)
  }

  private def getResource(tableName:Symbol, id:Any)= complete{
    dbCrud.selectById(tableName, id)
  }

  private def deleteResource(tableName:Symbol, id:Any)= complete{
    "delete resource"
  }


  private def queryResources(tableName: Symbol)= {
    parameters('offset.?[Int](0), 'limit.?[Int](0)) { (offset, limit) =>
      parameterMap { params =>
        logger.info(s"filter params: $params")
        parsePredicate(tableName, params) match {
          case Left(failures) =>
            complete(StatusCodes.BadRequest, s"failed coercing parameters to sql types: ${failures.map(_.exception.getMessage).mkString(",")}")
          case Right(predicate) =>
            logger.info(s"predicate: $predicate")
            complete {
              val data = dbCrud.select(tableName, offset = offset, count = limit, where = predicate)
              QueryResult(data.size, offset, data.toSeq)
            }
        }
      }
    }
  }
}
