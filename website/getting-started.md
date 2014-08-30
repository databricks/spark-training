---
layout: global
title: Getting Started
navigation:
  weight: 20
  show: true
skip-chapter-toc: true  
---

# Getting Started With Your USB Stick

If you are attending Spark Training in person, you should have a USB stick containing training material. 
If you do not have the USB stick,  <s>you can download a zip file with the contents</s> 
 __This link is removed temporarily to avoid attendees accidentally kicking off a download. 
If you need the usb files to follow along from a remote location, please email__ *training-feedback@databricks.com*

## Quick Start 
After loading the USB key, you should perform the following steps:

<p class="alert alert-warn">
    <i class="icon-info-sign">    </i>
    If you are a windows user, please consider using Powershell, or otherwise replace `cd` with `dir`
    and any forward slash (`/`) with a backwards slash (`\`) when navigating directories
    </p>

1. If your USB key  mounted as `NO NAME`, rename it to `SparkTraining` or any other name without a space. 
If you skip this step, you will see your sbt builds fail. 

2. Change directories in to where your USB stick is mounted

    <p class="alert alert-warn">
    <i class="icon-info-sign">    </i>
    Throughout the training exercises, all command-line instructions of the form 'usb/$' refer to a 
    terminal that is in the usb directory. Moreover, [usb root directory] refers to the full path to this directory.
    </p>


3. Check your setup by building a toy application. To do this, run the following commands, 
and confirm the results match. If they do, you just built a simple spark application - go look at the
source code to get a feel for what happened.

   ~~~
usb/$ cd simple-app
$ ../sbt/sbt package
$ ../spark/bin/spark-submit --class "SimpleApp" --master local[*] target/scala-2.10/simple-project_2.10-1.0.jar
2014-06-27 15:40:44.788 java[15324:1607] Unable to load realm info from SCDynamicStore
Lines with a: 73, Lines with b: 35
   ~~~

    <p class="alert alert-warn">
    <i class="icon-info-sign">    </i>
    If you do not have a USB key or cannot get the simple app to build, then ask a TA.
    </p>

## USB Contents

You'll find the following contents in the USB stick (this info is taken from the README):

 * **spark** - binary distribution
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
 * **streaming** - project template for Spark Streaming examples
 * **machine-learning** - project template for Machine Learning examples
 * **website** - documentation for the examples
