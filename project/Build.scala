import sbt._
import Keys._

object DbCrudBuild extends Build {

  organization:= "org.dbcrud"

  version:="0.1-SNAPSHOT"

  lazy val root = Project(id = "dbcrud", base = file(".")) aggregate(core, rest)

  lazy val core = Project(id = "dbcrud-core", base = file("core"))

  lazy val rest = Project(id = "dbcrud-spray", base = file("spray"))

  scalaVersion:="2.11.2"

  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

}