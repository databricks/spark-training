name := "wikipedia-kmeans"

version := "0.0"

scalaVersion := "2.9.3"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "0.8.0-incubating"


mainClass in (Compile, run) := Some("WikipediaKMeans")
