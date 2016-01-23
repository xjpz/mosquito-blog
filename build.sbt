name := """mosquito"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"
//crossScalaVersions := Seq("2.10.4", "2.11.6")

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "com.typesafe.slick" % "slick_2.10" % "2.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "mysql" % "mysql-connector-java" % "5.1.27",
//  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.5",
  "commons-codec" % "commons-codec" % "1.9",
  "org.xerial.snappy" % "snappy-java" % "1.1.0",
  "org.apache.commons" % "commons-email" % "1.4",
  "org.apache.commons" % "commons-math3" % "3.3"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

mappings in Universal ++=
    (baseDirectory.value / "res" * "*" get) map
        (x => x -> ("res/" + x.getName))

scalacOptions += "-feature"
