package org.dbcrud.rest

import org.dbcrud._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by julio on 9/01/15.
 */
class DbCrudRoute(dbCrud:DataCrud, config:Config) extends spray.routing.Directives with SprayJsonSupport with DefaultJsonProtocol {

  def isValidEntity(entity:String):Boolean = dbCrud.tableNames.exists(_.name == entity)

  def routes = pathPrefix(config.restPrefix) {
    path("entities"){
      get{
        complete(dbCrud.tableNames)
      }
    } ~ path(Segment) { entity =>
      //check that entity is valid!
      validate(isValidEntity(entity), s"invalid entity $entity") {
        pathEnd {
          post {
            complete {
              "post entity"
            }
          } ~
            get {
              complete {
                "retrieve entities"
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
