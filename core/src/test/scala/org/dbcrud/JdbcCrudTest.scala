package org.dbcrud

import java.util.Date
import javax.sql.DataSource

import org.dbcrud.ColumnOps._
import org.h2.jdbcx.JdbcDataSource

/**
 * Created by rinconj on 15/12/14.
 */
abstract class JdbcCrudTest(ds:DataSource) extends org.specs2.mutable.Specification {
  val dataSource = new ManagedDataSource(ds)

  val dbCrud = new JdbcCrud(dataSource)

  step{
    dbCrud.createTable('ACCOUNT, DbColumn('id, SqlInt), DbColumn('name, SqlString, 50), DbColumn('opened_at, SqlDate))
    dbCrud.createTable('TRANSACTION, DbColumn('id, SqlInt), DbColumn('desc, SqlString, 40), DbColumn('amount, SqlDate))
  }

  "list tables" in {
    dbCrud.tableNames.toSet shouldEqual(Set("ACCOUNT", "TRANSACTION"))
  }

  "insert data" in {
    dbCrud.insert('ACCOUNT, 'id->1, 'name->"account 1", 'opened_at->new Date) shouldEqual(1)
  }

  "update data" in {
    dbCrud.insert('ACCOUNT, 'id->99, 'name->"account 99", 'opened_at->new Date)
    dbCrud.updateAll('ACCOUNT, 'name->"account updated") should beGreaterThan(0)
    dbCrud.updateWhere('ACCOUNT, ('id is 99) or ('name is "account 99"), 'name -> "Account 99") shouldEqual(1)
  }

  "select data" in {
    for(i<-2 to 10) dbCrud.insert('ACCOUNT, 'id->i, 'name->s"account $i", 'opened_at->new Date)
    val result = dbCrud.select('ACCOUNT, offset=1, count=2)
    result should haveSize(2)
    result.head[Int]('ID) should_== 2
  }

}

class H2SqlCrudTest extends JdbcCrudTest({
  val ds = new JdbcDataSource()
  ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
  ds
})

class OracleSqlCrudTest extends JdbcCrudTest({
  val ds = new oracle.jdbc.pool.OracleConnectionPoolDataSource()
  ds.setURL("jdbc:oracle:thin:@localhost:49161:XE")
  ds.setUser("crud_test")
  ds.setPassword("crud_test")
  ds
})


