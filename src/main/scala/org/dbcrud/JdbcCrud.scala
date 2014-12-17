package org.dbcrud

import java.sql.ResultSet
import java.util.logging.Logger

/**
 * Created by rinconj on 16/12/14.
 */
class JdbcCrud(ds: ManagedDataSource, schema:String=null) extends DataCrud{
  private val logger  = Logger.getLogger(getClass.getName)

  private lazy val tables = ds.doWith {conn=>
    val rs = conn.getMetaData.getTables(null, schema, "%", Array("TABLE"))
    collect(rs, _.getString("TABLE_NAME"))
  }

  private lazy val typeMappings = ds.doWith{conn=>
    collect(conn.getMetaData.getTypeInfo, rs=>{
      rs.getInt("DATA_TYPE")->rs.getString("TYPE_NAME")
    }).toMap
  }

  private def collect[R](rs:ResultSet, rowMapper:ResultSet=>R):Seq[R]={
    Iterator.continually{
      if(rs.next()){
        Some(rowMapper(rs))
      } else None
    }.takeWhile(_.isDefined).flatten.toList
  }

  def createTable(name:Symbol, columns:(Symbol,Int)*){
    ds.doWith{conn=>
      val columnsLines = columns.map{case (colName, colType) => s"${colName.name} ${typeMappings(colType)}"}
      val sql = s"CREATE TABLE ${name.name} (${columnsLines.mkString(",")})"
      logger.info(s"executing: $sql")
      conn.createStatement().execute(sql)
    }
  }

  override def tableNames: Iterable[String] = tables

  override def insert(table: Symbol, values: (Symbol, Any)*): Int = {
    val (cols, vals) = values.unzip
    ds.doWith{conn=>
      val stmt = conn.prepareStatement( s"""INSERT INTO ${table.name} (${cols.map(_.name).mkString(",")})
        VALUES(${cols.map(_ => "?").mkString(",")})""")
      vals.zipWithIndex.foreach{t=>
        stmt.setObject(t._2+1, t._1)
      }
      stmt.executeUpdate()
    }
  }

  override def update(table: Symbol, id: Any, values: (Symbol, Any)*): Int = {
    val (cols, vals) = values.unzip
    ds.doWith{conn=>
      conn.prepareStatement(s"UPDATE ${table.name} SET " + cols.map(_.name + " = ?").mkString(",") + " where  = ?")
    }
  }

  override def selectWhere(table: Symbol, filters: (Symbol, Any)*): QueryData = ???

  override def delete(table: Symbol, id: Any): Int = ???

  override def select(table: Symbol, offset: Int, count: Int): QueryData = ???
}
