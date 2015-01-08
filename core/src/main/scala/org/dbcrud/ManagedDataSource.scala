package org.dbcrud

import java.sql.Connection
import javax.sql.DataSource

/**
 * Created by rinconj on 17/12/14.
 */
class ManagedDataSource(ds: DataSource) {

  def doWith[T](f: Connection => T) = {
    val conn = ds.getConnection
    try {
      f(conn)
    } finally {
      conn.close()
    }
  }
}
