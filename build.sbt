name := """play-traffic"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

val squants = "com.squants"  %% "squants"  % "0.5.3"

val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"

val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

val akkaTestKit = "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.1" % "test"
val akkaLog = "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.1" % "test"

val kafkaClient = "org.apache.kafka" % "kafka-clients" % "0.8.2.2"


libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    squants,
    "com.typesafe.play" %% "anorm" % "2.4.0",
    "org.xerial" % "sqlite-jdbc" % "3.8.10.1",
    kafkaClient,
    logging, logback,
    specs2 % Test, scalaTest,
    akkaTestKit, akkaLog
)

libraryDependencies += "org.scalanlp" % "breeze_2.11" % "0.11.2"
libraryDependencies += "org.scalanlp" % "breeze-natives_2.11" % "0.11.2"


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
// resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
// resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
// resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"



// Play provides two styles of routers, getOne expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq("-feature")

