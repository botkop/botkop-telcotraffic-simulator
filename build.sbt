name := """play-traffic"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"
val akkaVersion = "2.4.1"

libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    specs2 % Test,

    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
    "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion % "test",
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion,

    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3",

    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
    "org.scalatestplus" %% "play" % "1.4.0" % "test",


    "com.squants"  %% "squants"  % "0.5.3",
    "com.typesafe.play" %% "anorm" % "2.4.0",
    "org.xerial" % "sqlite-jdbc" % "3.8.10.1",
    "org.apache.kafka" % "kafka-clients" % "0.8.2.2",
    "org.scalanlp" % "breeze_2.11" % "0.11.2",
    "org.scalanlp" % "breeze-natives_2.11" % "0.11.2"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, getOne expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq("-feature")

// durning development the database is in the dist/data folder, so it will be included with production distributions
// in production deployment it will be in the data/ folder.
PlayKeys.devSettings += ("db.default.url", "jdbc:sqlite:dist/data/traffic.db")

