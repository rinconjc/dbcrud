package org.dbcrud.dialects

import java.sql.{PreparedStatement, Connection, Statement}

import org.dbcrud.{Predicate, ColumnOrder}

/**
 * Created by rinconj on 16/12/14.
 */
import scalaz.Scalaz._

trait DbmsDialect{

  def selectStatement(con:Connection, table:String, where:Predicate, offset:Int, limit:Int, orderBy:Seq[ColumnOrder]):PreparedStatement

}

trait SelectWithOffsetLimit{
  def selectStatement(con:Connection, table:String, where:Predicate, offset:Int, limit:Int, orderBy:Seq[ColumnOrder]): PreparedStatement ={
    val orderSql = orderBy.isEmpty? "" | s"ORDER BY ${orderBy.mkString(",")}"
    val ps = con.prepareStatement(s"SELECT * FROM $table ${where.whereSql} $orderSql LIMIT ? OFFSET ?")

    where.constants.zipWithIndex.foreach{t=>
      ps.setObject(t._2+1, t._1)
    }
    val count = where.constants.size
    ps.setInt(count+1, limit)
    ps.setInt(count+2, offset)
    ps
  }
}
