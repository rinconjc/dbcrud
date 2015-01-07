package org.dbcrud

import java.sql.{DatabaseMetaData, ResultSet}
import java.util.logging.{Level, Logger}

import org.dbcrud.dialects.DbmsDialect

import scala.util.{Failure, Try}
import scalaz.Scalaz._

class JdbcCrud(ds: ManagedDataSource, schema:String=null, dbmsDialect: DbmsDialect = null) extends DataCrud{
  private val logger  = Logger.getLogger(getClass.getName)

  private lazy val dbms = ds.doWith(_.getMetaData.getDatabaseProductName)

  private val dialect = Option(dbmsDialect).orElse(inferDialect(ds))

  private lazy val tables = ds.doWith {conn=>
    val rs = conn.getMetaData.getTables(null, schema, "%", Array("TABLE"))
    collect(rs, {rs=>
      val tableName = rs.getString("TABLE_NAME")
      DbTable(Symbol(tableName), columns(tableName), primaryKeys(tableName))
    })
  }

  private lazy val typeMappings = ds.doWith{conn=>
    collect(conn.getMetaData.getTypeInfo, rs=>{
      rs.getInt("DATA_TYPE")->rs.getString("TYPE_NAME")
    }).toMap
  }

  private def inferDialect(ds:ManagedDataSource):Option[DbmsDialect]={
    val dbms = ds.doWith(_.getMetaData.getDatabaseProductName)

    Try(Class.forName(s"org.dbcrud.dialects.${dbms}SqlDialect")).recoverWith{
      case e:Exception =>
        logger.warning(s"no dialect found in classpath: $e")
        Failure(e)
    }.flatMap(cls=>Try(cls.newInstance().asInstanceOf[DbmsDialect]).recoverWith{
      case e:Exception =>
        logger.log(Level.SEVERE, "failed instantiating dialect class", e)
        Failure(e)
    }).toOption
  }

  private def primaryKeys(table:String) = ds.doWith{conn=>
    collect(conn.getMetaData.getPrimaryKeys(null, null, table), rs=>{
      rs.getString("COLUMN_NAME")->rs.getInt("KEY_SEQ")
    }).sortBy(_._2).map(p=>Symbol(p._1))
  }

  private def columns(table:String) = ds.doWith{conn=>
    collect(conn.getMetaData.getColumns(null,null,table,"%"), {rs=>
      DbColumn(Symbol(rs.getString("COLUMN_NAME")), rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE"), rs.getInt("DECIMAL_DIGITS"),
        rs.getInt("NULLABLE")==DatabaseMetaData.attributeNullable, rs.getString("IS_AUTOINCREMENT")=="YES", rs.getString("IS_GENERATEDCOLUMN")=="YES")
    })
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

  override def tableNames: Iterable[Symbol] = tables.map(_.name)

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

  override def updateAll(table: Symbol, values: (Symbol, Any)*): Int = {
    updateWhere(table, EmptyPredicate, values: _*)
  }

  override def updateWhere(table: Symbol, where: Predicate, values: (Symbol, Any)*): Int = {
    val (cols, vals) = values.unzip
    ds.doWith{conn=>
      val ps = conn.prepareStatement(s"UPDATE ${table.name} SET ${cols.map(_.name + " = ?").mkString(",")} ${where.whereSql}")
      values.zipWithIndex.foreach{t=>
        ps.setObject(t._2+1, t._1)
      }
      where.constants.zipWithIndex.foreach{t=>
        ps.setObject(values.size + t._2+1, t._1)
      }
      ps.executeUpdate()
    }
  }

  override def delete(table: Symbol, id: Any): Int = ???

  override def select(table: Symbol, where:Predicate=EmptyPredicate, offset: Int=0, count: Int=0
                      , orderBy:Seq[(Symbol, ColumnOrder)]=Nil): QueryData = {
    ds.doWith{conn=>
      val ps = (offset>0).option().flatMap{_=>
        dialect.map(_.selectStatement(conn, table.name, offset, count, orderBy))
      }.getOrElse(conn.prepareStatement(s"SELECT * FROM ${table.name} " + (orderBy.isEmpty? ""|s"ORDER BY ${orderBy.mkString(",")}")))

      if(count>0){
        ps.setMaxRows(count)
      }
      toQueryData(ps.executeQuery())
    }
  }

  private def toQueryData(rs: ResultSet): QueryData = {
    val rsMeta = rs.getMetaData
    val columns = for (c <- 1 to rsMeta.getColumnCount) yield Symbol(rsMeta.getColumnName(c))
    val rows = collect(rs, { rs =>
      Array.range(1, columns.size + 1).map(rs.getObject(_).asInstanceOf[Any])
    })
    new QueryData(columns, rows)
  }
}
