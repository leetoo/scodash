name := "carkovani"

version := "1.0"

lazy val `carkovani` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.4",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.14" excludeAll(ExclusionRule("io.netty")),
  "com.typesafe.akka" %% "akka-persistence" % "2.4.4" excludeAll(ExclusionRule("io.netty")),
  "ch.qos.logback" % "logback-classic" % "1.0.9",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
  "net.databinder" %% "unfiltered-filter" % "0.8.4",
  "net.databinder" %% "unfiltered-netty" % "0.8.4",
  "net.databinder" %% "unfiltered-netty-server" % "0.8.4",
  "net.databinder" %% "unfiltered-json4s" % "0.8.4",
  "org.json4s" %% "json4s-ext" % "3.2.9",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "com.google.protobuf" % "protobuf-java"  % "2.5.0"
)

libraryDependencies += "org.webjars" % "jquery" % "3.2.1"

libraryDependencies += "org.webjars" % "bootstrap" % "3.3.7"

libraryDependencies += "org.webjars" % "flot" % "0.8.3"

//libraryDependencies += "uk.co.panaxiom" %% "play-jongo" % "2.0.0-jongo1.3"

libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"

//libraryDependencies += "com.github.ironfish" % "akka-persistence-mongo-common_2.11" % "0.7.6"

//libraryDependencies += "org.mongodb" % "casbah_2.11" % "3.1.0"

//libraryDependencies += "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.16" excludeAll(ExclusionRule("io.netty"))
//
//libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.5.4" excludeAll(ExclusionRule("io.netty"))
//
//libraryDependencies += "net.databinder" %% "unfiltered-filter" % "0.8.4"
//
//libraryDependencies += "net.databinder" %% "unfiltered-netty" % "0.8.4"
//
//libraryDependencies += "net.databinder" %% "unfiltered-netty-server" % "0.8.4"
//
//libraryDependencies += "net.databinder" %% "unfiltered-json4s" % "0.8.4"
//
//libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"


//libraryDependencies += "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "2.0.3"

//libraryDependencies += "com.typesafe.akka" % "akka-persistence_2.11" % "2.5.4"
//
//libraryDependencies += "io.circe" %% "circe-generic" % "0.8.0"
//
//libraryDependencies += "com.google.protobuf" % "protobuf-java"  % "2.5.0"
//
//libraryDependencies += "org.json4s" %% "json4s-ext" % "3.2.9"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  