package org.dbcrud

/**
 * Created by rinconj on 15/12/14.
 */

object DataCrud{
  type Row = Seq[(Column,Any)]
}

case class Column(name:Symbol, dbType:Int)

class QueryData(columns:Seq[Column], rows:Iterable[Array[Any]]) extends Iterable[DataCrud.Row]{
  def asMaps = rows.map(r=>columns.map(_.name).zip(r).toMap)

  override def iterator = rows.iterator.map(values=>columns.zip(values).toSeq)
}

trait DataCrud {

  def createTable(name:Symbol, columns:(Symbol,Int)*)

  def tableNames:Iterable[String]

  def insert(table:Symbol, values: (Symbol,Any)*):Int

  def update(table:Symbol, id:Any, values: (Symbol, Any)*):Int

  def delete(table:Symbol,  id:Any):Int

  def select(table:Symbol, offset:Int=0, count:Int=Int.MaxValue):QueryData

  def selectWhere(table:Symbol, filters:(Symbol, Any)*):QueryData

}


