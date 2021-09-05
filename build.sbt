name := """mosquito-blog"""

version := "1.0.0"

maintainer := "764613916@qq.com"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  jdbc,
  ws,
  filters,
  guice,
  "org.scalatestplus.play" % "scalatestplus-play_2.12" % "3.1.1" % "test",
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "mysql" % "mysql-connector-java" % "8.0.26",
  "com.typesafe.play" %% "play-mailer" % "6.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.0",
  "commons-codec" % "commons-codec" % "1.9",
  "cn.hutool" % "hutool-all" % "5.7.11"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

publishArtifact in (Compile, packageDoc) := false
