name := "dbcrud"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.4.2" % "test",
  "com.h2database" % "h2" % "1.4.183" % "test"
)