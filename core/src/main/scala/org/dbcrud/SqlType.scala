package org.dbcrud

import java.sql.{Timestamp, Types}
import java.text.SimpleDateFormat
import java.util.Date

import scala.util.Try

/**
 * Created by julio on 15/01/15.
 */
object SqlType{
  private val types:Map[Int, SqlType[_]] = Seq(SqlInt, SqlDouble, SqlBoolean, SqlString, SqlDate, SqlTimestamp).flatMap(t=>t.jdbcTypes.map(_->t)).toMap
  def apply(jdbcType:Int) =  types(jdbcType)
  def get(jdbcType:Int) =  types.get(jdbcType)
}

sealed trait SqlType[T]{
  def fromString(value:String):T
  def fromString_?(value:String):Option[T] = Option(value).map(fromString)
  def jdbcTypes:Set[Int]
  def ddl(typeName:String, size:Int=0, nullable:Boolean = true) = s"$typeName ${if(nullable) "NULL" else "NOT NULL"}"
}

case object SqlInt extends SqlType[Int]{
  override def fromString(value: String):Int = value.toInt

  override def jdbcTypes: Set[Int] = Set(Types.INTEGER, Types.SMALLINT, Types.TINYINT)
}

case object SqlLong extends SqlType[Long]{
  override def fromString(value: String):Long = value.toLong

  override def jdbcTypes: Set[Int] = Set(Types.INTEGER, Types.SMALLINT, Types.TINYINT)
}

case object SqlDouble extends SqlType[Double]{
  override def fromString(value: String): Double = value.toDouble

  override def jdbcTypes: Set[Int] = Set(Types.NUMERIC, Types.DECIMAL, Types.FLOAT, Types.DOUBLE)
}

case object SqlBoolean extends SqlType[Boolean]{
  override def fromString(value: String): Boolean = value.toBoolean

  override def jdbcTypes: Set[Int] = Set(Types.BOOLEAN, Types.BIT)
}

case object SqlString extends SqlType[String]{
  override def fromString(value: String): String = value

  override def jdbcTypes: Set[Int] = Set(Types.CHAR, Types.VARCHAR, Types.NCHAR, Types.NVARCHAR)

  override def ddl(typeName: String, size: Int=0, nullable: Boolean=true): String = s"$typeName($size) ${if(nullable) "NULL" else "NOT NULL"}"
}


case object SqlDate extends SqlType[Date]{
  override def fromString(value: String): Date = Try(new Date(value.toLong)).recoverWith{
    case e:Exception => Try(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(value))
  }.get

  override def jdbcTypes: Set[Int] = Set(Types.DATE)
}

case object SqlTimestamp extends SqlType[Timestamp]{
  override def fromString(value: String): Timestamp = Try(new Date(value.toLong)).recoverWith{
    case e:Exception => Try(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(value))}.
    map(d=>new Timestamp(d.getTime())).get

  override def jdbcTypes: Set[Int] = Set(Types.TIMESTAMP, Types.TIME)
}
