import AssemblyKeys._

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList(ps @ _*) if ps.last endsWith "ECLIPSEF.RSA" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "plugin.properties" => MergeStrategy.concat
  case PathList(ps @ _*) if ps.last endsWith "mailcap" => MergeStrategy.first
  case x => old(x)
}
}

name := "movielens-als"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies += ("org.apache.spark" % "spark-mllib_2.10" % "1.1.0").
  exclude("com.esotericsoftware.minlog", "minlog").
  exclude("commons-collections", "commons-collections").
  exclude("commons-logging", "commons-logging").
  exclude("commons-beanutils", "commons-beanutils-core")
