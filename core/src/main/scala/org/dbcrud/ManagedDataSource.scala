package org.dbcrud

import java.sql.Connection
import java.util.logging.{Level, Logger}
import javax.sql.DataSource

import scala.util.{Failure, Try}

/**
 * Created by rinconj on 17/12/14.
 */

class ManagedDataSource(ds: DataSource) {
  private val logger = Logger.getLogger(getClass.getName)

  def doWith[T](f: Connection => T):Try[T] = {
    val conn = Try(ds.getConnection).recoverWith{
      case e:Exception =>
        logger.log(Level.SEVERE, "Failed establishing connection", e)
        Failure(e)
    }
    try{
      conn.map(c=>f(c)).recoverWith{
        case e:Exception =>
          logger.log(Level.SEVERE, "Failed executing DB operation", e)
          Failure(e)
      }
    } finally {
      conn.map(_.close())
    }
  }
}
