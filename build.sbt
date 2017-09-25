name := "carkovani"

version := "1.0"

lazy val `carkovani` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs )

libraryDependencies += "org.webjars" % "jquery" % "3.2.1"

libraryDependencies += "org.webjars" % "bootstrap" % "3.3.7"

libraryDependencies += "org.webjars" % "flot" % "0.8.3"

libraryDependencies += "uk.co.panaxiom" %% "play-jongo" % "2.0.0-jongo1.3"

libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"

//libraryDependencies += "com.github.ironfish" % "akka-persistence-mongo-common_2.11" % "0.7.6"

libraryDependencies += "org.mongodb" % "casbah_2.11" % "3.1.0"

libraryDependencies += "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "2.0.3"

libraryDependencies += "com.typesafe.akka" % "akka-persistence_2.11" % "2.5.4"

libraryDependencies += "io.circe" %% "circe-generic" % "0.8.0"

libraryDependencies += "com.google.protobuf" % "protobuf-java"  % "2.5.0"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  