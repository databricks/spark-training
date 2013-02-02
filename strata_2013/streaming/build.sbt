name := "Tutorial"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "org.spark-project" %% "spark-streaming" % "0.7.0-SNAPSHOT"
)

//mappings in (Compile, packageBin) <+= baseDirectory map { base =>
//   (base / "resources" / "log4j.properties") -> "log4j.properties"
//}
