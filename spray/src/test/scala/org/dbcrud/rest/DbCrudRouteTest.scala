package org.dbcrud.rest

import org.dbcrud.rest.DbCrudRoute.RowSerializer
import org.dbcrud.{ColumnOps, DataCrud, QueryData}
import org.json4s.DefaultFormats
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
/**
 * Created by julio on 12/01/15.
 */
class DbCrudRouteTest extends Specification with Specs2RouteTest with HttpService with Json4sSupport with Mockito {
  def actorRefFactory = system
  implicit def json4sFormats = DefaultFormats + new RowSerializer

  private val dataCrud = mock[DataCrud]
  private val config = new Config
  private val dbCrudRoute = new DbCrudRoute(dataCrud, config).routes
  val restPrefix = "/" + config.restPrefix

  dataCrud.tableNames returns Seq('table1, 'table2)

  sequential

  "return the list of resources" in {
    Get(restPrefix + "/resources") ~> dbCrudRoute ~> check {
      responseAs[Seq[Symbol]] === Seq('table1, 'table2)
    }
  }

  "respond 404 for invalid entities" in{
    Get(restPrefix +"/tablex") ~> dbCrudRoute ~> check{
      status === NotFound
    }
  }

  "retrieve records in a table" in{
    dataCrud.select('table1, offset=0, count=0) returns new QueryData(Seq('field1, 'field2), Seq.range(0,10).map(i=>Array(s"Value$i", i)))

    Get(restPrefix + "/table1") ~> dbCrudRoute ~> check{
      status === OK
      val data = responseAs[QueryResult]
      data.count === 10
      data.offset === 0
      data.rows(0)[String]('field1) === "Value0"
    }
  }

  "retrieve rows with pagination" in{

    dataCrud.select('table1, offset=1, count=3) returns new QueryData(Seq('field1, 'field2), Seq.range(0,3).map(i=>Array(s"Value$i", i)))

    Get(restPrefix + "/table1?offset=1&limit=3") ~> dbCrudRoute ~> check{
      status === OK
      val data = responseAs[QueryResult]
      data.count === 3
      data.offset === 1
      data.rows(0)[String]('field1) === "Value0"
    }
  }

  "retrieve rows with filter conditions" in {
    import ColumnOps._
    dataCrud.select('table1, offset=0, count=0, where=Seq('field2->2, 'field3->"abracadabra")) returns new QueryData(Seq('field1, 'field2), Seq.range(0,3).map(i=>Array(s"Value$i", i)))

    Get(restPrefix + "/table1?field2=2&field3=abracadabra") ~> dbCrudRoute ~> check{
      status === OK
      val data = responseAs[QueryResult]
      data.count === 3
      data.offset === 0
      data.rows(0)[String]('field1) === "Value0"
    }

  }


}
