# SparkCamp Training USB #

## Contents ##
 * spark - binary distribution
     * Spark Version: SPARK_VERSION
     * Git Hash: GIT_HASH
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
 * streaming - a simple streaming application used for the SparkCamp streaming labs
 * machine-learning - the simple application used for the SparkCamp machine learning labs
 * website - the website contents for SparkCamp labs.

## Building and Using the Simple App ##
    % cd simple-app
    % ../sbt/sbt package
    % ../spark/bin/spark-submit \
      --class "SimpleApp" \
      --master local[*] \
      target/scala-2.10/simple-project_2.10-1.0.jar

## Viewing the website for more exercises
    % cd website
    To view the site, run jekyll - see the README.md in the website directory for more details on installing.
    % jekyll serve
    Now, view http://0.0.0.0:4000/ in your browser.