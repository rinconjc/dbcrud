import sbt._
import Keys._

object DbCrudBuild extends Build {
  lazy val root = Project(id = "dbcrud", base = file(".")) aggregate(core, rest)

  lazy val core = Project(id = "dbcrud-core", base = file("core"))

  lazy val rest = Project(id = "dbcrud-rest", base = file("rest"))

  scalaVersion:="2.11.2"

}