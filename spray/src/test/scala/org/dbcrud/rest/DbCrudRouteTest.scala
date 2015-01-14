package org.dbcrud.rest

import org.dbcrud.rest.DbCrudRoute.RowSerializer
import org.dbcrud.{DataCrud, QueryData}
import org.json4s.DefaultFormats
import org.mockito.Mockito._
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
/**
 * Created by julio on 12/01/15.
 */
class DbCrudRouteTest extends Specification with Specs2RouteTest with HttpService with Json4sSupport {
  def actorRefFactory = system
  implicit def json4sFormats = DefaultFormats + new RowSerializer

  private val dataCrud = mock(classOf[DataCrud])
  private val config = new Config
  private val dbCrudRoute = new DbCrudRoute(dataCrud, config).routes
  val restPrefix = "/" + config.restPrefix

  when(dataCrud.tableNames).thenReturn(Seq('table1, 'table2))

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
    when(dataCrud.select('table1)).thenReturn(new QueryData(Seq('field1, 'field2), Seq.range(0,10).map(i=>Array(s"Value$i", i))))

    Get(restPrefix + "/table1") ~> dbCrudRoute ~> check{
      val data = responseAs[QueryResult]
      data.count === 10
      data.offset === 0
      data.total === 10
      data.rows(0)[String]('field1) === "Value0"
    }
  }

  "retrieve rows with pagination" in{

    when(dataCrud.select('table1, offset=1, count=3)).thenReturn(new QueryData(Seq('field1, 'field2), Seq.range(0,3).map(i=>Array(s"Value$i", i))))

    Get(restPrefix + "/table1?offset=1&limit=3") ~> dbCrudRoute ~> check{
      val data = responseAs[QueryResult]
      data.count === 3
      data.offset === 1
      data.total === 10
      data.rows(0)[String]('field1) === "Value0"
    }
  }


}
