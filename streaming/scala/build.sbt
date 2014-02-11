name := "Tutorial"

scalaVersion := "2.10"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "0.9.0-incubating",
  "org.apache.spark" %% "spark-streaming-twitter" % "0.9.0-incubating"
)
