name := "Tutorial"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "0.9.0-incubating",
  "org.apache.spark" %% "spark-streaming-twitter" % "0.9.0-incubating"
)

mainClass in (Compile, run) := Some("Tutorial")
