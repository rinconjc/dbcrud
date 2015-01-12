package org.dbcrud.rest

import akka.actor.ActorRefFactory
import org.dbcrud.DataCrud
import org.mockito.Mockito
import org.specs2.mutable.Specification
import spray.httpx.SprayJsonSupport
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import Mockito._

/**
 * Created by julio on 12/01/15.
 */
class DbCrudRouteTest extends Specification with Specs2RouteTest with HttpService with SprayJsonSupport {
  override implicit def actorRefFactory: ActorRefFactory = system

  private val dataCrud = mock(classOf[DataCrud])
  private val config = new Config
  private val dbCrudRoute = new DbCrudRoute(dataCrud, config).routes
  val restPrefix = "/" + config.restPrefix

  when(dataCrud.tableNames).thenReturn(Seq('table1, 'table2))

  "return the list of entities" in {
   Get(restPrefix + "/entities") ~> dbCrudRoute ~> check{
     responseAs[Seq[String]] === Seq('table1, 'table2)
   }

  }

}
