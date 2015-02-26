package org.dbcrud.dialects

import org.dbcrud._

/**
 * Created by julio on 6/01/15.
 */
class H2SqlDialect extends DbmsDialect with SelectWithOffsetLimit{
  override val typeMapping: Map[SqlType[_], String] = Map(
  SqlInt -> "bigint",
  SqlString -> "varchar",
  SqlDouble -> "double",
  SqlDecimal -> "decimal",
  SqlBoolean -> "boolean",
  SqlDate -> "date",
  SqlTimestamp -> "timestamp"
  )
}

class OracleSqlDialect extends DbmsDialect with SelectWithOffsetLimit{
  override val typeMapping: Map[SqlType[_], String] = Map(
  SqlInt -> "number",
  SqlString -> "varchar2",
  SqlDouble -> "real",
  SqlDecimal -> "decimal",
  SqlBoolean -> "number",
  SqlDate -> "date",
  SqlTimestamp -> "timestamp"
  )
}


