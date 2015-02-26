package org.dbcrud

/**
 * Created by rinconj on 18/02/15.
 */
object ColumnOps {

  implicit class PredicateColumn(val column: Symbol) extends AnyVal {
    def is(other: Any) = new BinaryExpression(column, other, "=")

    def lte(other: Any) = new BinaryExpression(column, other, "<=")

    def lt(other: Any) = new BinaryExpression(column, other, "<")

    def gte(other: Any) = new BinaryExpression(column, other, ">=")

    def gt(other: Any) = new BinaryExpression(column, other, ">")
  }

  implicit class RichCondition(val condition: Predicate) extends AnyVal {
    def and(other: Predicate) = new CompositePredicate(condition, other, "and")

    def or(other: Predicate) = new CompositePredicate(condition, other, "or")
  }

  implicit def simpleConditions(conditions: Seq[(Symbol, Any)]): SimpleConditions = SimpleConditions(conditions)

}

sealed trait ColumnOrder {
  def toSql: String

  override def toString = toSql
}

case class Asc(column: Symbol) extends ColumnOrder {
  override def toSql: String = column.name
}

case class Desc(column: Symbol) extends ColumnOrder {
  override def toSql: String = column.name + " DESC"
}

trait Predicate {
  def asSql: String

  def constants: Seq[Any]

  def whereSql = "WHERE " + asSql
}

object EmptyPredicate extends Predicate {
  override def asSql: String = ""

  override def constants: Seq[Any] = Nil

  override def whereSql: String = ""
}

case class SimpleConditions(conditions: Seq[(Symbol, Any)]) extends Predicate {
  override def asSql: String = conditions.map {
    case (symb: Symbol, value: Symbol) => symb.name + " = " + value.name
    case (symb: Symbol, value: Any) => symb.name + " = ?"
  }.mkString(" AND ")

  override def constants: Seq[Any] = conditions.map(_._2).filterNot(_.isInstanceOf[Symbol])
}

class BinaryExpression(left: Symbol, right: Any, op: String) extends Predicate {
  override def asSql: String = right match {
    case other: Symbol => left.name + op + other.name
    case _ => left.name + op + "?"
  }

  override def constants: Seq[Any] = right match {
    case other: Symbol => Seq()
    case value => Seq(value)
  }
}

class CompositePredicate(left: Predicate, right: Predicate, op: String) extends Predicate {
  override def asSql: String = s"(${left.asSql}) $op (${right.asSql})"

  override def constants: Seq[Any] = left.constants ++ right.constants
}
