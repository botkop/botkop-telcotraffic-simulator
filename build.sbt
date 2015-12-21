name := """play-traffic"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"


libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    "com.typesafe.play" %% "anorm" % "2.4.0",
    "org.xerial" % "sqlite-jdbc" % "3.8.10.1",
    logging, logback,
    specs2 % Test,
    scalaTest
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
