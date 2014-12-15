package org.dbcrud

/**
 * Created by rinconj on 15/12/14.
 */
case class Column(name:Symbol, dbType:Int)

class QueryData(columns:Vector[Column], rows:Iterable[Vector[Any]]){
  def asMaps = rows.map(r=>columns.map(_.name).zip(r).toMap)
}

trait DbCrud {

  type Row = Seq[(Symbol,Any)]

  def tableNames:Iterable[String]

  def insert(table:Symbol, values: (Symbol,Any)*):Int

  def update(table:Symbol, id:Any, values: (Symbol, Any)*):Int

  def delete(table:Symbol,  id:Any):Int

  def select(table:Symbol, offset:Int=0, count:Int=Int.MaxValue, filters:Seq[(Symbol, Any)]):QueryData

  def selectAll(table:Symbol):QueryData

}


