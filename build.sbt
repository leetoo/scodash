name := "carkovani"

version := "1.0"

lazy val `carkovani` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc, guice, ws, javaWs)

val akkaVersion = "2.5.11"

val json4sVersion = "3.2.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.1.0",
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion excludeAll(ExclusionRule("io.netty")),
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion excludeAll(ExclusionRule("io.netty")),
  "ch.qos.logback" % "logback-classic" % "1.0.9",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
//  "net.databinder" %% "unfiltered-filter" % "0.8.4",
//  "net.databinder" %% "unfiltered-netty" % "0.8.4",
//  "net.databinder" %% "unfiltered-netty-server" % "0.8.4",
  "net.databinder" %% "unfiltered-json4s" % "0.8.4",
//  "org.json4s" %% "json4s-ext" % json4sVersion,
//  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.3.0",
  "org.postgresql" % "postgresql" % "9.4.1208",
  "com.google.protobuf" % "protobuf-java"  % "2.5.0",
  "org.apache.commons" % "commons-lang3" % "3.7",
  "com.sendgrid" % "sendgrid-java" % "4.0.1"
)

libraryDependencies += "org.webjars" % "jquery" % "3.2.1"

libraryDependencies += "org.webjars" % "bootstrap" % "3.3.7"

libraryDependencies += "org.webjars" % "flot" % "0.8.3"


libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  