package org.dbcrud.rest

import java.util.logging.Logger

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
class DbCrudRoute(dbCrud:DataCrud, config:Config) extends spray.routing.Directives with Json4sSupport {
  private val logger = Logger.getLogger(classOf[DbCrudRoute].getName)

  implicit def json4sFormats = DefaultFormats + new RowSerializer

  def isValidEntity(entity:String):Boolean = dbCrud.tableNames.exists(_.name == entity)

  private def parsePredicate(entity:String, params:Map[String,String]):Either[Seq[Failure[_]],Predicate]={
    val tableDef = dbCrud.tableDef(Symbol(entity))
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

  def routes = pathPrefix(config.restPrefix) {
    path("resources"){
      get{
        complete(dbCrud.tableNames)
      }
    } ~ path(Segment) { entity =>
      if(!isValidEntity(entity))
        complete(StatusCodes.NotFound, s"$entity resource not found")
      else{
        pathEnd {
          post {
            complete {
              "post entity"
            }
          } ~
            get {
              parameters('offset.?[Int](0), 'limit.?[Int](0)){(offset, limit)=>
                parameterMap{params=>
                  logger.info(s"filter params: $params")
                  parsePredicate(entity, params) match{
                    case Left(failures)=>
                      complete(StatusCodes.BadRequest, s"failed coercing parameters to sql types: ${failures.map(_.exception.getMessage).mkString(",")}")
                    case Right(predicate) =>
                      logger.info(s"predicate: $predicate")
                      complete {
                        val data = dbCrud.select(Symbol(entity),  offset = offset, count = limit, where = predicate)
                        QueryResult(data.size, offset, data.toSeq)
                      }
                  }
                }
              }
            }
        } ~
          path(Segment) { id =>
            put {
              complete {
                "update " + id
              }
            } ~
              get {
                complete {
                  "get by id" + id
                }
              } ~
              delete {
                complete {
                  "delete by id"
                }
              }
          }
      }
    }
  }
}
