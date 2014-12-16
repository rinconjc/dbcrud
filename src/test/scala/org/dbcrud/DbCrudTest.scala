package org.dbcrud

import java.util.Date

import org.h2.jdbcx.JdbcDataSource

/**
 * Created by rinconj on 15/12/14.
 */
class DbCrudTest extends org.specs2.mutable.Specification {
  val dataSource = {
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:test")
    ds
  }

  val dbCrud:DataCrud


  "list tables" in {
    dbCrud.tableNames.toSet shouldEqual(Set("ACCOUNT", "TRANSACTION", "PRODUCT"))
  }

  "insert data" in {
    dbCrud.insert('ACCOUNT, 'id->1, 'name->"account 1", 'opened_at->new Date) shouldEqual(1)
  }

  "update data" in {
    dbCrud.update('ACCOUNT, 1, 'name->"account updated")
  }

  "select data" in {
    dbCrud.select('ACCOUNT)
  }

}
