package org.dbcrud.rest

import com.typesafe.config.{ConfigFactory, Config}
import collection.JavaConversions._
/**
 * Created by julio on 13/01/15.
 */
class Settings(config:Config) {

  // validate vs. reference.conf
  config.checkValid(ConfigFactory.defaultReference(), "dbcrud.rest")

  def restPrefix = config.getString("dbcrud.rest.prefix")

  def restAliases = config.getConfig("dbcrud.rest.aliases").entrySet().map(e=>e.getKey->e.getValue.unwrapped().asInstanceOf[String]).toMap

}
