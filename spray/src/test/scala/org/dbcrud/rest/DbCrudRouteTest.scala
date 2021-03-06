package org.dbcrud.rest

import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import org.dbcrud.rest.DbCrudRoute.RowSerializer
import org.dbcrud._
import org.json4s.DefaultFormats
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import collection.JavaConversions._
import ColumnOps._
/**
 * Created by julio on 12/01/15.
 */
class DbCrudRouteTest extends Specification with Specs2RouteTest with HttpService with Json4sSupport with Mockito {
  def actorRefFactory = system
  implicit def json4sFormats = DefaultFormats + new RowSerializer

  private val dataCrud = mock[DataCrud]
  private val config = ConfigFactory.load().withValue("dbcrud.rest.aliases", ConfigValueFactory.fromMap(Map("tasks"->"table1")))
  private val dbCrudRoute = new DbCrudRoute(dataCrud, config).routes
  val restPrefix = "/" + config.getString("dbcrud.rest.prefix")

  dataCrud.tableNames returns Seq('table1, 'table2)
  dataCrud.tableDef('table1) returns DbTable('table1, Seq(DbColumn[Long]('id, SqlInt), DbColumn[String]('field1, SqlVarchar, 40), DbColumn[Long]('field2, SqlInt, 4)), Seq('id))

  sequential

  "return the list of resources" in {
    Get(restPrefix + "/resources") ~> dbCrudRoute ~> check {
      responseAs[Seq[Symbol]] === Seq('tasks, 'table2)
    }
  }

  "respond 404 for invalid entities" in{
    Get(restPrefix +"/tablex") ~> dbCrudRoute ~> check{
      status === NotFound
    }
  }

  "retrieve records in a table" in{
    dataCrud.select('table1, offset=0, count=0, where=EmptyPredicate) returns new QueryData(Seq('field1, 'field2), Seq.range(0,10).map(i=>Array(s"Value$i", i)))

    Get(restPrefix + "/tasks") ~> dbCrudRoute ~> check{
      status === OK
      val data = responseAs[QueryResult]
      data.count === 10
      data.offset === 0
      data.rows(0)[String]('field1) === "Value0"
    }
  }

  "retrieve rows with pagination" in{

    dataCrud.select('table1, offset=1, count=3, where=EmptyPredicate) returns new QueryData(Seq('field1, 'field2), Seq.range(0,3).map(i=>Array(s"Value$i", i)))

    Get(restPrefix + "/tasks?offset=1&limit=3") ~> dbCrudRoute ~> check{
      status === OK
      val data = responseAs[QueryResult]
      data.count === 3
      data.offset === 1
      data.rows(0)[String]('field1) === "Value0"
    }
  }

  "retrieve rows with filter conditions" in {
    dataCrud.select('table1, offset=0, count=0, where=Seq('field1->"abracadabra", 'field2->2)) returns new QueryData(Seq('field1, 'field2), Seq.range(0,3).map(i=>Array(s"Value$i", i)))

    Get(restPrefix + "/tasks?field2=2&field1=abracadabra") ~> dbCrudRoute ~> check{
      status === OK
      val data = responseAs[QueryResult]
      data.count === 3
      data.offset === 0
      data.rows(0)[String]('field1) === "Value0"
    }
  }

  "reject invalid filter conditions" in {
    Get(restPrefix + "/tasks?field2=abc&field1=abracadabra") ~> dbCrudRoute ~> check{
      status === BadRequest
    }
  }

  "insert a record" in {
    dataCrud.insert('table1, 'field1->100, 'field2->"abracadabra") returns 10
    dataCrud.selectById('table1, 10) returns Map('id->10, 'field1->100, 'field2->"abracadabra")

    Post(restPrefix + "/tasks", Map('field1->100, 'field2->"abracadabra")) ~> dbCrudRoute ~> check{
      status === OK
      responseAs[Map[String,Any]].get("id") === Some(10)
    }
  }

  "update a record" in {
    dataCrud.selectById('table1, 10L) returns Map('id->10, 'field1->200, 'field2->"abracadabra")

    Put(restPrefix + "/tasks/10", Map('field1->200)) ~> dbCrudRoute ~> check{
      status === OK
      responseAs[Map[Symbol, Any]] === Map('id->10, 'field1->200, 'field2->"abracadabra")
    }
  }

  "delete a record" in{
    Delete(restPrefix + "/tasks/10") ~> dbCrudRoute ~> check{
      status === OK
    }
  }

}
