package org.dbcrud.dialects

import java.sql.{PreparedStatement, Connection, Statement}

/**
 * Created by rinconj on 16/12/14.
 */

trait DbmsDialect{

  def selectStatement(con:Connection, table:String, offset:Int, limit:Int):PreparedStatement

}

trait SelectWithOffsetLimit{
  def selectStatement(con:Connection, table:String, offset:Int, limit:Int): PreparedStatement ={
    val ps = con.prepareStatement(s"SELECT * FROM $table LIMIT ? OFFSET ?")
    ps.setInt(1, limit)
    ps.setInt(2, offset)
    ps
  }
}
