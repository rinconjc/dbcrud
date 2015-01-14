package org.dbcrud.rest

import org.dbcrud.Row

/**
 * Created by rinconj on 14/01/15.
 */
case class QueryResult(count:Int, total:Int, offset:Int, rows:Seq[Row]) {

}
