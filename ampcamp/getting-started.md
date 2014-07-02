---
layout: global
title: Getting Started & Known Issues
categories: [module]
navigation:
  weight: 20
  show: true
---

# Getting Started With Your USB Stick

If you are attending Spark Training in person, you should have a USB stick containing training material. 
If you do not have the USB stick, you can [download a zip file with the contents](http://bit.ly/spark-training-2).

## Initial Test 
After loading the USB key, you should perform the following steps:

<p class="alert alert-warn">
    <i class="icon-info-sign">    </i>
    If you are a windows user, please consider using Powershell, or otherwise replace `cd` with `dir`
    and any forward slash (`/`) with a backwards slash (`\`) when navigating directories
    </p>

1. If your USB key  mounted as `NO NAME`, rename it to `SparkTraining` or any other name without a space. 
If you skip this step, you will see your sbt builds fail. 

    <p class="alert alert-warn">
    <i class="icon-info-sign">    </i>
    For the rest of this training session, all command-line instructions assume that the terminal is in the <code>SparkTraining</code> directory.
    </p>


2. Check your setup by building a toy application. To do this, run the following commands:

   ~~~
cd simple-app
../sbt/sbt package
../spark/bin/spark-submit \
  --class "SimpleApp" \
  --master local[*] \
  target/scala-2.10/simple-project_2.10-1.0.jar
   ~~~

    <p class="alert alert-warn">
    <i class="icon-info-sign">    </i>
    If you do not have a USB key or cannot get the simple app to build, then ask a TA.
    </p>

## Additional Download
Please download the following 

## USB Contents

Throughout the exercises, you should remain at the root of the USB stick if you want to 
copy and paste the commands as written in the docs.  
  
You'll find the following contents in the USB stick (taken from the README):

 * **spark** - binary distribution @ Spark 1.0.1-SNAPSHOT (git hash: e6c90583b4d68ddce3f9dd2c76c8bbad593ad077)
     * conf/log4j.properties - WARN used for default level, Snappy warnings silenced
 * **data** - example and lab data
     * graphx - graphx lab data
     * movielens - MLlib lab data
     * wiki_parquet - SparkSQL lab data
     * examples-data - examples directory from Spark src
     * join - example files for joins
 * **sbt** - a fresh copy of sbt (v. 0.13.5)
     * sbt-launcher-lib.bash - modified to understand non-default location of bin scripts
     * sbt.bat - modified to understand non-default location of bin scripts [Windows]
     * conf/sbtopts - modified to point to embeded ivy cache
     * conf/sbtconfig.txt - modified to point to embeded ivy cache for [Windows]
     * ivy/cache - a pre-populated cache, pointed to via conf/sbtopts
     * bin - removed and all files moved into sbt's home directory so users can run sbt/sbt similair to working with spark's source code 
 * **simple-app** - a simple example app to build (based on the Spark quick start docs)


