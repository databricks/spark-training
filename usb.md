# Spark Summit 2014 Training USB #

## Contents ##
 * spark - binary distribution (git hash: GIT_HASH)
     * conf/log4j.properties - WARN used for default level, Snappy warnings silenced
 * data - example and lab data
     * graphx - graphx lab data
     * movielens - MLlib lab data
     * wiki_parquet - SparkSQL lab data
     * examples-data - examples directory from Spark src
     * join - example files for joins
 * sbt - a fresh copy of sbt (v. 0.13.5)
     * sbt-launcher-lib.bash - modified to understand non-default location of bin scripts
     * sbt.bat - modified to understand non-default location of bin scripts [Windows]
     * conf/sbtopts - modified to point to embeded ivy cache
     * conf/sbtconfig.txt - modified to point to embeded ivy cache for [Windows]
     * ivy/cache - a pre-populated cache, pointed to via conf/sbtopts
     * bin - removed and all files moved into sbt's home directory so users can run sbt/sbt similair to working with spark's source code 
 * simple-app - a simple example app to build (based on the Spark quick start docs)
 * streaming
 * machine-learning

## Building and Using the Simple App ##
    cd simple-app
    ../sbt/sbt package
    ../spark/bin/spark-submit \
      --class "SimpleApp" \
      --master local[*] \
      target/scala-2.10/simple-project_2.10-1.0.jar
