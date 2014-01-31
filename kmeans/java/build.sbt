name := "wikipedia-kmeans"

version := "0.0"

scalaVersion := "2.10.3"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "0.9.0-incubating"


mainClass in (Compile, run) := Some("WikipediaKMeans")
