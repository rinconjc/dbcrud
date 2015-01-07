package org.dbcrud.dialects

import java.sql.{PreparedStatement, Connection, Statement}

import org.dbcrud.ColumnOrder

/**
 * Created by rinconj on 16/12/14.
 */
import scalaz.Scalaz._

trait DbmsDialect{

  def selectStatement(con:Connection, table:String, offset:Int, limit:Int, orderBy:Seq[(Symbol, ColumnOrder)]):PreparedStatement

}

trait SelectWithOffsetLimit{
  def selectStatement(con:Connection, table:String, offset:Int, limit:Int, orderBy:Seq[ColumnOrder]): PreparedStatement ={
    val orderSql = orderBy.isEmpty? "" | s"ORDER BY ${orderBy.mkString(",")}"
    val ps = con.prepareStatement(s"SELECT * FROM $table $orderSql LIMIT ? OFFSET ?")
    ps.setInt(1, limit)
    ps.setInt(2, offset)
    ps
  }
}
