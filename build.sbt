name := "carkovani"

version := "1.0"

lazy val `carkovani` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs )

libraryDependencies += "org.webjars" % "bootstrap" % "3.3.7"

libraryDependencies += "org.webjars" % "flot" % "0.8.3"



unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  