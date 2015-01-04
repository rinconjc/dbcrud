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

  def updateAll(table:Symbol, values: (Symbol, Any)*):Int

  def updateWhere(table:Symbol, where : Predicate, values: (Symbol, Any)*):Int

  def delete(table:Symbol,  id:Any):Int

  def select(table:Symbol, offset:Int=0, count:Int=Int.MaxValue):QueryData

  def selectWhere(table:Symbol, filters:(Symbol, Any)*):QueryData

}

object ColumnOps{
  implicit class PredicateColumn(val column:Symbol) extends AnyVal{
    def is(other:Any) = new BinaryExpression(column, other, "=")
    def lte(other:Any) = new BinaryExpression(column, other, "<=")
    def lt(other:Any) = new BinaryExpression(column, other, "<")
    def gte(other:Any) = new BinaryExpression(column, other, ">=")
    def gt(other:Any) = new BinaryExpression(column, other, ">")
  }
  
  implicit class RichCondition(val condition: Predicate) extends AnyVal{
    def and(other:Predicate) = new CompositePredicate(condition, other, "and")
    def or(other:Predicate) = new CompositePredicate(condition, other, "or")
  }
  
}

trait Predicate{
  def asSql:String
  def constants:Seq[Any]
}

class BinaryExpression(left:Symbol, right:Any, op:String) extends Predicate{
  override def asSql: String = right match {
    case other:Symbol => left.name + op + other.name
    case _ => left.name + op + "?"
  }

  override def constants: Seq[Any] = right match{
    case other:Symbol => Seq()
    case value => Seq(value)
  }
}

class CompositePredicate(left:Predicate, right: Predicate, op:String) extends Predicate{
  override def asSql: String = s"(${left.asSql}) $op (${right.asSql})"

  override def constants: Seq[Any] = left.constants ++ right.constants
}
