---
layout: global
title: Overview Of The Exercises
navigation:
  weight: 30
  show: true
---

These exercises are divided into sections designed to give a hands-on experience with various software components of the Berkeley Data Analytics Stack (BDAS).
For Spark, we will walk you through using the Spark shell for interactive exploration of data. You have the choice of doing the exercises using Scala or using Python.
For Shark, you will be using SQL in the Shark console to interactively explore the same data.
The advanced modules may use other data sets, such as Twitter data for Spark Streaming.

## Cluster Details
Your cluster contains 6 m1.xlarge Amazon EC2 nodes.
One of these 6 nodes is the master node, responsible for scheduling tasks as well as maintaining the HDFS metadata (a.k.a. HDFS name node).
The other 5 are the slave nodes on which tasks are actually executed.
You will mainly interact with the master node.
If you haven't already, let's ssh onto the master node (see instructions above).

Once you've used SSH to log into the master, run the `ls` command and you will see a number of directories.
Some of the more important ones are listed below:

- Templates for exercises:
   - `streaming`: Standalone program for Spark Streaming exercises
   - `java-app-template`: Template for standalone Spark programs written in Java
   - `scala-app-template`: Template for standalone Spark programs written in Scala
   - `shark`: Shark installation
   - `spark`: Spark installation
   - `machine-learning`: Root directory for machine learning module
   - `blinkdb`: BlinkDB installation

- Useful scripts/documentation:
   - `spark-ec2`: Suite of scripts to manage Spark on EC2
   - `training`: Documentation and code used for training exercises

- Infrastructure:
   - `ephemeral-hdfs`: Hadoop installation
   - `scala-2.10.3`: Scala installation
   - `hive`: Hive installation

You can find a list of your 5 slave nodes in spark-ec2/slaves:

    cat spark-ec2/slaves

For stand-alone Spark programs, you will have to know the Spark cluster URL. You can find that in spark-ec2/cluster-url:

    cat spark-ec2/cluster-url

## Dataset For Exploration
Among other datasets, your HDFS cluster should come preloaded with 20GB of Wikipedia traffic statistics data obtained from http://aws.amazon.com/datasets/4182 .
To make the analysis feasible (within the short timeframe of the exercise), we took three days worth of data (May 5 to May 7, 2009; roughly 20G and 329 million entries).
You can list the files:

<pre class="prettyprint lang-bsh">
ephemeral-hdfs/bin/hadoop fs -ls /wiki/pagecounts
</pre>

There are 74 files (2 of which are intentionally left empty).

The data are partitioned by date and time.
Each file contains traffic statistics for all pages in a specific hour.
Let's take a look at the file:

<pre class="prettyprint lang-bsh">
ephemeral-hdfs/bin/hadoop fs -cat /wiki/pagecounts/part-00148 | less
</pre>

The first few lines of the file are copied here:

<pre class="prettyprint lang-bsh">
20090507-040000 aa ?page=http://www.stockphotosharing.com/Themes/Images/users_raw/id.txt 3 39267
20090507-040000 aa Main_Page 7 51309
20090507-040000 aa Special:Boardvote 1 11631
20090507-040000 aa Special:Imagelist 1 931
</pre>

Each line, delimited by a space, contains stats for one page.
The schema is:

`<date_time> <project_code> <page_title> <num_hits> <page_size>`

The `<date_time>` field specifies a date in the YYYYMMDD format (year, month, day) followed by a hyphen and then the hour in the HHmmSS format (hour, minute, second).
There is no information in mmSS.
The `<project_code>` field contains information about the language of the pages.
For example, project code "en" indicates an English page.
The `<page_title>` field gives the title of the Wikipedia page.
The `<num_hits>` field gives the number of page views in the hour-long time slot starting at `<data_time>`.
The `<page_size>` field gives the size in bytes of the Wikipedia page.

To quit `less`, stop viewing the file, and return to the command line, press `q`.

