scalaVersion := "2.13.15"
name := "Voting App"
organization := "ch.epfl.scala"
version := "1.0"

resolvers += "Akka Repository" at "https://repo.akka.io/releases/"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "com.typesafe.play" %% "play" % "2.9.5",
  "com.typesafe" % "config" % "1.4.3",
  "ch.qos.logback" % "logback-core" % "1.5.12",
  "ch.qos.logback" % "logback-classic" % "1.5.12",
  "com.typesafe.slick" %% "slick" % "3.5.2",
  "com.typesafe.slick" %% "slick-codegen" % "3.5.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.2",
  "org.postgresql" % "postgresql" % "42.7.4",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)

resolvers += "Akka Repository" at "https://repo.akka.io/releases/"

fork in run := true