import sbt._
import Keys._

object DbCrudBuild extends Build {

  lazy val commonSettings = Seq(
    organization:= "org.dbcrud",
    version:="0.1-SNAPSHOT",
    scalaVersion:="2.11.2",
    libraryDependencies += "com.typesafe" % "config" % "1.2.1"
  )

  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "log-implicits")

  lazy val root = Project(id = "dbcrud", base = file(".")) aggregate(core, spray)

  lazy val core = Project(id = "dbcrud-core", base = file("core")).settings(commonSettings :_*)

  lazy val spray = Project(id = "dbcrud-spray", base = file("spray")).settings(commonSettings :_*).dependsOn(core)

}