package org.dbcrud.dialects

import org.dbcrud.{SqlLong, SqlString, SqlInt, SqlType}

/**
 * Created by julio on 6/01/15.
 */
class H2SqlDialect extends DbmsDialect with SelectWithOffsetLimit{
  override val typeMapping: Map[SqlType[_], String] = Map(
  SqlInt -> "number",
  SqlLong -> "bigint",
  SqlString -> "varchar"
  )
}
