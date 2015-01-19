package org.dbcrud

import java.sql.{Timestamp, Types}
import java.text.SimpleDateFormat
import java.util.Date

import scala.util.Try

/**
 * Created by julio on 15/01/15.
 */
object SqlType {
  private val sqlTYpes:Map[Int, SqlType[_]] = Seq(SqlInt, SqlDouble, SqlBooelan, SqlVarchar, SqlChar, SqlNChar, SqlNVarchar, SqlDate, SqlTimestamp).map(t=>t.sqlType->t).toMap
  def apply[T](typeId:Int):SqlType[T] = sqlTYpes(typeId).asInstanceOf[SqlType[T]]
}

sealed trait SqlType[T]{
  def fromString(value:String):T
  def sqlType:Int
  def fromString_?(value:String):Option[T] = Option(value).map(fromString)
}

case object SqlInt extends SqlType[Long]{
  override def fromString(value: String):Long = value.toLong
  override def sqlType = Types.INTEGER
}

case object SqlDouble extends SqlType[Double]{
  override def fromString(value: String): Double = value.toDouble
  override def sqlType = Types.NUMERIC
}

case object SqlBooelan extends SqlType[Boolean]{
  override def fromString(value: String): Boolean = value.toBoolean
  override def sqlType = Types.BIT
}

abstract class SqlString(val sqlType:Int) extends SqlType[String]{
  override def fromString(value: String): String = value
}

case object SqlVarchar extends SqlString(Types.VARCHAR)
case object SqlChar extends SqlString(Types.CHAR)
case object SqlNVarchar extends SqlString(Types.NVARCHAR)
case object SqlNChar extends SqlString(Types.NCHAR)

case object SqlDate extends SqlType[Date]{
  override def fromString(value: String): Date = Try(new Date(value.toLong)).recoverWith{
    case e:Exception => Try(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(value))
  }.get

  override def sqlType = Types.DATE
}

case object SqlTimestamp extends SqlType[Timestamp]{
  override def fromString(value: String): Timestamp = Try(new Date(value.toLong)).recoverWith{
    case e:Exception => Try(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(value))}.
    map(d=>new Timestamp(d.getTime())).get

  override def sqlType = Types.TIMESTAMP
}
