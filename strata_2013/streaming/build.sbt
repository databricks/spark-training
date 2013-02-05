name := "Tutorial"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "org.spark-project" %% "spark-streaming" % "0.7.0-SNAPSHOT",
  "org.twitter4j" % "twitter4j-core" % "3.0.2",
  "org.twitter4j" % "twitter4j-stream" % "3.0.2"
)
