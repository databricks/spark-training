import AssemblyKeys._

assemblySettings

name := "movielens-als"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.0.0" % "provided"
