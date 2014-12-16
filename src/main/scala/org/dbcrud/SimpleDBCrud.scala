package org.dbcrud

import javax.sql.DataSource

/**
 * Created by rinconj on 16/12/14.
 */
class SimpleDBCrud(ds: DataSource, schema:String=null) extends DataCrud{

  private lazy val tables = {
    val conn = ds.getConnection
    val rs = conn.getMetaData.getTables(null, schema, "", Array("TABLE"))    
    Iterator.continually{
      if(rs.next()){
        Some(rs.getString("TABLE_NAME"))
      } else None
    }.takeWhile(_.isDefined).flatten
  }

  override def tableNames: Iterable[String] = ???

  override def update(table: Symbol, id: Any, values: (Symbol, Any)*): Int = ???

  override def insert(table: Symbol, values: (Symbol, Any)*): Int = ???

  override def selectWhere(table: Symbol, filters: (Symbol, Any)*): QueryData = ???

  override def delete(table: Symbol, id: Any): Int = ???

  override def select(table: Symbol, offset: Int, count: Int): QueryData = ???
}
