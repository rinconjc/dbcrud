package org.dbcrud.rest

import spray.routing.HttpServiceActor

/**
 * Created by julio on 9/01/15.
 */
class DbCrudService(dbCrud:DataCrud) extends HttpServiceActor {

  def isValidEntity(entity:String):Boolean = ???

  def receive = runRoute {
    path("rest" / Segment) {entity =>
      //check that entity is valid!
      validate(isValidEntity(entity), s"invalid entity $entity"){
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
