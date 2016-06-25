name := """mosquito-blog"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  //"com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "com.typesafe.play" %% "play-mailer" % "5.0.0-M1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
