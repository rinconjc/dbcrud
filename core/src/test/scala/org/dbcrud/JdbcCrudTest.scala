package org.dbcrud

import java.util.Date
import javax.sql.DataSource

import org.dbcrud.ColumnOps._
import org.h2.jdbcx.JdbcDataSource

import scala.util.Success

/**
 * Created by rinconj on 15/12/14.
 */
abstract class JdbcCrudTest(ds:DataSource) extends org.specs2.mutable.Specification {
  val dataSource = new ManagedDataSource(ds)

  val dbCrud = new JdbcCrud(dataSource)

  sequential

  step{
    dbCrud.execSql("DROP TABLE ACCOUNT");
    dbCrud.execSql("DROP TABLE ACT_TRANS");
    dbCrud.createTable('ACCOUNT, DbColumn('id, SqlInt), DbColumn('name, SqlString, 50), DbColumn('opened_at, SqlDate))
    dbCrud.createTable('ACT_TRANS, DbColumn('id, SqlInt), DbColumn('description, SqlString, 40), DbColumn('amount, SqlDecimal))
  }

  "list tables" in {
    dbCrud.tableNames.toSet shouldEqual(Set('ACCOUNT, 'ACT_TRANS))
  }

  "insert data" in {
    dbCrud.insert('ACCOUNT, 'id->1, 'name->"account 1", 'opened_at->new Date) shouldEqual(Success(1))
  }

  "update data" in {
    dbCrud.insert('ACCOUNT, 'id->99, 'name->"account 99", 'opened_at->new Date)
    dbCrud.updateAll('ACCOUNT, 'name->"account updated") should beASuccessfulTry(beGreaterThan(0))
    dbCrud.updateWhere('ACCOUNT, ('id is 99) or ('name is "account 99"), 'name -> "Account 99") shouldEqual(Success(1))
  }

  "select data" in {
    for(i<-2 to 10) dbCrud.insert('ACCOUNT, 'id->i, 'name->s"account $i", 'opened_at->new Date)
    val result = dbCrud.select('ACCOUNT, offset=1, count=2)
    result should beASuccessfulTry
    result.get.head[Long]('ID) should_== 2L
  }

}

class H2SqlCrudTest extends JdbcCrudTest({
  val ds = new JdbcDataSource()
  ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
  ds
})

class OracleSqlCrudTest extends JdbcCrudTest({
  val ds = new oracle.jdbc.pool.OracleConnectionPoolDataSource()
  ds.setURL("jdbc:oracle:thin:@localhost:1521:XE")
  ds.setUser("crud_test")
  ds.setPassword("crud_test")
  ds
}){

  override val dbCrud = new JdbcCrud(dataSource, "CRUD_TEST")

}


