---
layout: global
title: AMP Camp 2012 Exercises
---
# Introduction to AMP Camp 2012 Exercises

The following series of exercises will walk you through the process of setting up a 4-machine cluster on EC2 running [Spark](http://spark-project.org), [Shark](http://shark.cs.berkeley.edu) and [Mesos](http://mesos-project.org),
then loading and analyzing a real wikipedia dataset using your cluster.
We will begin with simple interactive analysis techniques at the command-line, and progress to writing standalone programs, and then onto more advanced machine learning algorithms.

# Launching a Spark/Shark Cluster on EC2

This section will walk you through the process of launching a small cluster using your own Amazon EC2 account and our scripts and AMI (New to AMIs? See this [intro to AMIs](https://aws.amazon.com/amis/)).

## Pre-requisites

The cluster setup script we'll use below requires Python 2.x and has been tested to work on Linux or OS X.
We will use the [Bash shell](http://www.gnu.org/software/bash/manual/bashref.html) in our examples below.
If you are using Windows, consider installing [Cygwin](http://www.cygwin.com/) (note that we have not tested this, hence providing debug support would be hard).

## Setting up EC2 keys

Make sure you have an Amazon EC2 account.
Set the environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` to your Amazon EC2 access key ID and secret access key.
These can be obtained from the [AWS homepage](http://aws.amazon.com/) by clicking `Account > Security Credentials > Access Credentials`:

![Downloading AWS access keys](img/aws-accesskey.png)

    export AWS_ACCESS_KEY_ID=<ACCESS_KEY_ID>
    export AWS_SECRET_ACCESS_KEY=<SECRET_ACCESS_KEY>

Create an Amazon EC2 key pair for yourself.
This can be done by logging into your Amazon Web Services account through the [AWS console](http://aws.amazon.com/console/), selecting `EC2` from the `Services` menu,  selecting `Key Pairs` on the left sidebar, and creating and downloading a key:

![Downloading an EC2 Keypair](img/aws-keypair.png)



Make sure that you set the permissions for the private key file to `600` (i.e. only you can read and write it) so that `ssh` will work (commands to do this are provided farther below).

<div class="alert alert-info">
<i class="icon-info-sign"> 	</i>
The AMI we are using for this exercise is only available in the `us-east` region.
So make sure you create a key-pair in that region!
</div>

## Getting the scripts to launch EC2 cluster

Check out the launch scripts by cloning the github repository.

    git clone git://github.com/amplab/ampcamp.git

You can also obtain them by downloading the zip file at `http://github.com/amplab/ampcamp/zipball/master`

## Launching the cluster
Launch the cluster by running the following command.
This script will launch a cluster, create a HDFS cluster and configure Mesos, Spark, and Shark.
Finally, it will copy the datasets used in the exercises from S3 to the HDFS cluster.
_This can take around 15-20 mins._

    cd ampcamp
    ./spark-ec2 -i <key_file> -k <name_of_key_pair> --copy launch ampcamp

Where `<name_of_key_pair>` is the name of your EC2 key pair (that you gave it when you created it), `<key_file>`is the private key file for your key pair.

For example, if you created a key pair named `ampcamp-key` and the private key (`<key_file>`) is in your home directory and is called `ampcamp.pem`, then the command would be

    ./spark-ec2 -i ~/ampcamp.pem -k ampcamp-key --copy launch ampcamp

The following are some errors that you may encounter, and other frequently asked questions.
Once you are able to successfully launch the cluster, continue to step 4.

__Question: I got the following permission error when I ran the above command. Help!__

<pre class="nocode">
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@ WARNING: UNPROTECTED PRIVATE KEY FILE! @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
Permissions 0644 for ‘../ampcamp.pem' are too open.
It is recommended that your private key files are NOT accessible by others.
This private key will be ignored.
bad permissions: ignore key: ../ampcamp.pem
Permission denied (publickey).
</pre>

__Answer:__ Run the next two commands.

    chmod 600 ../ampcamp.pem
    ./spark-ec2 -i <key_file> -k <name_of_key_pair> --copy --resume launch ampcamp

__Question: I got the following error when I ran the above command. Help!__

<pre class="nocode">
￼"Your requested instance type (m2.xlarge) is not supported in your requested Availability Zone (us-east-1b).  Please retry your request by not specifying an Availability Zone or choosing us-east-1d, us-east-1c, us-east-1a, us-east-1e."
</pre>

__Answer:__ Add the `-z` flag to your command line arguments to use an availability zone other than `us-east-1b`.
You can set the value of that flag to "none", as in the following example command, which tells the script to pick a random availability zone.
It may randomly pick an availability zone that doesn't support this instance size (such as `us-east-1b`), so you may need to try this command a few times to get it to work.

    ./spark-ec2 -i <key_file> -k <name_of_key_pair> -z none --copy launch ampcamp

__Question: I got the following error when I ran the above command. Help!__

<pre class="nocode">
12/08/21 16:50:45 INFO tools.DistCp: destPath=hdfs://ip-10-42-151-150.ec2.internal:9000/wiki/pagecounts
java.lang.IllegalArgumentException: Invalid hostname in URI
s3n://AKIAJIFGXUZ4MDJNYCGQ:COWo3AxVhjyu43Ug5kDvTnO/V3wQloBRIEOYEQgG@ak-ampcamp/wikistats_20090505-07
</pre>

__Answer:__ The data copy from S3 to your EC2 cluster has failed. Do the following steps:

1. Login to the master node by running

   ~~~
   ./spark-ec2 -i <key_file> -k <key_pair> login ampcamp
   ~~~

2. Open the HDFS config file at `/root/ephemeral-hdfs/conf/core-site.xml` and
   copy your AWS access key and secret key into the respective fields.

3. Restart HDFS

   ~~~
   /root/ephemeral-hdfs/bin/stop-dfs.sh
   /root/ephemeral-hdfs/bin/start-dfs.sh
   ~~~

4. Delete the directory the data was supposed to be copied to

   ~~~
   /root/ephemeral-hdfs/bin/hadoop fs -rmr /wiki
   ~~~

5. Logout and run the following command to retry copying data from S3

   ~~~
   ./spark-ec2 -i <key_file> -k <key_pair> copy-data ampcamp
   ~~~



__Question: Can I specify the instances types while creating the cluster?__

__Answer:__ These exercises have been designed to work with at least 3 slave
machines using instances of type __m2.xlarge__.
You can also launch the cluster with different [instance types](http://aws.amazon.com/ec2/instance-types/).
However, you should ensure two things:

1. __Correct number of slaves:__ Make sure that the total memory in the slaves is about 54GB as the exercises are designed accordingly.
   So if you are using `m1.large` instances (which have 7.5 GB memory), then you should launch a cluster with at least 8 slaves.

   You can specify the instance type in the above command by setting the flag `-t <instance_type>` .
   Similarly, you can specify the number of slaves by setting the flag `-s <number of slaves>`.
   For example, to launching a cluster with 8 `m1.large` slaves, use

   ~~~
   ./spark-ec2 -i <key_file> -k <name_of_key_pair> -t m1.large -s 8 --copy launch ampcamp
   ~~~

2. __Correct java heap setting for Spark:__ Make sure to change the `SPARK_MEM` variable in
   `/root/spark/conf/spark-env.sh` and `/root/shark/conf/shark-env.sh` on all of the instances to match the amount of memory available in the instance type you use.
   This is typically set it to the total amount of memory of the instance minus 1 GB for the OS (that is, for `m1.large` with 7.5GB memory, set `SPARK_MEM=6g`).
   There is a easy way to change this configuration on all the instances.
   First, change this file in the master.
   Then run

   ~~~
   /root/mesos-ec2/copy-dir /root/spark/conf/ .
   ~~~

   to copy the configuration directory to all slaves.

__Information:__ Sometimes the EC2 instances don't initialize within the standard waiting time of 120 seconds.
If that happens you, will ssh errors (or check in the Amazon web console).
In this case, try increasing the waiting to 4 minutes using the `-w 240` option.

## Post-launch steps
Your cluster should be ready to use.
You can find the master hostname (`<master_node_hostname>` in the instructions below) by running

    ./spark-ec2 -i -k get-master ampcamp

At this point, it would be helpful to open a text file and copy `<master_node_hostname>` there.
In a later exercise, you will want to have `<master_node_hostname>` ready at hand without having to scroll through your terminal history.

## Terminating the cluster (Not yet! Only after you do the exercises below.)
__After you are done with your exercises__, you can terminate the cluster by running

    ./spark-ec2 -i <key_file> -k <key_pair> destroy ampcamp

# Logging into the Cluster

Log into your cluster via `ssh -i <key_file> -l root@<master_node_hostname>`

   __Question: I got the following permission error when I ran the above command. Help!__

   <pre class="nocode">
   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
   @             WARNING: UNPROTECTED PRIVATE KEY FILE!              @
   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
   Permissions 0644 for '../ampcamp.pem' are too open.
   It is recommended that your private key files are NOT accessible by others.
   This private key will be ignored.
   bad permissions: ignore key: ../ampcamp.pem
   Permission denied (publickey).
   </pre>

__Answer:__ Run the next two commands, then try to log in again:

    chmod 600 ../ampcamp.pem
    ./spark-ec2 -i <key_file> -k <name_of_key_pair> --copy --resume launch ampcamp

# Intro to Scala

This short exercise will teach you the basics of Scala and introduce you to functional programming with collections.
Do as much as you feel you need.

If you're comfortable with Python, feel free to skip ahead to the next section.

The exercise is based on a great and fast tutorial, [First Steps to Scala](http://www.artima.com/scalazine/articles/steps.html).
Just reading that and trying the examples at the console might be enough!
Open this in a separate browser window and look through it if you need help.
We will use only sections 1-9.

2. Launch the Scala console by typing `scala`.

3. Declare a list of integers, `numbers`, as `val numbers = List(1, 2, 5, 4, 7, 3)`

4. Declare a function, `cube`, that computes the cube (third power) of a number.
   See steps 2-4 of First Steps to Scala.
   Then apply the function on the list using `map`.

5. Then also try writing the function inline in a `map` call, using closure notation.

6. Define a `factorial` function that computes n! = 1 * 2 * ... * n given input n.
   You can use either a loop or recursion (see steps 5-7 of First Steps to Scala).
   Then compute the sum of factorials in `numbers`.

7. As a final exercise, implement a program that counts how many times a particular word occurs in a text file.
   You can load a text file as an array of lines as shown below:

   ~~~
   import scala.io.Source
   val lines = Source.fromFile("/root/mesos/README").getLines.toArray
   ~~~

   Then use functional methods to count the number of occurrences (steps 8-9 of First Steps to Scala).
   Also add a flag to your program to perform case-insensitive search.

# Spark Shell and Shark

In this section, we will walk you through using the Spark shell and/or the Shark console to interactively explore data.
We will be working with Wikipedia traffic statistics data obtained from http://aws.amazon.com/datasets/4182

To make the analysis feasible (within the short timeframe of the exercise), AMP Camp organizers took three days worth of data (May 5 to May 7, 2009; roughly 20G and 329 million entries) and preloaded it into an instance of HDFS running on your cluster.
We'll take a look at it in a minute.

## Data Set and Cluster
If you have launched the cluster with the default script above (no custom instance type and/or number of slaves), your cluster should contain 4 m2.xlarge Amazon EC2 nodes:

![Running EC2 instances in AWS Management Console](img/aws-runninginstances.png)

One of these 4 nodes is the master node, responsible for scheduling tasks as well as maintaining the HDFS metadata (a.k.a. HDFS name node).
The other 3 are the slave nodes on which tasks are actually executed.
You will mainly interact with the master node.
If you haven't already, let's ssh onto the master node:

    ssh -i <key_file> -l root <master_node_hostname>

On the cluster, run the `ls` command and you will see a number of directories.
Some of the more important ones are listed below:

- `ephemeral-hdfs:` Hadoop installation.
- `hive:` Hive installation
- `java-app-template:` Some stand-alone Spark programs in java 4. mesos: Mesos installation
- `mesos-ec2:` A suite of scripts to manage Mesos on EC2
- `scala-2.9.1.final:` Scala installation
- `scala-app-template:` Some stand-alone Spark programs in scala 8. spark: Spark installation
- `shark:` Shark installation

You can find a list of your 3 slave nodes in mesos-ec2/slaves:

    cat mesos-ec2/slaves

Your HDFS cluster should come preloaded with 20GB of Wikipedia traffic statistics data for May 5 to May 7, 2009.
You can list the files:

    ephemeral-hdfs/bin/hadoop fs -ls /wiki/pagecounts

There are 74 files (2 of which are intentionally left empty).

The data are partitioned by date and time.
Each file contains traffic statistics for all pages in a specific hour.
Let's take a look at the file:

    ephemeral-hdfs/bin/hadoop fs -cat /wiki/pagecounts/part-00148 | less

The first few lines of the file are copied here:

<pre class="nocode">
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

## Data Exploration Using Spark

Let's now use Spark to do some order statistics on the data set.
First, launch the Spark shell:

    /root/spark/spark-shell

Wait for the Scala prompt to appear.

1. Warm up by creating an RDD (Resilient Distributed Dataset) named `pagecounts` from the input files.
   In the Spark shell, the SparkContext is already created for you as variable `sc`.

   ~~~
   scala> sc
   res: spark.SparkContext = spark.SparkContext@470d1f30

   scala> val pagecounts = sc.textFile("/wiki/pagecounts")
   12/08/17 23:35:14 INFO mapred.FileInputFormat: Total input paths to process : 74
   pagecounts: spark.RDD[String] = spark.MappedRDD@34da7b85
   ~~~

2. Let's take a peek at the data. You can use the take operation of an RDD to get the first K records. Here, K = 10.

   ~~~
   scala> pagecounts.take(10)
   ......
   res: Array[String] = Array(20090505-000000 aa.b ?71G4Bo1cAdWyg 1 14463, 20090505-000000 aa.b Special:Statistics 1 840, 20090505-000000 aa.b Special:Whatlinkshere/MediaWiki:Returnto 1 1019, 20090505-000000 aa.b Wikibooks:About 1 15719, 20090505-000000 aa ?14mFX1ildVnBc 1 13205, 20090505-000000 aa ?53A%2FuYP3FfnKM 1 13207, 20090505-000000 aa ?93HqrnFc%2EiqRU 1 13199, 20090505-000000 aa ?95iZ%2Fjuimv31g 1 13201, 20090505-000000 aa File:Wikinews-logo.svg 1 8357, 20090505-000000 aa Main_Page 2 9980, 20090505-000000 aa User:TottyBot 1 7998, 20090505-000000 aa Wikipedia:Community_Portal 1 9644, 20090505-000000 aa main_page 1 928, 20090505-000000 ab.d %D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F: \
   Recentchangeslinked/%D0%A3%D1%87%D0%B0%D1%81%D1%82%D0%BD%D0%B8%D0%BA: \
   H%C3%A9g%C3%A9sippe_...
   ~~~

   Unfortunately this is not very readable because take returns an array and Scala simply prints the array with each element separated by a comma.
   We can make it prettier by traversing the array to print each record on its own line.

   ~~~
   scala> pagecounts.take(10).foreach(println)
   ......
   20090505-000000 aa.b ?71G4Bo1cAdWyg 1 14463
   20090505-000000 aa.b Special:Statistics 1 840
   20090505-000000 aa.b Special:Whatlinkshere/MediaWiki:Returnto 1 1019
   20090505-000000 aa.b Wikibooks:About 1 15719
   20090505-000000 aa ?14mFX1ildVnBc 1 13205
   20090505-000000 aa ?53A%2FuYP3FfnKM 1 13207
   20090505-000000 aa ?93HqrnFc%2EiqRU 1 13199
   20090505-000000 aa ?95iZ%2Fjuimv31g 1 13201
   20090505-000000 aa File:Wikinews-logo.svg 1 8357
   20090505-000000 aa Main_Page 2 9980
   ~~~

2. Let's see how many records in total are in this data set (this command will take a while, so read ahead while it is running).

   ~~~
   scala> pagecounts.count
   ~~~

   This should launch 177 Spark tasks on the Mesos cluster.
   If you look closely at the terminal, the console log is pretty chatty and tells you the progress of the tasks.
   Because we are reading 20G of data from HDFS, this task is I/O bound and can take a while to scan through all the data (2 - 3 mins).
￼
   While it's running, you can open the Mesos web console to see the progress.
   To do this, open your favorite browser, and type in the following URL.
   Recall that during the Cluster Setup section, you copied `<master_node_hostname>` to a text file for easy access.

   `http://<master_node_hostname>:8080`

   ![Mesos web UI](img/mesos-webui640.png)

   When your count does finish running, it should return the following result: `res2: Long = 329641466`

4.  Recall from above when we described the format of the data set, that the second field is the "project code" and contains information about the language of the pages.
    For example, the project code "en" indicates an English page.
    Let's derive an RDD containing only English pages from `pagecounts`.
    This can be done by applying a filter function to `pagecounts`.
    For each record, we can split it by the field delimiter (i.e. a space) and get the second field-– and then compare it with the string "en".

    To avoid reading from disks each time we perform any operations on the RDD, we also __cache the RDD into memory__.
    This is where Spark really starts to to shine.

    ~~~
    scala> val enPages = pagecounts.filter(_.split(" ")(1) == "en").cache
    enPages: spark.RDD[String] = spark.FilteredRDD@8262adc
    ~~~

    When you type this command into the Spark shell, Spark defines the RDD, but because of lazy evaluation, no computation is done yet.
    Next time any action is invoked on `enPages`, Spark will cache the data set in memory across the 3 slaves in your cluster.

5. How many records are there for English pages?

   ~~~
   scala> enPages.count
   ......
   res: Long = 122352588
   ~~~

   The first time this command is run, similar to the last count we did, it will take 2 - 3 minutes while Spark scans through the entire data set on disk.
   __But since enPages was marked as "cached" in the previous step, if you run count on the same RDD again, it should return an order of magnitude faster__.

   If you examine the console log closely, you will see lines like this, indicating some data was added to the cache:

   `12/08/18 00:30:55 INFO spark.CacheTrackerActor: Cache entry added: (2, 0) on ip-10-92-69-90.ec2.internal (size added: 16.0B, available: 4.2GB)`

6. Let's try something fancier.
   Generate a histogram of total page views on Wikipedia English pages for May to May 7, 2009.
   The high level idea of what we'll be doing is as follows.
   First, we generate a key value pair for each line; the key is the date (the first eight characters of the first field), and the value is the number of pageviews for that date (the fourth field).

   ~~~
   scala> val enTuples = enPages.map(line => line.split(" "))
   enTuples: spark.RDD[Array[java.lang.String]] = spark.MappedRDD@5a62a404

   scala> val enKeyValuePairs = enTuples.map(line => (line(0).substring(0, 8), line(3).toInt))
   enKeyValuePairs: spark.RDD[(java.lang.String, Int)] = spark.MappedRDD@142eda55
   ~~~

   Next, we shuffle the data and group all values of the same key together.
   Finally we sum up the values for each key.
   There is a convenient method called `reduceByKey` in Spark for exactly this pattern.
   Note that the second argument to `reduceByKey` determines the number of reducers to use.
   By default, Spark assumes that the reduce function is algebraic and applies combiners on the mapper side.
   Since we know there is a very limited number of keys in this case (because there are only 3 unique dates in our data set), let's use only one reducer.

   ~~~
   scala> enKeyValuePairs.reduceByKey(_+_, 1).collect
   ......
   res: Array[(java.lang.String, Int)] = Array((20090506,204190442), (20090507,202617618), (20090505,207698578))
   ~~~

   The `collect` method at the end converts the result from an RDD to a Scala array.
   Note that when we don't specify a name for the result of a command (e.g. `val enTuples` above), a variable with name `res` is automatically created.

   We can combine the previous three commands into one:

   ~~~
   scala> enPages.map(line => line.split(" ")).map(line => (line(0).substring(0, 8), line(3).toInt)).reduceByKey(_+_, 1).collect
   ......
   res: Array[(java.lang.String, Int)] = Array((20090506,204190442), (20090507,202617618), (20090505,207698578))
   ~~~

7. Suppose we want to find the top 50 most-viewed pages during these three days.
   Conceptually, this task is very similar to the previous query.
   But, given the large number of pages (23 million distinct page names), the new task is very expensive.
   We are doing a super expensive group-by with a lot of network shuffling of data.

   Also, we would like to use the distributed sorting in Spark rather than loading all 23 million results to the master node to take the top K.
   Spark provides a `sortByKey` method for an RDD, but the value needs to come before the key in a tuple when running this method.
   So make sure you swap the key and value in each tuple that results from `reduceByKey` before running `sortByKey`.

   To recap, first we find the fields in each line of data (`map(l => l.split(" "))`).
   Next, we extract the fields for page name and number of page views (`map(l => (l(2), l(3).toInt))`).
   We reduce by key again, this time with 40 reducers (`reduceByKey(_+_, 40)`).
   Finally, we swap the key and values (`map(x => (x._2, x._1))`), sort by key in descending order (the `false` argument specifies descending order and `true` would specify ascending), and return the top 50 results (`take`). The full command is:

   ~~~
   scala> enPages.map(l => l.split(" ")).map(l => (l(2), l(3).toInt)).reduceByKey(_+_, 40).map(x => (x._2, x._1)).sortByKey(false).take(50)
   ~~~

   There is no hard and fast way to calculate the optimal number of reducers for a given problem; you will
   build up intuition over time by experimenting with different values.

   To leave the Spark shell, type `exit` at the prompt.

## Data Exploration Using Shark

Now that we've had fun with Scala, let's try some SQL in Shark.
First, launch the Shark console:

    cd /root/
    /root/shark/bin/shark-withinfo

1. Similar to [2Apache Hive, Shark can query external tables (tables that are not created in Shark).
   Before you do any querying, you will need to tell Shark where the data is and define its schema.

   ~~~
   shark> CREATE EXTERNAL TABLE wikistats (dt string, project_code string, page_name string, page_views int, bytes int) row format delimited fields terminated by ' ' location '/wiki/pagecounts';
   ~~~

   FAQ: If you see the following errors, don’t worry. Things are still working under the hood.

   <pre class="nocode">
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.core.resources" but it cannot be resolved.
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.core.runtime" but it cannot be resolved.
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.text" but it cannot be resolved.
   </pre>

1. Let's create a table containing all English records and cache it in the cluster's memory.

   ~~~
   shark> create table wikistats_cached as select * from wikistats where project_code="en";
   ~~~

3. Do a simple count to get the number of English records. If you have some familiarity working with databases, note that "`count(1)`" is the syntax in Hive for performing a count(*) operation. The Hive syntax is described at https://cwiki.apache.org/confluence/display/Hive/GettingStarted

   ~~~
   shark> select count(1) from wikistats_cached;
   ......
   122352588
   Time taken: 7.632 seconds
   12/08/18 21:23:13 INFO CliDriver: Time taken: 7.632 seconds
   ~~~

4. Output the total traffic to Wikipedia English pages for each hour between May 7 and May 9, with one line per hour.

   ~~~
   shark> select dt, sum(page_views) views from wikistats_cached group by dt;
   ~~~

5. In the Spark section, we ran a very expensive query to compute the top 50 pages. It is fairly simple to d the same thing in SQL.

   There are two steps we can take to make the query run faster. (1) In the first command below, we turn off map-side aggregation (a.k.a. combiners). Map-side aggregation doesn't improve run-time since each page appears only once in each partition, so we are building a giant hash table on each partition for nothing.
(2) In the second command below, we increase the number of reducers used in this query to 50. Note that the
default number of reducers, which we have been using so far in this section, is 1.

   Also note that the "50" in the second command refers to the number of reducers while the "50" in the third command refers to the number of Wikipedia pages to return.

   ~~~
   shark> set hive.map.aggr=false;
   shark> set mapred.reduce.tasks=50;
   shark> select page_name, sum(page_views) views from wikistats_cached group by page_name order by views desc limit 50;
   ~~~

   __Remember to turn hive.map.aggr back on after the query.__

6. With all the warm up, now it is your turn to write queries. Write Hive QL queries to answer the following questions:

- Count the number of distinct date/times for English pages

  <div class="accordion">
  <div class="accordion-group">
   <div class="accordion-heading">
     <a class="accordion-toggle" data-toggle="collapse" href="#collapse-sql1">
       <i class="icon-ok-sign"> </i>
       View Solution
     </a>
   </div>
   <div id="collapse-sql1" class="accordion-body collapse">
     <div class="accordion-inner" markdown="1">
      select count(distinct dt) from wikistats_cached;
     </div>
   </div>
   </div>
   </div>

- How many hits are there on pages with Berkeley in the title throughout the entire period?

  <div class="accordion">
  <div class="accordion-group">
   <div class="accordion-heading">
     <a class="accordion-toggle" data-toggle="collapse" href="#collapse-sql2">
       <i class="icon-ok-sign"> </i>
       View Solution
     </a>
   </div>
   <div id="collapse-sql2" class="accordion-body collapse">
     <div class="accordion-inner" markdown="1">

      select count(page_views) from wikistats_cached where page_name like "%berkeley%";

     </div>
   </div>
   </div>
   </div>

- Generate a histogram for the number of hits for each hour on May 6, 2009; sort the output by date/time. Based on the output, which hour is Wikipedia most popular?

  <div class="accordion">
  <div class="accordion-group">
   <div class="accordion-heading">
     <a class="accordion-toggle" data-toggle="collapse" href="#collapse-sql3">
       <i class="icon-ok-sign"> </i>
       View Solution
     </a>
   </div>
   <div id="collapse-sql3" class="accordion-body collapse">
     <div class="accordion-inner" markdown="1">
      select dt, sum(page_views) from wikistats_cached where dt like "20090506%" group by dt order by dt;
     </div>
   </div>
   </div>
   </div>

To exit Shark, type the following at the Shark command line (and don't forget the semicolon!).

    shark> exit;

# Intro to Running Standalone Spark Programs
So far we have been doing a lot of ad-hoc style analytics using the command line interfaces to Spark and Shark. For some tasks, it makes more sense to write a standalone Spark program. This section will walk you through the process of writing a stand alone program and running in on EC2.

On our AMP Camp AMI we have included two "template" projects for Scala and Java standalone Spark programs.  These can be found in the `/root/scala-app-template` and `/root/java-app-template` directories.

Feel free to browse through the contents of those directories. You can try to compile and run each of the apps with sbt/sbt run. For more details, see the slides from Matei Zaharia's AMP Camp talk on Standalone Spark Programs which are linked to from [the AMP Camp Agenda page](http://ampcamp.berkeley.edu/agenda).

# Machine Learning

To allow you to complete the machine learning exercises within the relatively short time available during AMP Camp, using only the relatively small number of nodes available to you, we will now work with a restricted set of the Wikipedia traffic statistics data from May 5-7, 2009. In particular, we have restricted the dataset to only include a subset of the full set of articles. This restricted dataset is pre- loaded in the HDFS on your cluster in `/wikistats_20090505-07_restricted`.

## High-level Problem Statement

To allow you to complete the machine learning exercises within the relatively short time available during AMP Camp, using only the relatively small number of nodes available to you, we will now work with a restricted set of the Wikipedia traffic statistics data from May 5-7, 2009.  In particular, we have restricted the dataset to only include a subset of the full set of articles.  This restricted dataset is pre-loaded in the HDFS on your cluster in `/wikistats_20090505-07_restricted`.



## Command Line Preprocessing and Featurization

To apply most machine learning algorithms, we first must preprocess and featurize the data.  That is, for each data point, we must generate a vector of numbers describing the salient properties of that data point.  In our case, each data point will consist of a unique Wikipedia article identifier (i.e., a unique combination of Wikipedia project code and page title) and associated traffic statistics.  We will generate 24-dimensional feature vectors, with each feature vector entry summarizing the page view counts for the corresponding hour of the day.

Recall that each record in our dataset consists of a string with the format "`<date_time> <project_code> <page_title> <num_hits> <page_size>`".  The format of the date-time field is YYYYMMDD-HHmmSS (where 'M' denotes month, and 'm' denotes minute).

1. You can preprocess and featurize the data yourself by following along with the parts of this exercise. To skip ahead to exercise 2 where we start examining the data set we will input to K means, just copy and paste all of the code from our solution to preprocess the data.

   <div class="accordion">
   <div class="accordion-group">
   <div class="accordion-heading">
     <a class="accordion-toggle" data-toggle="collapse" href="#collapse-ml0">
       <i class="icon-ok-sign"> </i>
       View Solution
     </a>
   </div>
   <div id="collapse-ml0" class="accordion-body collapse">
   <div class="accordion-inner" markdown="1">

   Place the following code within a Scala `object` and call the `featurization` function from a `main` function:

   ~~~
   import scala.io.Source
   import spark.SparkContext
   import SparkContext._
   lazy val hostname = Source.fromFile("/root/mesos-ec2/masters").mkString.trim
   def featurization(sc: SparkContext) {
    val featurizedRdd = sc.textFile("hdfs://"+hostname+":9000/wikistats_20090505-07_restricted").map{line => {
      val Array(dateTime, projectCode, pageTitle, numViews, numBytes) = line.trim.split("\\s+")
      val hour = dateTime.substring(dateTime.indexOf("-")+1, dateTime.indexOf("-")+3).toInt
      (projectCode+" "+pageTitle, hour -> numViews.toInt)
    }}.groupByKey.map{grouped => {
      val (article, hoursViews) = grouped
      val sums = Array.fill[Int](24)(0)
      val counts = Array.fill[Int](24)(0)
      for((hour, numViews) <- hoursViews) {
        sums(hour) += numViews
        counts(hour) += 1
      }
      val avgs: Array[Double] =
        for((sum, count) <- sums zip counts) yield
          if(count > 0) sum/count.toDouble else 0.0
      article -> avgs
    }}.filter{t => {
      t._2.forall(_ > 0)
    }}.map{t => {
      val avgsTotal = t._2.sum
      t._1 -> t._2.map(_ / avgsTotal)
    }}
    featurizedRdd.cache()
    println("Number of records in featurized dataset: " + featurizedRdd.count)
    println("Selected feature vectors:")
    featurizedRdd.filter{t => {
      t._1 == "en Computer_science" || t._1 == "en Machine_learning"
    }}.collect.map{t => t._1 -> t._2.mkString("[",",","]")}.foreach(println)
    featurizedRdd.map{t => t._1 -> t._2.mkString(",")}.saveAsSequenceFile("hdfs://"+hostname+":9000/wikistats_featurized")
   }
   ~~~
   </div>
   </div>
   </div>
   </div>


   -  We'll start by entering Spark and loading the data. First, launch a Spark shell.

      ~~~
      cd /root/
      /root/spark/spark-shell
      ~~~

      Next, load the data.

      ~~~
      val data = sc.textFile("/wikistats_20090505-07_restricted")
      ~~~

    - Next, for every line of data, we collect a tuple with elements described next. The first element is what we will call the "full document title", a concatenation of the project code and page title. The second element is a key-value pair whose key is the hour from the `<date-time>` field and whose value is the number of views that occurred in this hour.

      There are a few new points to note about the code below. First, `data.map` takes each line of data in the RDD data and applies all of the code contained in the curly braces after the `=>` symbol. The last line of code is automatically output. The first line of code within the curly braces splits the line of data into the five data fields we discussed in the Spark exercises above. The second line of code within the braces extracts the hour information from the `<date-time>` string. The final line forms the output tuple.

      ~~~
      val featureMap = data.map(line => {
        val Array(dateTime, projectCode, pageTitle, numViews, numBytes) = line.trim.split("\\s+")
        val hour = dateTime.substring(9, 11).toInt
        (projectCode+" "+pageTitle, hour -> numViews.toInt)
      })
      ~~~

      To double-check that your code did what you wanted it to do, you can print the first 10 output tuples:

      ~~~
      featureMap.take(10).foreach(println)
      ~~~

      Now we want to find the average hourly views for each article (average for the same hour across different days).

      In the code below, we first take our tuples in the RDD `featureMap` and, treating the first elements as keys and the second elements as values, group all the values for a single key (i.e. a single article) together using `groupByKey`.  We put the article name in article and the multiple tuples of hours and pageviews in `hoursViews`. The syntax `Array.fill[Int](24)(0)` initializes an integer array of 24 elements with a value of 0 at every element. The for loop then collects the number of days for which we have a particular hour of data in `counts[hour]` and the total pageviews at hour across all these days in `sums[hour]`. Finally, we use the syntax sums zip counts to make an array of tuples with parallel elements from the sums and counts arrays and use this to calculate the average pageviews at particular hours across days in the data set.

      ~~~
      val featureGroup = featureMap.groupByKey.map(grouped => {
        val (article, hoursViews) = grouped
        val sums = Array.fill[Int](24)(0)
        val counts = Array.fill[Int](24)(0)
        for((hour, numViews) <- hoursViews) {
          sums(hour) += numViews
          counts(hour) += 1
        }
        val avgs: Array[Double] =
          for((sum, count) <- sums zip counts) yield
            if(count > 0) sum/count.toDouble else 0.0
        article -> avgs
      })
      ~~~

      Using `println` directly here as above doesn't let us see what’s inside the arrays. The `mkString` method prints an array by concatenating all of its elements with some specified delimiter.  Note that when we use `_1` to access the first part of a tuple, the indexing is 1 and 2 for the first and second parts, not 0 and 1.

      ~~~
      featureGroup.take(10).foreach(x => println(x._1, x._2.mkString(" ")))
      ~~~

    - Now suppose we’re only interested in those articles that were viewed at least once in each hour during the data collection time.

      To do this, we filter to find those articles with an average number of views (the second tuple element in an article tuple) greater than zero in every hour.

      ~~~
      val featureGroupFiltered = featureGroup.filter(t => t._2.forall(_ > 0))
      ~~~

    - So far article popularity is still implicitly in our feature vector (the sum of the average views per hour is the average views per day if the number of days of data is constant across hours).  Since we are interested only in which times are more popular viewing times for each article, we next divide out by this sum.

      If you were following along with the AMP Camp lectures, note that this normalization is different from standardizing each feature separately but accomplishes the goal that all features are on a comparable scale.

      ~~~
      val featurizedRDD = featureGroupFiltered.map(t => {
        val avgsTotal = t._2.sum
        t._1 -> t._2.map(_ /avgsTotal)
      })
      ~~~

      We can use the same command as before to view the latest RDD.

      ~~~
      featurizedRDD.take(10).foreach(x => println(x._1, x._2.mkString(" ")))
      ~~~

    - Save the RDD within Spark and to a file for later use.
      Locally, we just cache the RDD.

      ~~~
      featurizedRDD.cache
      ~~~

      To save to file, we first create a string of comma-separated values for each data point.

      ~~~
      featurizedRDD.map(t => t._1 -> t._2.mkString(",")).saveAsSequenceFile("/wikistats_featurized")
      ~~~

2. In this exercise, we examine the preprocessed data.

    - Count the number of records in the preprocessed data.  Recall that we potentially threw away some data when we filtered out records with zero views in a given hour.

      ~~~
      featurizedRDD.count
      ~~~

      <div class="accordion">
      <div class="accordion-group">
       <div class="accordion-heading">
         <a class="accordion-toggle" data-toggle="collapse" href="#collapse-ml1">
           <i class="icon-ok-sign"> </i>
           View Solution
         </a>
       </div>
       <div id="collapse-ml1" class="accordion-body collapse">
         <div class="accordion-inner" markdown="1">
         Number of records in the preprocessed data: 802450
         </div>
      </div>
      </div>
      </div>


   - Print the feature vectors for the Wikipedia articles with project code “en” and the following titles: Computer_science, Machine_learning.  The second line below shows another option for printing arrays in a readable way at the command line.

     ~~~
     val featuresCSML = featurizedRDD.filter(t => t._1 == "en Computer_science" || t._1 == "en Machine_learning").collect
     featuresCSML.foreach(x => println(x._1 + "," + x._2.mkString(" ")))
     ~~~

      <div class="accordion">
      <div class="accordion-group">
       <div class="accordion-heading">
         <a class="accordion-toggle" data-toggle="collapse" href="#collapse-ml2">
           <i class="icon-ok-sign"> </i>
           View Solution
         </a>
       </div>
       <div id="collapse-ml2" class="accordion-body collapse">
         <div class="accordion-inner">
         <textarea rows="12" style="width: 100%" readonly>
         (en Machine_learning, [0.03708182184602984,0.027811366384522376,0.031035872632003234,0.033454252317613876,0.033051189036678766,0.023780733575171308,0.03224506247480856,0.029826682789197912,0.04997984683595326,0.04433696090286176,0.04997984683595326,0.04474002418379687,0.04272470777912134,0.054816606207174545,0.054816606207174545,0.04474002418379687,0.054010479645304324,0.049173720274083045,0.049173720274083045,0.05038291011688836,0.04594921402660219,0.04957678355501815,0.03667875856509473,0.030632809351068126])
         (en Computer_science, [0.03265137425087828,0.057656540607563554,0.03306468278569953,0.033374664186815464,0.03709444100020666,0.03947096507542881,0.03502789832610044,0.03637115106426948,0.036577805331680105,0.0421574705517669,0.04267410622029345,0.03885100227319695,0.03885100227319695,0.046083901632568716,0.04691051870221121,0.050320314114486474,0.05259351105600331,0.04649721016738996,0.04732382723703245,0.048357098574085565,0.04236412481917752,0.043190741888820015,0.03626782393056417,0.03626782393056417])
         </textarea>
         </div>
      </div>
      </div>
      </div>


## Clustering

Now, try to solve the following problem using Spark. We provide less guidance for this problem. If you run out of time, or get stuck or are just curious, again feel free to jump straight to our solutions.

1. We now further explore the featurized dataset via K-means clustering. Implement K-means clustering (as a standalone Spark program) and generate 10 clusters from the featurized dataset created above. For each cluster, print its centroid vector and the " " strings of 10 articles assigned to that cluster.


   <div class="accordion">
   <div class="accordion-group">
   <div class="accordion-heading">
     <a class="accordion-toggle" data-toggle="collapse" href="#collapse-kmeans1">
       <i class="icon-ok-sign"> </i>
       View Solution
     </a>
   </div>
   <div id="collapse-kmeans1" class="accordion-body collapse">
   <div class="accordion-inner" markdown="1">

   Place the following code within a Scala `object` and call the `clustering` function from a `main` function:

   ~~~
   import scala.io.Source
   import scala.util.Random
   import spark.SparkContext
   import SparkContext._
   lazy val hostname = Source.fromFile("/root/mesos-ec2/masters").mkString.trim
   //computes the Euclidean distance between vectors a1 and a2
   def distL2(a1: Array[Double], a2: Array[Double]): Double = {
     require(a1.size == a2.size)
     var sum: Double = 0.0
     for(i <- a1.indices) sum += math.pow(a1(i) - a2(i), 2)
     math.sqrt(sum)
   }
   //class for computing running averages of real vectors
   class ArrAvg extends Serializable {
     private var avg: Array[Double] = null
     private var count: Int = 0      //number of vectors averaged thus far
     //constructor initializing this ArrAvg to be the average of the single vector arr
     def this(arr: Array[Double]) { this(); this += arr }
     //incorporate arr into this running average
     def +=(arr: Array[Double]): ArrAvg = {
       if(avg == null) avg = arr.clone    //if this average is currently empty
       else {
         //update avg to include arr
         require(avg.size == arr.size)
         for(i <- avg.indices) avg(i) = avg(i)*count/(count+1).toDouble + arr(i)/(count+1).toDouble
       }
       count += 1
       this
     }
     //incorporate all vectors averaged in other into this running average
     def ++=(other: ArrAvg): ArrAvg = {
       if(other.count == 0) return this   //if other is empty
       else if(avg == null) {
         //if this average is currently empty, set it equal to other
         avg = other.avg.clone
         count = other.count
       }
       else {
         //update this.avg and this.count based on the contents of other
         require(avg.size == other.avg.size)
         for(i <- avg.indices) {
           avg(i) *= count.toDouble/(count+other.count)
           avg(i) += other.avg(i)*other.count/(count+other.count).toDouble
         }
         count += other.count
       }
       this
     }
     def getAvg = avg
   }
   def clustering(sc: SparkContext) {
     //define settings
     //desired number of clusters
     val K = 10
     //stopping criterion for K-means iterations (threshold on average centroid change)
     val eps = 1e-6
     //load and cache the featurized data
     val rdd = sc.sequenceFile[String, String]("hdfs://"+hostname+":9000/wikistats_featurized").map{t => {
       //parse the string representation of the feature vectors used for data storage
       t._1 -> t._2.split(",").map(_.toDouble)
     }}.cache()
     //select a random subset of the data points as initial centroids
     var centroids = rdd.sample(false, 0.005, 23789).map(_._2).collect.map(_ -> Random.nextDouble).sortBy(_._2).map(_._1).take(K)
     println("Done selecting initial centroids")
     //run the K-means iterative procedure to find centroids
     var iterI = 0
     var oldCentroids = centroids
     var delta = 0.0
     //iterate until average centroid change (measured by Euclidean distance) is <= eps
     do {
       //store the previous set of centroids
       oldCentroids = centroids
       //compute new centroids; the aggregate() function on RDDs is analogous to
       //  the aggregate() function on Iterables
       centroids = rdd.map(_._2).aggregate(Array.fill(K)(new ArrAvg))((aa: Array[ArrAvg], arr: Array[Double]) => {
         //determine the centroid closest to feature vector arr
         val centroidI = centroids.indices.minBy{i => distL2(centroids(i), arr)}
         //incorporate arr into the ArrAvg corresponding to this closest centroid
         aa(centroidI) += arr
         aa
       }, (aa1: Array[ArrAvg], aa2: Array[ArrAvg]) => {
         //combine ArrAvgs computed for each new centroid
         (aa1 zip aa2).map(t => t._1 ++= t._2)
       }).map(_.getAvg)
       /* NOTE: if no points are assigned to a given centroid, then _.getAvg above will
                yield null; we do not currently handle this case, as this is only an
                exercise, and this situation is unlikely to occur on the given data;
                however, a proper implementation should handle this case */
       //compute the average change between elements of oldCentroids and new centroids
       delta = (centroids zip oldCentroids).map{t => distL2(t._1, t._2)}.reduce(_+_)/K.toDouble
       println("Finished iteration "+iterI+" (delta="+delta+")")
       iterI += 1
     } while(delta > eps)
     //print results
     println("Centroids with some articles:")
     val numArticles = 10
     for((centroid, centroidI) <- centroids.zipWithIndex) {
       //print centroid
       println(centroid mkString ("[",",","]"))
       //print numArticles articles which are assigned to this centroid’s cluster
       rdd.filter{t => {
         centroids.indices.minBy{i => distL2(centroids(i), t._2)} == centroidI
       }}.take(numArticles).foreach(println)
       println()
     }
   }
   ~~~

   </div>
   </div>
   </div>
   </div>

2. Run K-means again (so that the cluster centroids are initialized differently) and see how the clusters change.

   <div class="accordion">
   <div class="accordion-group">
   <div class="accordion-heading">
     <a class="accordion-toggle" data-toggle="collapse" href="#collapse-kmeans2">
       <i class="icon-ok-sign"> </i>
       View Solution
     </a>
   </div>
   <div id="collapse-kmeans2" class="accordion-body collapse">
   <div class="accordion-inner">

   Centroids with some articles for K-means with K=10:

   <textarea rows="12" style="width: 100%" readonly>
   [0.022147691025988934,0.028446757284088322,0.03401348051181095,0.04028622120760124,0.044808787400008564,0.05102000676659651,0.054746875714012155,0.056997844456603025,0.06023923286538736,0.05995930437707476,0.05484747876276983,0.052368291791386884,0.05836844829674058,0.06332469983469591,0.06252944032549862,0.05669573830648844,0.04611266361220511,0.036162554227650384,0.02705067763648778,0.021163652272439472,0.017747384624005096,0.016299762995042936,0.016537288598262062,0.018125717107154878]
   (ja %E8%A5%BF%E5%B7%9D%E7%BE%8E%E5%92%8C,[D@16d19d73)
   (ja %E7%A5%9D%E6%97%A5%E6%B3%95,[D@1bb7f4b9)
   (ja %E3%83%9C%E3%83%93%E3%83%BC%E3%83%BB%E3%82%AA%E3%83%AD%E3%82%B4%E3%83%B3,[D@4a23abe6)
   (ja %E3%82%A8%E3%83%AA%E3%83%BC%E3%82%BC%E3%81%AE%E3%81%9F%E3%82%81%E3%81%AB,[D@6beaaf10)
   (ja 2%E9%87%8D%E5%8F%8D%E8%BB%A2%E3%83%97%E3%83%AD%E3%83%9A%E3%83%A9,[D@7c8b7ac9)
   (ja %E5%B0%8F%E8%B0%B7%E5%B9%B8%E5%BC%98,[D@292a6200)
   (ja DOUBLE,[D@1d88031d)
   (ja P-3_(%E8%88%AA%E7%A9%BA%E6%A9%9F),[D@4c847410)
   (ja %E7%84%A1%E9%87%8F%E5%A4%A7%E6%95%B0,[D@bdfedaf)
   (ja Roman,[D@1a6d8240)
   [0.018530166208031794,0.014164029467697646,0.011775435662179815,0.010682359375998917,0.010434563297147339,0.011733114542324872,0.015504242943136072,0.022969386777524067,0.03073608478499401,0.03718037182862875,0.041169148553117506,0.04339765324619536,0.049217061716337725,0.058553363000577154,0.06397029996914101,0.06819456022389997,0.07040081881858114,0.07065109592285682,0.07545585630364683,0.0744093165084814,0.07123832576597369,0.058321300507240874,0.04336448167990859,0.027946962896378505]
   (es Melendi,[D@778e7e6a)
   (fr Fichier:US_Locator_Blank.svg,[D@2c0d9e78)
   (it Omicidio,[D@321eeee0)
   (de Ridley_Scott,[D@cd4cdd5)
   (fr Thierry_Henry,[D@67156187)
   (en USS_Vella_Gulf_(CG-72),[D@2cb61af0)
   (pl Dmitrij_Mendelejew,[D@67a2997e)
   (pl Jurij_Gagarin,[D@640b5459)
   (en Roderick_Bradley,[D@23ced25f)
   (de Konjunktur,[D@5f556d56)
   [0.04004400475581517,0.038118787030749966,0.03801015733643086,0.03967369502072206,0.04151351988591238,0.04308393683465291,0.044778057420320046,0.04384191817330043,0.04327253705198659,0.04337169109087168,0.04206913431317084,0.040278518303564805,0.04101954271722155,0.038450206013100216,0.0418555337240833,0.043012543561769416,0.04353482943994479,0.043243093996798466,0.04369268461921003,0.04282953399927644,0.042365251465160035,0.04139263576836268,0.040894906504096745,0.03965328097347838]
   (en Dick_Fuld,[D@25a97638)
   (en Stargate_SG-1_(season_10),[D@189069d9)
   (en Recollection,[D@31153228)
   (en John_Taverner,[D@668ff944)
   (en Category:Computer_languages,[D@763613f6)
   (en Dream_Job,[D@5858b81b)
   (en Fictional,[D@289f21ed)
   (en Brazilian_films_of_2008,[D@676c6370)
   (en Breville,[D@7cf4831b)
   (en Intel_P35,[D@18cba1b4)
   [0.0374891593665341,0.03647933999115497,0.03490100360527816,0.03515709593165882,0.03469511934394539,0.03467025288539275,0.03329515631628146,0.03368583693510732,0.03477995751228721,0.036139262146712585,0.036710028791579495,0.037891229499294896,0.042674988377533606,0.08459747304988914,0.047525076105976734,0.04547605472166529,0.045894075497203375,0.04573279489946979,0.04511827746337737,0.04541791931578055,0.04559712807618702,0.04402081145055172,0.042593234700449276,0.039458724016689144]
   (en Modernized_Load-Carrying_Equipment,[D@5357f509)
   (en Passenger_rail_transport_in_China,[D@33984c9e)
   (en Sentimental_novel,[D@53ce3388)
   (en BBC_Online,[D@69950b4)
   (en Len_Bosack,[D@3b815cce)
   (en Fuji-Servetto,[D@29f3f6e7)
   (ja %E5%B6%8B%E6%9D%91%E4%BE%91,[D@4fd6cd35)
   (en Lechitic_languages,[D@1f3a7b86)
   (en Digital_subtraction_angiography,[D@3883aa6e)
   (ru.q %D0%A1%D0%B5%D0%BA%D1%81,[D@4c2a38be)
   [0.050561001862224196,0.05109208279218707,0.052605718164506036,0.05403393713172642,0.05214285050352309,0.04787392679595907,0.039443144422478656,0.03296110630434489,0.02877417857709431,0.0266050285603666,0.02535277917627233,0.025495577756069122,0.027674046032043615,0.03230355314426185,0.036812246876504216,0.039516779336855566,0.04251301869056978,0.04416092811265632,0.045521754846991565,0.04708016847807021,0.049128276676792884,0.04986530476895841,0.04976581264907038,0.0487167783404734]
   (en Alydar,[D@6b8a24e9)
   (en AJ_Buckley,[D@3a7d7427)
   (en The_Old_Regime_and_the_Revolution,[D@254ca71)
   (en File:Evstafiev-bosnia-sarajevo-serbs-toast.jpg,[D@cb6c1e9)
   (en Underground_(soundtrack),[D@3274ae84)
   (en Pandamonium_(TV_series),[D@a6d75fd)
   (en Joe_B._Hall,[D@185d3a64)
   (en Nicotina,[D@10a487bb)
   (en The_Sandman_(DC_Comics/Vertigo),[D@21cb0e86)
   (en Deep_Water_Slang_V2.0,[D@1a2690bf)
   [0.041710303207894095,0.038279529726798316,0.03633519156675436,0.036137178058335974,0.03588734746680139,0.03511199697067867,0.03273275085363018,0.031182210413119978,0.0309164565152981,0.031029341057239656,0.031292135138417894,0.03203783496775679,0.03456017008357846,0.03721815744086422,0.04064688332473918,0.0426120213468905,0.044877155468792694,0.04537918193846189,0.04616844847291527,0.04909265938711985,0.05946311232478101,0.0735443794073923,0.06434038325303444,0.04944517160870489]
   (en Vice_Chief_of_Naval_Operations,[D@3f686e67)
   (en Secret_Admirer,[D@61284cb6)
   (en Cleaver_(The_Sopranos),[D@1cc4728f)
   (es 18_de_abril,[D@253d190a)
   (en Yuya_Ozeki,[D@4e36f29f)
   (en File:Gray557.png,[D@7972ccd6)
   (en Where%27s_Your_Head_At%3F,[D@5fe633e4)
   (en Paperback_Hero_(1999_film),[D@258eeec)
   (en Aging_barrel,[D@1cadc928)
   (en Crawler-transporter,[D@55716fcd)
   [0.07083508476656544,0.06248561700630076,0.05226199674927553,0.04391332021512457,0.0362388948836421,0.028771811915904597,0.021766912642120367,0.017851811183800522,0.016547718864265582,0.016204081338222126,0.01609928843778344,0.01793340743858376,0.02167101081510387,0.026905775933065687,0.03476028008247919,0.04174969217701337,0.04556642622981125,0.0502964960299522,0.05343915640099257,0.05633593277960072,0.05930919417599005,0.06268432745911172,0.07100232753007056,0.07536943494521993]
   (es Cocci%C3%B3n_del_pan,[D@4113dfa1)
   (en Incest_(book),[D@7fbee467)
   (es Rigidez,[D@4d8567b6)
   (en It%27s_a_Big_Big_World,[D@715c057c)
   (es Xena,_la_princesa_guerrera,[D@506c4a08)
   (es Compa%C3%B1%C3%ADa_multinacional,[D@75707c77)
   (en Canine_degenerative_myelopathy,[D@2bfba4dd)
   (es Napalm_Death,[D@1604bfba)
   (es Bill_Weasley,[D@11c757a1)
   (pt Intifada,[D@364a1425)
   [0.015940518515149795,0.013132258953179704,0.011945897552640562,0.011861991244436657,0.012805831491068948,0.015679187263148014,0.02262443064726532,0.035556345120969696,0.04741118399400519,0.05668415262778178,0.06092749528188912,0.06019468009188924,0.06382755672329135,0.06871833451187394,0.06897048207607162,0.06478477925594898,0.060607724418520206,0.055794153506566455,0.05487181360690192,0.05290308482272885,0.04992589295590789,0.04168858732173386,0.03152206946939797,0.02162154854763272]
   (fr Alain_Duhamel,[D@5dd22216)
   (ru %D0%A2%D1%83%D0%B9%D0%BE%D0%BD,[D@ebc0279)
   (fr NFFNSNC,[D@59465d7d)
   (de Michael_Diekmann,[D@1aa3e755)
   (ru %D0%92%D0%B8%D0%BA%D0%B8%D0%BF%D0%B5%D0%B4%D0%B8%D1%8F:%D0%97%D0%B0%D1%8F%D0%B2%D0%BA%D0%B8_%D0%BD%D0%B0_%D1%81%D1%82%D0%B0%D1%82%D1%83%D1%81_%D0%BF%D0%B0%D1%82%D1%80%D1%83%D0%BB%D0%B8%D1%80%D1%83%D1%8E%D1%89%D0%B5%D0%B3%D0%BE,[D@7ea88b1c)
   (zh %E5%BD%AD%E4%BA%8E%E6%99%8F,[D@6b451c3a)
   (de St%C3%A4dte_in_den_Vereinigten_Staaten,[D@6405adb)
   (de Mini_(BMW),[D@6b0ce311)
   (de Fusion_(Wirtschaft),[D@1b0fa7ff)
   (ru %D0%A5%D0%B0%D0%BD%D0%BA%D0%B0,[D@20de643a)
   [0.03352617350252567,0.03079052991999171,0.02964440457103978,0.029540020953520832,0.029461836540902554,0.029905089678931806,0.029808884916087165,0.03200541174382243,0.03512436551351589,0.039966520210524596,0.04240044988606437,0.04418582216021132,0.04632109711997721,0.04557993800971453,0.05757313899096623,0.05831192867593248,0.0564793332541563,0.051774310974806004,0.049047699260265844,0.049311394625994494,0.049607769417391215,0.047604845887662825,0.04413031817168932,0.03789871601430566]
   (es Stamford_Bridge,[D@273f5150)
   (en Santa_Fe_Institute,[D@2df36f51)
   (ru %D0%9A%D0%BE%D0%BD%D1%82%D0%B5%D0%B9%D0%BD%D0%B5%D1%80%D0%BE%D0%B2%D0%BE%D0%B7,[D@49114668)
   (en Large_segment_offload,[D@d257b52)
   (ru %D0%92%D0%BE%D1%82%D0%BA%D0%B8%D0%BD%D1%81%D0%BA%D0%B8%D0%B9_%D0%B7%D0%B0%D0%B2%D0%BE%D0%B4,[D@17c34b3c)
   (en V%C3%A9hicule_de_l%27Avant_Blind%C3%A9,[D@16805c54)
   (en Lobengula,[D@452d9a56)
   (en White_City_Stadium,[D@34efa795)
   (en Magdalene_College,_Cambridge,[D@61e34259)
   (en Garth_Jennings,[D@f191344)
   [0.04397009580374622,0.04162512759241117,0.03956298146407021,0.03880084655148732,0.03722109162254666,0.03461427673659028,0.03034563252301237,0.027899146946995145,0.02672476183219311,0.02700962431341052,0.02676881490505619,0.027838522610068694,0.03097753816972948,0.03720679918909251,0.04442681935077926,0.04892967621461186,0.053597342502344755,0.057160187311227316,0.06022186015740068,0.06160813428878053,0.05672625533767601,0.051335667357740755,0.049207945989977214,0.04622085122905136]
   (en The_Good_Girl_Gone_Bad_Tour,[D@7ebc7f3f)
   (en Saliva_(band),[D@946421a)
   (en Queue,[D@639dbdb7)
   (en Tufted_Titmouse,[D@9126c79)
   (en Keystone_(architecture),[D@188f8096)
   (en George_Jones,[D@75430472)
   (en Jason_&_the_Scorchers,[D@2d91483)
   (en File:Lamb_of_God-0358-Randy_Blythe.jpg,[D@79d1a5ea)
   (en Beneath_the_Sky,[D@32e3e421)
   (en Pajeon,[D@f964ca1)
   </textarea>

   </div>
   </div>
   </div>
   </div>

3. Repeat #2, now using 50 clusters.


   <div class="accordion">
   <div class="accordion-group">
   <div class="accordion-heading">
     <a class="accordion-toggle" data-toggle="collapse" href="#collapse-kmeans3">
       <i class="icon-ok-sign"> </i>
       View Solution
     </a>
   </div>
   <div id="collapse-kmeans3" class="accordion-body collapse">
   <div class="accordion-inner">

   Centroids with some articles for K-means with K=50:

   <textarea rows="12" style="width: 100%" readonly>
      [0.013690037692894869,0.010981137438827645,0.009832182695759195,0.009747683964607061,0.01062609887924154,0.013795931813798259,0.022683117107498634,0.041557225413187376,0.058233651817725204,0.07006137710396709,0.0714935795862924,0.06678149417967616,0.06815596753041171,0.0727939164406252,0.06914650297633196,0.06319227057504655,0.055070747215743086,0.04834312245154153,0.04703580574392569,0.04624735116600365,0.04519051524010059,0.038067519497170195,0.028328931218810187,0.01894383225081437]
      (en Energy_systems,[D@44f55289)
      (de Flughafen_Berlin-Tegel,[D@45fdc3bd)
      (fr API,[D@173dd131)
      (fr AXA,[D@5b70f3e2)
      (fi Kosovo,[D@549ba954)
      (de Ich_heirate_eine_Familie,[D@3789f531)
      (pl Rumie%C5%84_zaka%C5%BAny,[D@42408073)
      (vls Voorblad,[D@18b12000)
      (ru %D0%96%D0%B5%D0%BD%D1%89%D0%B8%D0%BD%D0%B0_%D1%81%D0%B2%D0%B5%D1%80%D1%85%D1%83,[D@14abecaa)
      (en Mobile_Application_Part,[D@233e9780)
      [0.03359814011632332,0.02667104594671035,0.022811578225626638,0.022856983200659174,0.02093809674251062,0.020242821353529512,0.019750518562312233,0.02036905084222096,0.022175798302187164,0.024598116150137232,0.02588978025099701,0.02687859951197871,0.029374901136574393,0.032166066251566726,0.0331893071088909,0.03594835549655178,0.03809043706775847,0.03842153852781294,0.04098059570559075,0.04254181856879228,0.04555675947730949,0.06401096790093644,0.21245755037991104,0.10048117317311173]
      (es Categor%C3%ADa:Nacidos_en_1990,[D@79113734)
      (en Melanie_Hill,[D@593f7504)
      (en Daytime_Emmy_Awards,[D@551b8762)
      (es Alter_ego,[D@2555e95)
      (en Resevoir_Dogs,[D@32b48965)
      (es Celtic_Metal,[D@6aa85fe4)
      (en Zbtb7,[D@542fa870)
      (es Robin_Tunney,[D@4db7c3e)
      (en The_Ring_Magazine,[D@65b9ccef)
      (he %D7%A9%D7%9C%D7%9E%D7%94_%D7%90%D7%A8%D7%A6%D7%99,[D@93d9316)
      [0.037312987693335777,0.03622333855264966,0.034788692352340896,0.03488444244719704,0.03466895473892905,0.03427797856800618,0.03319911683428152,0.03320298207144765,0.03426614206378585,0.035447935412627925,0.035627841707178136,0.03641543106806654,0.039647660765122814,0.09200699009124498,0.045984824048791964,0.04553392824007411,0.045832607251189525,0.04562537123946478,0.04588185281273379,0.045479749167558155,0.04573441295163629,0.045226482036330444,0.04329272194852333,0.03943755593748365]
      (en Top_Combine,[D@15a12c74)
      (en Two_Cunts_in_a_Kitchen,[D@86d7ec4)
      (en Palm_V,[D@4c9d7a2e)
      (fr Bas,[D@76c0894d)
      (en Line-line_intersection,[D@5a765b23)
      (en Third_Way_(UK),[D@1059853b)
      (es Tasa_de_fertilidad,[D@6539ecdf)
      (en Aphididae,[D@3dee5864)
      (en Sara_Lee_Corporation,[D@6aed2cff)
      (en White_Collar_Boxing,[D@79255030)
      [0.03827607972931965,0.03688290857519039,0.03730575040076769,0.03804161979455341,0.03904031050248076,0.04077215443277167,0.09972056647746416,0.038591768311124416,0.03520931723146316,0.034799623525455015,0.03432663045242935,0.03352939077257891,0.035304144837436414,0.03759911382135781,0.04030152155603132,0.04165315121437421,0.042572801776989216,0.04276207666611427,0.04337664385774248,0.04300142068756039,0.04302886501159411,0.043166354210285786,0.04166397166430874,0.03907381449060665]
      (en Dream_Job,[D@5e288a7c)
      (en Dog_Chapman,[D@379c032f)
      (en Murray_Leinster,[D@63951f2e)
      (en Casablanca_(disambiguation),[D@53323815)
      (en City_of_Lancaster,[D@600155f)
      (en Tift_Merritt,[D@6f7b130c)
      (en Make_Me_a_Supermodel_(Australian_TV_series),[D@791fb201)
      (en Absolutely_(Madness_album),[D@fdf9dc7)
      (en SEED,[D@21ef7bf5)
      (en Dillon_Aero_INC,[D@71b3233a)
      [0.043326443282417526,0.03967449739106684,0.038110062053493717,0.03726680851406506,0.0363693517418515,0.03507937150901526,0.033183981825031864,0.031823267576933115,0.030963210679450077,0.03142154196106844,0.031122496273773798,0.03168960523456704,0.033497483580470076,0.03626323295512729,0.04003676658112028,0.041837864603423786,0.043296993424595656,0.04508496017852218,0.0452870674135991,0.047150281518651,0.04725912851090397,0.04849839258876087,0.050435360662139495,0.10132182993995222]
      (en Pajeon,[D@4f083e77)
      (en Primer_(film),[D@263d1f2a)
      (es Bill_Weasley,[D@76c91202)
      (en Robert_Nilsson,[D@abd7e3b)
      (en File:Sentrycarnage.jpg,[D@1eaa58ff)
      (en Don%27t_Turn_Around,[D@41b8dd13)
      (en Fascination,[D@4fba2c1a)
      (es Categor%C3%ADa:Nacidos_en_1981,[D@47a587da)
      (pt Adenosina,[D@447a1df5)
      (en Wire-guided_missile,[D@2d44e2f2)
      [0.04184299891781855,0.03951277738466899,0.03792117323131207,0.03772711612080457,0.03704490113634133,0.03586663392243524,0.03348124312463087,0.032208741167506,0.03171329074644188,0.03162501936613686,0.03170549632568884,0.03232586320640061,0.033864323541233546,0.03743102991696143,0.04038372043647544,0.04249190210888159,0.0443611661150819,0.04542617093972548,0.04660525258257858,0.048144379010137885,0.0490506670315248,0.05156618928727052,0.09055327204939653,0.047146672330546154]
      (en AJ_Buckley,[D@305b8b04)
      (en Yuya_Ozeki,[D@4a03cc69)
      (en Paperback_Hero_(1999_film),[D@3f612775)
      (en Aging_barrel,[D@36592d5)
      (en Digital_subtraction_angiography,[D@18adfb1)
      (en Acrodermatitis_enteropathica,[D@4463de41)
      (en File:Sopchart.jpg,[D@7894a07a)
      (en File:Andres_Iniesta_Joan_Gamper_.jpg,[D@65b3e76e)
      (en Three_Colors,[D@61f1aec)
      (en Introducing_the_Hardline_According_to_Terence_Trent_D%27Arby,[D@64207f17)
      [0.044271353679789356,0.09764782189347346,0.04350063698269629,0.0403001813119237,0.039466590396921665,0.036584016323859846,0.03384901027533281,0.03196672294836675,0.031082171495541686,0.03147669899786348,0.030608378921005524,0.03143075869572981,0.03274290192148159,0.03682961905334994,0.03985493175440974,0.04179411507292415,0.04277457117287955,0.0436993353678273,0.04457064686117769,0.045691846472146413,0.045736933285157814,0.04619319025599423,0.044900356290875215,0.043027210569272105]
      (en Fictional,[D@1f41ae43)
      (en WNGS,[D@1117c0bd)
      (en List_of_Governors_of_Oregon,[D@2e6ec10f)
      (en Bay_leaves,[D@6944d799)
      (en Don_Stroud,[D@26a6b3cd)
      (en Abb%C3%A9_Faria,[D@a666b8e)
      (en Hart_Foundation,[D@4a1f3cf7)
      (en File:IMG_0469_Kathmandu_Pashupatinath.jpg,[D@48dba225)
      (en V4_engine,[D@4b725081)
      (en Category:Moon_myths,[D@3e036bd9)
      [0.03761333070702806,0.0354847905362183,0.03476986020972756,0.03501780607939363,0.03456576391219111,0.03395166743484648,0.03216088820318773,0.03194932046885595,0.03275094299437892,0.03561919162191425,0.0362424623751747,0.03699243825368648,0.03881797073344937,0.0444790571841871,0.05522506878461686,0.053064772003426706,0.050073486871666406,0.050878210196894355,0.052943233827590884,0.05271201775778118,0.050464127528411604,0.048696072983404476,0.04511252249649672,0.04041499683547097]
      (en Large_segment_offload,[D@2a8d9527)
      (en Sentimental_novel,[D@353b4944)
      (en Gluon,[D@47472aa9)
      (en Patrick_Rothfuss,[D@1141e1ce)
      (en T%C3%ADr_na_n%C3%93g,[D@7c4fa43b)
      (en Diversity,[D@50a75d3f)
      (en Mahican,[D@1bdafc77)
      (en Collaboration,[D@45f4f6ea)
      (en Kevin_Carter,[D@557ea087)
      (en Wink,[D@6c5d2aea)
      [0.03897867816128028,0.03834860974412867,0.03880068697572123,0.04245155312925211,0.10845262015277504,0.04337852545314084,0.03813871735335395,0.03581182125290446,0.03383139737415474,0.03210265389478046,0.031004482517117633,0.03144340190739859,0.032734701699384326,0.036301067013269614,0.039281284546963766,0.04001816146772564,0.04152299113504844,0.04220167208584952,0.04226062259678078,0.043631710419223854,0.04345588017683003,0.043744323497829994,0.04203092060242138,0.040073516842664574]
      (ja %E3%83%9C%E3%83%93%E3%83%BC%E3%83%BB%E3%82%AA%E3%83%AD%E3%82%B4%E3%83%B3,[D@3cb04ab4)
      (en Big_Eyes,_Small_Mouth,[D@59f88e8c)
      (en Great_year,[D@4d56b779)
      (en IBM_Informix,[D@78279099)
      (en Chinese_exploration,[D@6d866a6f)
      (en.d obstinate,[D@13b5d596)
      (en Brad_Lidge,[D@6d974f6)
      (en Chapter_15,_Title_11,_United_States_Code,[D@2e8dc5fd)
      (en Ayesha_Omar,[D@5dc17ceb)
      (en A_Month_in_the_Country,[D@49f05863)
      [0.03365161766189066,0.03277616570729011,0.032246219814716155,0.03218938897598891,0.032220189956570514,0.03265657570491317,0.03204582701163645,0.03237577015555831,0.03396083491870862,0.03516202711334218,0.035702835276102865,0.036311840815814976,0.039532527077426625,0.045957046549533184,0.12931793345567272,0.05239889799032056,0.046747624709228726,0.04427990432340238,0.04224248663850008,0.042106895618955226,0.04194780022083082,0.040648485500148775,0.038022122680712964,0.035498982122734994]
      (en Gravitational_potential_energy,[D@7abcaec7)
      (en Street_Sk8er,[D@7976c9c4)
      (en Category:National_Hockey_League_statistical_records,[D@65b8a128)
      (en Agrippina,[D@3c5c7d44)
      (ja %E5%9D%82%E4%BA%95%E6%B3%89%E6%B0%B4,[D@5c241a51)
      (en Ridge_Racer_Type_4,[D@463beb19)
      (en Belo_horizonte,[D@7c7da55)
      (en The_Masque_of_Mandragora,[D@51cf0670)
      (en Shrek_The_Third,[D@6eafcd05)
      (ja %E6%95%A3%E5%85%B5,[D@57bd3c08)
      [0.040793739450269734,0.03950960163844526,0.03881191648853432,0.04004274927356807,0.04113059824886209,0.08093146576163252,0.03844347591804635,0.03527365933015297,0.034355388778771555,0.033082892759197936,0.03218837315511005,0.032465116806668484,0.03380462263743424,0.03700114890954683,0.04041859797115725,0.04193685935348478,0.04324820547690486,0.0443409072105781,0.045524140709397684,0.04585127968926604,0.04677681393789954,0.04715188091698689,0.04482002861450173,0.04209653696358257]
      (en Paris_Is_Burning_(EP),[D@1810360f)
      (en Prince_Adam,[D@504d1f20)
      (en Desperate_Living,[D@7736970f)
      (en Emotions_(album),[D@20f796ee)
      (en Anheuser_Busch,[D@5ed5f42a)
      (en File:1971MonteCarlo.jpg,[D@3091220a)
      (en Kyung_Wha_Chung,[D@851d76f)
      (en Talk:Human_penis_size,[D@3516bcad)
      (en Laura_Hall,[D@67bd1d15)
      (en File:Milky_Way_Spiral_Arm.svg,[D@30c082e8)
      [0.027591976891821322,0.025822204316779045,0.025374142787286107,0.025869385580310933,0.026432669517413666,0.026627123307825597,0.026236227379136064,0.02702211243993515,0.029283045801486038,0.029088634920805507,0.03076973030513273,0.031512411002304584,0.033165428688053576,0.037162409576505816,0.04084469985792605,0.05655311764214123,0.2147632454291122,0.06693469085175532,0.043926243322686256,0.03945132453778323,0.037018028172677846,0.03548450686780665,0.0328633080029957,0.030203332800319352]
      (en File:GVRD_-_Vancouver.svg,[D@68608f02)
      (en Ores,[D@1b4d01d7)
      (en Jamaica_national_bobsled_team,[D@5cb3d841)
      (en List_of_famous_people_from_the_Washington,_D.C._metropolitan_area,[D@233ac2f3)
      (commons.m Category:Sabine_women,[D@66d4f196)
      (de Klaas_Heufer-Umlauf,[D@2640e398)
      (en Bethe_formula,[D@6e1e23c6)
      (ja %E7%89%B9%E5%88%A5:%E3%83%87%E3%83%BC%E3%82%BF%E6%9B%B8%E3%81%8D%E5%87%BA%E3%81%97/5%E6%9C%888%E6%97%A5,[D@7cbed4a4)
      (ja %E5%B9%B3%E5%B2%A9%E7%B4%99,[D@547a9af7)
      (en Francis_Chan,[D@3d53f975)
      [0.044559703026044374,0.043920884491910506,0.04385091423698174,0.04504920775559662,0.044156520545519815,0.040687918738288774,0.03634123738785706,0.03204831412333519,0.029024102679377224,0.028020241363275387,0.027190870118347962,0.027691781728013915,0.03079751104796179,0.03686688333504525,0.041678384349284184,0.04477063975507247,0.047277457126474534,0.04941658013871971,0.05149293735427017,0.052732795974168176,0.052224278133679115,0.05217831791623576,0.05042771464964218,0.047594804024897984]
      (en Alydar,[D@6a3e3449)
      (en Where%27s_Your_Head_At%3F,[D@19dadb1d)
      (en Nadya_Suleman,[D@6f675a2d)
      (en Fat-tailed_scorpion,[D@6a51797e)
      (en Local_extinction,[D@aff75ba)
      (en Qubit,[D@98a0bfa)
      (en Hanson_Brothers,[D@46184efa)
      (en Philip_V_of_Spain,[D@65e92309)
      (en Atomic_number,[D@2cfb5824)
      (en Order_of_British_Empire,[D@2957ba8e)
      [0.03319898557711845,0.02752870947082992,0.024903767931141078,0.02401120598479864,0.023273298342706042,0.022739033936211118,0.021623280672054775,0.023109351532176105,0.02652239503530215,0.028012859414067726,0.02906039479108302,0.0300438073437489,0.03278085962579659,0.03473307000095583,0.03706334634870501,0.03895903072016884,0.03991931415030645,0.0401526308036665,0.04098039672024446,0.04726914686600329,0.06862485254632487,0.18530826574750422,0.0736464396408501,0.0465355567982359]
      (en Newport_(disambiguation),[D@7a29467)
      (en Fc_barca,[D@42bcfa1b)
      (en Ellen_Johnson_Sirleaf,[D@4c824d8)
      (en Islam_in_Mongolia,[D@6feae35b)
      (en UEFA_Cup_2008%E2%80%9309_final_phase,[D@25f4a4c4)
      (sv Volkswagen,[D@606d5353)
      (en Jonny_Kennedy,[D@29799f9a)
      (es Manchester_United_F.C.,[D@6fdd4cbb)
      (commons.m Template:Potd/2009-05-08_(de),[D@29d8c25d)
      (en LAV_25,[D@55c07d21)
      [0.021001188149544842,0.015662223535789305,0.012726650038060368,0.011267719870763726,0.010655019775933031,0.011534474311909359,0.015166810748334016,0.0222543571174067,0.03054092777433242,0.03752774859543848,0.04194575488346181,0.04450704223764425,0.05106646527149139,0.05653706449418359,0.058362275353036175,0.05990488829645834,0.060974373718449666,0.06252313181060352,0.06804350543555938,0.07441789599650522,0.07935722632683219,0.06957704838600963,0.05184508809559512,0.03260111977665739]
      (es Melendi,[D@405e036b)
      (de Ridley_Scott,[D@5c3286d0)
      (fr Thierry_Henry,[D@7930f0f9)
      (en USS_Vella_Gulf_(CG-72),[D@7c1ca997)
      (de DeForest_Kelley,[D@3dc23829)
      (pl Rumie%C5%84_lombardzki,[D@19f52b6c)
      (nl Hendrik_VIII_van_Engeland,[D@2ef5ccc4)
      (de Damian_Marley,[D@b566ff2)
      (en Tom_Parker_Bowles,[D@57cf7eae)
      (pl Open_Directory_Project,[D@7785fe8b)
      [0.03447811547041484,0.03358192295206849,0.03288753063881627,0.03361392236345572,0.03426027392413496,0.034409975721540725,0.03412619749556583,0.03491143758082917,0.03670883293641782,0.03972602190276481,0.1015134434535629,0.04110278199898818,0.04080814598333609,0.04279393927491422,0.04401718669485223,0.04549877077889781,0.044677936072879704,0.04401018042040887,0.043192343695201287,0.043095746743131984,0.04227538289981791,0.04194572380956984,0.040016846569746314,0.03634734061868388]
      (ru %D0%92%D0%B8%D0%BA%D0%B8%D0%BF%D0%B5%D0%B4%D0%B8%D1%8F:%D0%97%D0%B0%D1%8F%D0%B2%D0%BA%D0%B8_%D0%BD%D0%B0_%D1%81%D1%82%D0%B0%D1%82%D1%83%D1%81_%D0%BF%D0%B0%D1%82%D1%80%D1%83%D0%BB%D0%B8%D1%80%D1%83%D1%8E%D1%89%D0%B5%D0%B3%D0%BE,[D@6a89dd28)
      (ja %E3%82%81%E3%81%90%E3%82%8A%E9%80%A2%E3%81%84,[D@116a3bd)
      (ru.q %D0%A1%D0%B5%D0%BA%D1%81,[D@7557cdd9)
      (en List_of_people_from_Connecticut,[D@578b9dbe)
      (ar %D8%B2%D8%B9%D9%81%D8%B1%D8%A7%D9%86,[D@f2151d9)
      (en The_Nosebleed_Section,[D@54848551)
      (en File:Viswanathan_Anand_08_14_2005.jpg,[D@484cb430)
      (en Goodbye_Alice_in_Wonderland,[D@23c9e065)
      (en File:Latex_dripping.JPG,[D@1aab193e)
      (en Yarm,[D@5706431f)
      [0.020487640073007667,0.02212459769348883,0.023217130467478045,0.024475851604047128,0.02644861307925844,0.026752047401033364,0.029077695701185662,0.029896089092520218,0.031665355208896705,0.0331073802964078,0.033504259245394935,0.03483148466642022,0.06218923812841838,0.24472236324180352,0.08778332394589201,0.05497618208363646,0.041576281986889854,0.033333454227206696,0.028007897956862437,0.026103683208714108,0.02339840086482072,0.021817803980119798,0.0205316390473179,0.019971586799179215]
      (de Chronik_der_Attentate,[D@de4e7b8)
      (en List_of_Dungeons_%26_Dragons_4th_edition_monsters,[D@2f28e828)
      (ja %E5%BE%B3%E5%B7%9D%E5%AE%97%E6%AD%A6,[D@26a1b248)
      (ja %E6%88%91%E5%AD%AB%E5%AD%90%E5%B8%82,[D@41bfa8c8)
      (en Tinderbox,[D@de20431)
      (en Russell_Brand%27s_Ponderland,[D@71813415)
      (ja %E3%82%A2%E3%82%B9%E3%82%AF%E3%83%AC%E3%83%94%E3%82%A6%E3%82%B9,[D@598288ea)
      (en 2,5-dimethoxy-4-methylamphetamine,[D@8eae88f)
      (en List_of_NCAA_Division_I_FBS_football_stadiums,[D@764a08db)
      (ja %E8%8A%8B%E6%B4%97%E5%9D%82%E4%BF%82%E9%95%B7,[D@768b9e88)
      [0.037518906143072725,0.035496880747337395,0.034590310461296715,0.03447227923574621,0.03407499459486551,0.03260205391923134,0.03146863730306599,0.03055314814406124,0.030847191040714365,0.03166606091132011,0.031870402104066206,0.03246324583135959,0.034534637147116375,0.03812057121438213,0.0409175771542706,0.04244592811247962,0.045042062948758975,0.04885399097059433,0.11825106334730298,0.052969721860340074,0.04895865141192071,0.046740686392732565,0.04512377073530985,0.04041722826865432]
      (en Category:Computer_languages,[D@bb2f67e)
      (fr Fichier:US_Locator_Blank.svg,[D@1254d032)
      (ja %E5%B6%8B%E6%9D%91%E4%BE%91,[D@7e308c04)
      (en Fran%C3%A7ois_B%C3%A9gaudeau,[D@25389b55)
      (en Maria_Louisa_of_Spain_(1745-1792),[D@275dfc8a)
      (en The_Dead_Zone,[D@b6edc37)
      (en Alessandro_Rosina,[D@1b3ba4be)
      (en Andy_(given_name),[D@68bcc3e9)
      (en.q CSI:_Crime_Scene_Investigation,[D@40e22bb7)
      (en Boonoonoonoos,[D@40a41ca8)
      [0.0540359054872748,0.0507839812836343,0.047979435906455876,0.04594535509918895,0.04277513608087509,0.037530401692376265,0.029809052504704656,0.024455210534394405,0.0215803501894298,0.02002576050889224,0.01915230856803524,0.019893676863634698,0.022576835164870676,0.027972134693589324,0.035001370226341935,0.04161082912402405,0.04799162466726147,0.052658326848639615,0.0563978673998805,0.059109939837125605,0.06102891601510996,0.06247200583494897,0.061307094188449915,0.05790648128086151]
      (en Saliva_(band),[D@42758ddb)
      (en George_Jones,[D@33b446f3)
      (en File:Lamb_of_God-0358-Randy_Blythe.jpg,[D@9e160a)
      (es Rigidez,[D@ab8ead7)
      (es Xena,_la_princesa_guerrera,[D@7241cec0)
      (en Blood_in_My_Eye,[D@3e6cd9da)
      (es Napalm_Death,[D@5c52973a)
      (en Mississippi:_The_Album,[D@32469030)
      (en Maureen_O%27Sullivan,[D@36a45919)
      (en Bobby_Bonds,[D@5fd59455)
      [0.012269333118518723,0.00931031069104092,0.007799035609358975,0.007163223653399205,0.007440597763679801,0.009663319198034202,0.014241960561180642,0.022018538786316405,0.02871822063160083,0.03397684949792524,0.03692234740314685,0.040403928632491655,0.04588864272065182,0.05911549966150395,0.07065539277085024,0.07819949145827094,0.08338027690901208,0.08472108052388344,0.08995381884458498,0.08318931756666045,0.07063565917745358,0.051388603966268986,0.03334483089582854,0.019599719958337758]
      (it Omicidio,[D@69f2fae4)
      (pl Dmitrij_Mendelejew,[D@4f35c513)
      (pl Jurij_Gagarin,[D@531d8b05)
      (fr Th%C3%A9%C3%A2tre_(genre_litt%C3%A9raire),[D@38921fe4)
      (de Naz%C4%B1m_Hikmet,[D@16bcec0)
      (tr G%C3%BCneydo%C4%9Fu_Anadolu_B%C3%B6lgesi,[D@4cd29ffa)
      (it Verismo,[D@1804fd7d)
      (pl El%C5%BCbieta_II,[D@6f9e25dc)
      (pl Karol_Linneusz,[D@77c7c7c3)
      (ro George_Bacovia,[D@5d0c10a4)
      [0.03663950541969362,0.035569572198196786,0.03556544362816141,0.0360334356481691,0.036571956664935956,0.037672266802240395,0.039086930148569184,0.09973513922348329,0.0388676466773104,0.037121377283648074,0.036553295487326255,0.03617382692022845,0.036839420726951885,0.03920298663076264,0.04167966960939662,0.04238362711054383,0.04219662732109017,0.04294739438329245,0.04324994386341364,0.042475852127857906,0.04298225315455466,0.04221999730253209,0.04038407368925317,0.03784775797838807]
      (en Stargate_SG-1_(season_10),[D@4e546712)
      (en Ghulam_Nabi,[D@73af6eb)
      (en Spellbinder_2:_Land_of_the_Dragon_Lord,[D@3028c202)
      (en Michel_Brown,[D@43d92a01)
      (commons.m File:Paracetamol_acetaminophen_500_mg_pills.jpg,[D@6654ca73)
      (en Christiane_Herbold,[D@48b77881)
      (ko %EC%99%95%EC%9D%98_%EB%82%A8%EC%9E%90,[D@5488273)
      (en Atomic_commit,[D@5b6c4eba)
      (en Kripo,[D@23a4c43a)
      (en Gun_laws_in_California,[D@164ece1e)
      [0.03801145094776703,0.036505565214841995,0.03522377866912081,0.03515170310256873,0.03474671125063014,0.034187550778383,0.03251077245007532,0.03198419033762471,0.03261385323836898,0.033749377263122673,0.03333242428193051,0.03403973845701895,0.03564059979394542,0.039204836866253784,0.04239708460839102,0.04418109932601992,0.04771057437225797,0.10535161980405387,0.049362381595800975,0.04695079073987059,0.046768276733304005,0.046088666900048036,0.04408511780599849,0.04020183546260279]
      (en Fuji-Servetto,[D@5ead3c53)
      (en Selections_for_Friends_-_Live_from:_Schubas_Tavern,_Chicago,_Montalvo_Winery,_Saratoga_California,[D@3f539db4)
      (en Chava_Alberstein,[D@a96b4e7)
      (en Carlos_Eduardo_de_Castro_Louren%C3%A7o,[D@2c3eaf1f)
      (en USS_San_Francisco_(CA-38),[D@475b32ea)
      (en Roma_in_Bulgaria,[D@346bc53f)
      (en Alt.sex,[D@f62c5fc)
      (en Bowden_cable,[D@1db42b48)
      (en Languages_of_Germany,[D@1f859334)
      (es Joshua_Bell,[D@7a834b16)
      [0.04058672266446782,0.03852360531318485,0.03767127409222684,0.037376318797599034,0.037117884262843986,0.03580455967524992,0.03342375949256313,0.03225046191048891,0.03197720301550508,0.032339941999755935,0.0320290970556327,0.03282254500105083,0.034561858462704556,0.03824272043151499,0.041410959690908924,0.04311182601149178,0.04481200951233985,0.04596697236259904,0.047290993809791085,0.04961315858702849,0.09182348391798593,0.050516759174329476,0.04712837908566507,0.043597505673071635]
      (en John_Taverner,[D@5147d895)
      (en Garth_Jennings,[D@43dc47e3)
      (en List_of_universities_in_Spain,[D@32f3aae3)
      (en Venetian_Arsenal,[D@1b583737)
      (en Nicotene,[D@3c9109e7)
      (en The_Thin_Blue_Line_(emblem),[D@56213bc1)
      (en Sun_City,_Arizona,[D@1fda2814)
      (en SS_Independence,[D@2b7d4962)
      (en Palestinian_Arabic,[D@2e5cd73c)
      (en Dunluce_Castle,[D@5136bbeb)
      [0.06153089970448505,0.053337282805189144,0.040700716926028904,0.03152484138025859,0.024098055723485702,0.01762141410180347,0.013099452492165806,0.011335298332672869,0.011768099062133382,0.013342389270590638,0.01431116652282229,0.0190657425314697,0.029210269692620736,0.0395017183324991,0.04984106780345634,0.051969671908192766,0.049164883220059345,0.0601694220975182,0.06980056092872877,0.07157576189717368,0.07049727519390377,0.06640982960998539,0.06507180251322046,0.06505237794953582]
      (en Tufted_Titmouse,[D@6d480717)
      (pt Celeron,[D@13c9af0e)
      (en Canine_degenerative_myelopathy,[D@1e03ca48)
      (pt.d exce%C3%A7%C3%A3o,[D@ad54c5d)
      (es Tocadiscos,[D@39887339)
      (pt Rey_Mysterio,_Jr.,[D@2d44c0b1)
      (es Medicina_complementaria_y_alternativa,[D@7cc6bbae)
      (pt John_Forbes_Nash,[D@5b4bdc81)
      (es Migraci%C3%B3n_de_las_aves,[D@5179c7fa)
      (pt Arte_da_Idade_M%C3%A9dia,[D@123817e0)
      [0.02048127447438117,0.026230959437722544,0.0317745283856621,0.03775967260199356,0.043089704963645184,0.04831313147456882,0.05184429011668281,0.05429181051047816,0.05870509336771364,0.06102333192003332,0.05857621432650617,0.05822822073364478,0.0696490638757112,0.0773762654556471,0.06681759316677474,0.05515847297043658,0.04330685191000553,0.03343882840136657,0.02473988142073411,0.018904654352964626,0.015464527971344259,0.014058891343567439,0.014423960441465905,0.01634277637694963]
      (ja 2%E9%87%8D%E5%8F%8D%E8%BB%A2%E3%83%97%E3%83%AD%E3%83%9A%E3%83%A9,[D@3da602f)
      (ja %E5%B0%8F%E8%B0%B7%E5%B9%B8%E5%BC%98,[D@75e0afa2)
      (ja P-3_(%E8%88%AA%E7%A9%BA%E6%A9%9F),[D@722af123)
      (ja %E7%84%A1%E9%87%8F%E5%A4%A7%E6%95%B0,[D@613d6b63)
      (ja Roman,[D@7556275)
      (ja Takuya,[D@76b2f315)
      (ja %E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB:Keikyu_linemap.svg,[D@5e752494)
      (ja TBF_(%E8%88%AA%E7%A9%BA%E6%A9%9F),[D@5cb4a0fe)
      (ja %E3%82%A2%E3%83%AC%E3%83%83%E3%82%AF%E3%82%B9%E3%83%BB%E3%83%9F%E3%83%A9%E3%83%BC,[D@56b5b33e)
      (ja %E3%83%AB%E3%82%A4%E3%83%BB%E3%83%B4%E3%82%A3%E3%83%88%E3%83%B3,[D@330ad7eb)
      [0.03630527308212861,0.035351826199753064,0.03440426391448079,0.0345120174567211,0.03426988700404493,0.03418840587738582,0.03363201610153393,0.03380478744801308,0.03496808831886133,0.036491946360231696,0.03702126672634711,0.039641270882926356,0.08892697472555411,0.04346255053317728,0.044171815225456,0.04493689553427321,0.045084329950127316,0.04475621786881656,0.045236955937591215,0.045476267575248114,0.045769626876290055,0.04519946526845384,0.04289326411885477,0.03949458701372971]
      (en Nautilus_Pompilius_(band),[D@41fb8e4)
      (en Pailin,[D@3ea382d9)
      (en New_Siberian_Islands,[D@60e394af)
      (fr El_Chocolate,[D@7d369eda)
      (en Animal_kingdom_(band),[D@cf73a6f)
      (en Telithromycin,[D@3b1d540f)
      (es Compresor,[D@279c31b)
      (en Electronic_arts,[D@7f822e2)
      (en italy,[D@33ba2784)
      (en Enforcer_(professional_wrestling),[D@27865a4)
      [0.038036174923793714,0.03177809026641383,0.032765649174974286,0.03803063239620409,0.03827285170520207,0.03382321803453648,0.04008388715471962,0.03405636164691777,0.03841851336822606,0.10250075288214837,0.04574104268132081,0.03724973946739301,0.0437285589300145,0.03911318790565904,0.04164278610072062,0.045752041320226144,0.04440499857208532,0.039552696392045485,0.04476279282430526,0.03837984690916248,0.03849981986264965,0.04023348351374696,0.03953260852001849,0.033640265447515666]
      (en Secret_Admirer,[D@17fede87)
      (en Intel_P35,[D@5dc37d5f)
      (en Apache_OFBiz,[D@4d5c1810)
      (en Carboprost,[D@592fea1d)
      (en File:Naturkundemuseum_Berlin_-_Archaeophis_proavus_Massalongo_-_Monte_Bolca.jpg,[D@58b343a9)
      (en Walking_with_dinosaurs,[D@611f39bd)
      (en Bharathiraja,[D@4907d91b)
      (en Nocturne_(band),[D@223ea112)
      (ru %D0%92%D1%81%D0%B5%D0%BC%D0%B8%D1%80%D0%BD%D0%B0%D1%8F_%D0%B2%D1%8B%D1%81%D1%82%D0%B0%D0%B2%D0%BA%D0%B0,[D@3dc0b64e)
      (en Category:Native_American_pornographic_film_actors,[D@3700da8e)
      [0.04090300503097149,0.04555589514136939,0.1129183522580837,0.04825586343452862,0.04134421376769555,0.041516636625761084,0.03492703279189913,0.031481148968786274,0.030567026198955963,0.029566153494031574,0.02895676161244145,0.029616650036482624,0.0316251447672392,0.03508524981070899,0.03861620613144132,0.03972846679387096,0.041412956225921525,0.04154714698472277,0.04243123406747227,0.04308078785549328,0.04401341900138539,0.04377516950472104,0.04237563342429848,0.04069984607171811]
      (en Pandamonium_(TV_series),[D@1bb8f1c7)
      (en Modernized_Load-Carrying_Equipment,[D@b094e09)
      (en Johnny_Lodden,[D@fe23688)
      (en Auma_Obama,[D@4c6a22dd)
      (en Anastasis,[D@4c17025c)
      (en Rules_of_engagement_(disambiguation),[D@7a9c0d0a)
      (en File:Gwen-Stefani.jpg,[D@1b1c4a69)
      (en File:Mkgold_01.jpg,[D@5e594166)
      (en Kelenna_Azubuike,[D@35d4bfee)
      (en Alvin_and_The_Chipmunks,[D@2284bdde)
      [0.19651284492420276,0.05547411843217102,0.041891273728624494,0.03936948026983286,0.03508899297753266,0.03144458099134731,0.028659454532982357,0.02685412441964628,0.02649382426578641,0.026407473630671675,0.025825529575447016,0.027280042998801726,0.029015863835219317,0.031590050713878874,0.03423283833109369,0.03545091179578571,0.03650117786941308,0.03727168328060116,0.03738406595912845,0.0372623054153245,0.037867030916692024,0.03874459742069808,0.039710862352949586,0.04366687136216897]
      (en Incest_(book),[D@73c536fd)
      (pt Intifada,[D@189d576c)
      (en Newport_Harbor:_The_Real_Orange_County,[D@1d68014)
      (en Category:Artists_who_committed_suicide,[D@2972a1fd)
      (en Miss_Peru,[D@24e8fe4d)
      (en Carrier_Air_Wing_Eight,[D@3c8f8429)
      (en Whacking_Day,[D@722dbece)
      (en Ken_Macha,[D@194a00f2)
      (en Vacant,[D@49841cd6)
      (en Champion_Jack_Dupree,[D@814b4f)
      [0.08049591280522013,0.074290542846375,0.06363619001122807,0.05154071418151759,0.03913654256267717,0.028051453500260268,0.019022203920135435,0.014315234378734002,0.01293595586239683,0.012419032409890354,0.012310481954508496,0.013724182503668933,0.01610656161539519,0.02078915248619662,0.02949644006511014,0.03931105652654023,0.044394369469514666,0.046919296673520405,0.04775006648318911,0.05153938369812783,0.05766400496708146,0.06605404129168732,0.07719276711680981,0.08090441267021488]
      (es Cocci%C3%B3n_del_pan,[D@5d968ccf)
      (es Compa%C3%B1%C3%ADa_multinacional,[D@46f69a09)
      (es Ballena_yubarta,[D@67fae944)
      (en Yvette_Nicole_Brown,[D@9e8a4b1)
      (es Lucy_Liu,[D@894658c)
      (es Sin%C3%B3nimo,[D@4696ceba)
      (es Micr%C3%B3fono,[D@56de398a)
      (es Cory_en_la_Casa_Blanca,[D@1793bf94)
      (es Anglicana,[D@650699a3)
      (es Pedro_Infante,[D@1450d026)
      [0.015697165556709136,0.011878835722746004,0.009744041020384021,0.008820474348730206,0.008669140864823987,0.010033462982603408,0.014839195477999306,0.02676094901628073,0.03908163327798332,0.04901911617885544,0.05450813363762468,0.05167323281877442,0.056904301363831886,0.07209267259698242,0.07630154998395933,0.07614249792377893,0.07040667383202165,0.06296765315294166,0.05988723704800751,0.05741380822110396,0.05727656911992629,0.049350001522022284,0.03696594031662722,0.02356571401528222]
      (fr NFFNSNC,[D@9ca377)
      (de Michael_Diekmann,[D@2bafd9f1)
      (de St%C3%A4dte_in_den_Vereinigten_Staaten,[D@260d739f)
      (de Mini_(BMW),[D@3d21df3d)
      (de Konjunktur,[D@7ac73d25)
      (it Morte,[D@2e7f871c)
      (en Mental_Health_Act_1983,[D@367f571d)
      (it Emorragia_cerebrale,[D@62304cde)
      (de Cappuccino,[D@5106b52e)
      (de Salami,[D@f610891)
      [0.039719552052528306,0.039862583648347744,0.04125387359069873,0.04366450454144857,0.04459111437772066,0.04262255041112303,0.043524692079272356,0.04299279667341874,0.04302271965315555,0.04226519183434494,0.04113338046493871,0.040744170073703935,0.039921694660290684,0.04238894593466977,0.042978982418328794,0.04271332517072928,0.041898908929371266,0.04216778509618619,0.04042197849333902,0.040669934326811444,0.04026936457386517,0.04089658592374294,0.040424676418899366,0.03985068865306463]
      (en Dick_Fuld,[D@486d80af)
      (en Recollection,[D@d1b864f)
      (en Passenger_rail_transport_in_China,[D@a0eb342)
      (en Len_Bosack,[D@4b1ab736)
      (en Breville,[D@46f260bf)
      (en Adi_Dharm,[D@52adbffc)
      (en Hayato_Gokudera,[D@d1763cc)
      (ja %E8%8D%92%E4%BA%95%E3%81%BE%E3%81%A9%E3%81%8B,[D@7a98ecca)
      (en Ultratop_50_number-one_hits_of_1996,[D@4de200a7)
      (en Original_antigenic_sin,[D@3098fbe3)
      [0.024584003835707722,0.03392769437330905,0.04122931348233933,0.04904874699121367,0.050620238349517226,0.058631873853356435,0.06331664501565697,0.06574658407609807,0.06756301434648505,0.06398469955090175,0.0549411005356533,0.048682238932382674,0.050280144050328636,0.05327732259383587,0.05177097213374066,0.04660302482739635,0.03792278507020614,0.029602233016373627,0.022829320468379936,0.01871124464774886,0.016284428906012685,0.01552662642690375,0.01639632124276355,0.01851942327368879]
      (ja %E8%A5%BF%E5%B7%9D%E7%BE%8E%E5%92%8C,[D@4f02e8b5)
      (ja %E7%A5%9D%E6%97%A5%E6%B3%95,[D@47fbeb99)
      (ja %E3%82%A8%E3%83%AA%E3%83%BC%E3%82%BC%E3%81%AE%E3%81%9F%E3%82%81%E3%81%AB,[D@6c28aeba)
      (ja UFJ%E3%82%AB%E3%83%BC%E3%83%89,[D@62e76ccf)
      (ja %E5%87%BA%E5%85%A5%E5%9B%BD%E7%AE%A1%E7%90%86%E5%8F%8A%E3%81%B3%E9%9B%A3%E6%B0%91%E8%AA%8D%E5%AE%9A%E6%B3%95,[D@4798bcc3)
      (ja %E3%83%A9%E3%82%A4%E3%83%96%E3%83%89%E3%82%A2%E3%83%BB%E3%82%B7%E3%83%A7%E3%83%83%E3%82%AF,[D@7c90caed)
      (ja %E7%AE%B1%E6%A0%B9%E7%94%BA,[D@5facd7f)
      (ja %E4%B9%99%E9%BB%92%E3%81%88%E3%82%8A,[D@14c94fea)
      (ja %E3%82%B3%E3%83%BC%E3%83%B3%E3%82%B9%E3%82%BF%E3%83%BC%E3%83%81,[D@2c9d964f)
      (ja %E3%83%9C%E3%82%BF%E3%83%B3%E5%9E%8B%E9%9B%BB%E6%B1%A0,[D@1df7336b)
      [0.02631360207470874,0.022393182268970517,0.019994811074366674,0.018801531961717896,0.018337734930772637,0.01816060891630173,0.019383541392722452,0.021855800763520252,0.02439779919663121,0.026481980048644386,0.028081352618943723,0.02931323966624559,0.03148019366363638,0.03511950511750645,0.035230578920193684,0.03693109097099796,0.03867094235428024,0.039897261076975714,0.04381098316406143,0.07536573053366573,0.20995472641374083,0.09147808266317072,0.051418961191601796,0.037126759016623254]
      (en Roderick_Bradley,[D@49b03ede)
      (de Jos%C3%A9_Bosingwa,[D@51ef869d)
      (en Gibbs,[D@446ed771)
      (it Winnipeg,[D@49025ed0)
      (pl Zebrzydowice_(wojew%C3%B3dztwo_%C5%9Bl%C4%85skie),[D@3a9ecf22)
      (en Category:1987_deaths,[D@1030df3f)
      (it Solo_due_ore,[D@7890a966)
      (en Eastern_Samar,[D@6161a560)
      (en Rev_Run,[D@51b07392)
      (en Category:Gemstones,[D@1773a024)
      [0.029510340646833704,0.03223935445185962,0.03537285377026975,0.03716868678448816,0.05492262998265673,0.194329865059025,0.058686821086128584,0.04055105606412972,0.0373581041187666,0.03357018530338906,0.03040344382040677,0.029458910876985912,0.03133819509778805,0.0327441229382069,0.03571911132486286,0.03542593340148303,0.034716866833085745,0.033390923039271665,0.03139426920585797,0.031087259682647892,0.030971759698327935,0.03075785468374756,0.029761666844933286,0.029119785284847672]
      (en Brazilian_films_of_2008,[D@60c0f5)
      (en Ace_of_Cakes,[D@501c1504)
      (en Chloroplatinic_acid,[D@63a7e2b2)
      (en Delivery_order,[D@2314473b)
      (en Zero_Day_(film),[D@850847b)
      (en Category:Songs_about_California,[D@5e29ab80)
      (en Texaco_Star_Theater,[D@1ba90cc)
      (en List_of_Thunderbolts_members,[D@7f7841f6)
      (en Urs_Graf,[D@3032bedb)
      (fr Oryx,[D@53903495)
      [0.04102789149030779,0.03922389874088759,0.03787465666892647,0.03779938511138804,0.03760130566904017,0.03585617464720992,0.034009893430898636,0.032314009472563465,0.03183771865302027,0.03196836907762991,0.03193207029726911,0.03285794223745986,0.03443886568162612,0.037760684694087124,0.04116037288170978,0.04336181790548545,0.04479312056392423,0.045855047114626735,0.046842565894062854,0.04835250504850499,0.050626431679390384,0.08857310938357082,0.049231110185560804,0.044701053470849385]
      (en Cleaver_(The_Sopranos),[D@2efc4e0e)
      (es 18_de_abril,[D@3668913b)
      (en Nicotina,[D@b16d465)
      (en File:Gray557.png,[D@7d23893)
      (en Deep_Water_Slang_V2.0,[D@7a803ee7)
      (en Treatment_of_Crohn%27s_disease,[D@79a1ee85)
      (en Crawler-transporter,[D@76329c25)
      (es Rifle,[D@74a97dc2)
      (en File:Ibexes.jpg,[D@237f8764)
      (en Laura_Palmer,[D@991c871)
      [0.03977066840248162,0.04043527334839897,0.04345812966134165,0.1166092384823452,0.04547523723441136,0.03960501446063052,0.037993636097551846,0.03387536979743335,0.03156018960889398,0.030161068878659963,0.029920397016710187,0.02980376714556209,0.03128609357877073,0.03542335670452361,0.038170788356492395,0.04013327793461594,0.040537939067923356,0.04177858071813292,0.04289161283936683,0.042562125154098726,0.04342754539054656,0.04341950871168668,0.04184711116424363,0.03985407024517781]
      (en Joe_B._Hall,[D@7f4f88cb)
      (en Stephanie_Nadolny,[D@3e9e08b9)
      (en Superfast,[D@794ec9cc)
      (en Netherlands_men%27s_national_ice_hockey_team,[D@23a2ae4c)
      (en Podocarpaceae,[D@d6111db)
      (en Real_World/Road_Rules_Challenge:_The_Duel,[D@5fdb47b9)
      (en Crisis_Core,[D@3541f3d9)
      (en Zeppelin_LZ1,[D@7cf046df)
      (en Redeemer_(Norma_Jean_album),[D@2bd1a8e)
      (en Incisive_foramen,[D@4d1a6209)
      [0.038011374975027136,0.03652704879920949,0.03534950343920852,0.03531288278975522,0.03470775441702596,0.03344606074802809,0.0317446255013029,0.031141605558571546,0.031231354638799547,0.031896884378301876,0.03212502278397296,0.032763943162889136,0.03469458802797162,0.03805258777069096,0.040871206133805994,0.04279986480438668,0.044637214938814306,0.0462193633686289,0.049726248432027456,0.1099187811213301,0.052588139285522445,0.04883964959936623,0.04572157938186428,0.041672715943498695]
      (en Underground_(soundtrack),[D@6aba1b2)
      (en Lechitic_languages,[D@6e04b089)
      (en Music_of_Switzerland,[D@75e22acc)
      (en The_Rachel_Papers,[D@5367f38a)
      (en American_Kickboxing_Academy,[D@4da613cd)
      (en Men_of_Mathematics,[D@525e1f8e)
      (es Chris_Novoselic,[D@2169cde1)
      (en List_of_national_libraries,[D@294b7fea)
      (en Cathedral_and_John_Connon_School,[D@1bbaf0d4)
      (en Robert_Pape,[D@e18fc85)
      [0.033747780061727184,0.03299445351177026,0.03224094799567083,0.03230138663058843,0.032932809610576826,0.03353607790972506,0.03398474051157381,0.03497337995797068,0.03604900498625534,0.03860150259142391,0.040929893115044955,0.1083553409952662,0.04492343000376752,0.04422249141780801,0.04499383533891488,0.04452691213706421,0.04456435922446962,0.04323899243545191,0.04257173101995972,0.042132097554361314,0.04215879027883677,0.040896990175507096,0.039191931261149095,0.03593112127511652]
      (en Tourism_in_Zimbabwe,[D@6fe411db)
      (en International_Standard_Bibliographic_Description,[D@66507a40)
      (en Motion_simulator,[D@2d8a343a)
      (en Ghostfest,[D@4c6ae331)
      (en Absorption_(economics),[D@7d69cd28)
      (en Christiane_F,[D@2d1c3d6b)
      (commons.m File:Vel%C3%A1zquez_Venus.jpg,[D@190452f0)
      (ja BD-J,[D@6ae11764)
      (en Everaldo_Coelho,[D@5fc6dd0d)
      (en Christopher_Powell,[D@78dab19a)
      [0.03889205305754244,0.03756286072466983,0.03687716334689762,0.036776562658031815,0.036492991287504224,0.03543372804882056,0.03351877263491065,0.0327747995173534,0.03308582338660118,0.03369521660948239,0.03374555410839795,0.03429605328467838,0.036002884173217405,0.039519475683129815,0.04293879464437088,0.045807939964173254,0.08985007327656452,0.04813465968388507,0.04700204659192288,0.047360753556636416,0.047005334178594003,0.04708262488974583,0.04485101712746748,0.04129281756540193]
      (en The_Old_Regime_and_the_Revolution,[D@216c226d)
      (en V%C3%A9hicule_de_l%27Avant_Blind%C3%A9,[D@4248333f)
      (en Beneath_the_Sky,[D@121c8f18)
      (en Vacuum_engineering,[D@10f679f2)
      (en Ethics_of_artificial_intelligence,[D@25c00791)
      (es ExFAT,[D@6230d3f3)
      (en George,_Duke_of_Clarence,[D@73ab28e1)
      (en ASM_International_(company),[D@6a09071a)
      (en Bronx_(cocktail),[D@76b17258)
      (en Emerald_Necklace,[D@7bca2c48)
      [0.03596235302742407,0.03458700385349975,0.03459790522177838,0.03425679208363687,0.03344808325114071,0.03310101951285792,0.0323218178809635,0.03231062939650359,0.03280140662934504,0.03395841916296418,0.03457986407294768,0.035145231774253764,0.03681723819259251,0.04061152714508384,0.04533615055667245,0.11462211301193091,0.049742897154354836,0.04602583620519788,0.04522578081583296,0.0451914445764026,0.045023331841765775,0.0440230224883876,0.04193117746208828,0.03837895468237477]
      (en File:Saint_lucia_mountain_resort.JPG,[D@74e694c0)
      (en Lebesgue_measure,[D@4e300535)
      (en Carey_Mercer,[D@32a602ad)
      (en Four_Seasons_Hotel_New_York,[D@30f9c5d5)
      (en Juliet_Schor,[D@5e2b3c11)
      (en Tim_Tam_Slam,[D@6874b063)
      (en M829_(munition),[D@44e86928)
      (en Tomica,[D@764fe46f)
      (en Arthur_Guinness,[D@771e7017)
      (en List_of_spiders_of_India,[D@6f513319)
      [0.04262865403357803,0.040340141712830346,0.038085858685232815,0.036772485478517074,0.03405069471493207,0.03076429520769338,0.026043322157929226,0.023861936593456458,0.023250004018456585,0.023991523315212464,0.024119700513380787,0.02552216099065218,0.02930422866689119,0.038014508350075006,0.04858094035899266,0.05576829600881234,0.06002591280261374,0.06121323940936413,0.0616968159757828,0.062068091400072536,0.059027897867273825,0.05661929965044827,0.05219637621904915,0.046053615868752805]
      (en The_Good_Girl_Gone_Bad_Tour,[D@34e605d9)
      (en Queue,[D@698a04b0)
      (en Keystone_(architecture),[D@6439f4f1)
      (en Jason_&_the_Scorchers,[D@19002d9f)
      (en Carvedilol,[D@5ab32e8b)
      (en Superhero,[D@29b4d632)
      (en File:Elvis%27_tomb.jpg,[D@1f5a8202)
      (en North_Pacific_Giant_Octopus,[D@6f0d6162)
      (en Portal:Anime_and_manga,[D@5774a1e3)
      (en File:Discovery_of_the_Mississippi.jpg,[D@2a306af0)
      [0.05406727617836943,0.054572376096413376,0.05550648983662688,0.057390298190645755,0.05572300598232512,0.05033509519571247,0.041836607720879423,0.033294282658611674,0.027286591494284175,0.023985661955932745,0.02206008833388276,0.021823602638838688,0.0240534306039036,0.028775560374153603,0.03324997434766627,0.03649290420047818,0.0403508045888445,0.04257993459214769,0.04459527615831335,0.046330236857884635,0.049028654191779805,0.052252245285199485,0.052590122454385956,0.051819480062720163]
      (en File:Evstafiev-bosnia-sarajevo-serbs-toast.jpg,[D@37acfc7a)
      (en The_Sandman_(DC_Comics/Vertigo),[D@3df3ca24)
      (en Perfect_Strangers_(TV_series),[D@505c2142)
      (es Extracci%C3%B3n_l%C3%ADquido-l%C3%ADquido,[D@508b9d45)
      (en Facial_(sex_act),[D@7e024652)
      (en American_Juniors,[D@4756f21f)
      (en George_Wallace,[D@1d31f5d0)
      (en File:Arnold_Schwarzenegger_sexual_harassment_protestors750.jpg,[D@3b0139a9)
      (en Trojan_(condoms),[D@4d70a482)
      (en Koichi_Kimura,[D@1e407a86)
      [0.01503366096986348,0.012781526460462756,0.012065357175499085,0.01261840001735856,0.014572022607009289,0.019040777897473347,0.027258104377625825,0.03783561552294416,0.04501739943098403,0.049271468147765916,0.0519514764131547,0.0573470433361471,0.06197497142743064,0.06295306909084239,0.06081979505159612,0.058510475667165364,0.061988973203836284,0.06386640752892177,0.06817001056769906,0.06459715209164817,0.0542170825241415,0.040002986348948597,0.02848354115943362,0.01962268298204822]
      (ru %D0%A2%D1%83%D0%B9%D0%BE%D0%BD,[D@35c91014)
      (de Fusion_(Wirtschaft),[D@233eaca2)
      (ru %D0%A5%D0%B0%D0%BD%D0%BA%D0%B0,[D@67b7d1c1)
      (ru %D0%90%D0%BF%D0%BE%D0%BA%D1%80%D0%B8%D1%84,[D@552c0b19)
      (pl Agenda_21,[D@3e8cc1fe)
      (ru %D0%91%D1%80%D0%B8%D1%82%D0%B0%D0%BD%D1%81%D0%BA%D0%B8%D0%B5_%D0%92%D0%B8%D1%80%D0%B3%D0%B8%D0%BD%D1%81%D0%BA%D0%B8%D0%B5_%D0%BE%D1%81%D1%82%D1%80%D0%BE%D0%B2%D0%B0,[D@b142ac7)
      (ar %D9%81%D9%88%D8%A7%D9%83%D9%87,[D@5904747f)
      (sk Schizofr%C3%A9nia,[D@337c6b81)
      (ru %EC%EE%F2%E8%E2%E0%F6%E8%FF,[D@2d75fc8f)
      (ru %D0%9A%D0%BE%D1%8D%D0%BB%D1%8C%D0%BE,[D@1d18279a)
      [0.031778949968226805,0.025992481446089406,0.022789246442960157,0.021452772508060287,0.020704762344453977,0.02073589926813595,0.02146986683621215,0.025094559295104714,0.03083076437470682,0.038694694330820636,0.043111955864607865,0.046293605367723464,0.049954448260157484,0.054159328570719645,0.05743762642179867,0.05806715464333817,0.0593563680198277,0.055709049069217066,0.05299861551593601,0.0527636800116166,0.05601714174007668,0.057563223213369236,0.05509496938380394,0.04192883710303631]
      (fr Alain_Duhamel,[D@f7bc4bc)
      (es Stamford_Bridge,[D@6b11a95)
      (en Santa_Fe_Institute,[D@55482fa1)
      (en BBC_Online,[D@762f1ec4)
      (en White_City_Stadium,[D@f8d127a)
      (en Magdalene_College,_Cambridge,[D@76b80f8f)
      (en Haworth,[D@2e05b22d)
      (en List_of_Earth_observation_satellites,[D@77f4bff5)
      (en Robert_Louis_Stevenson,[D@6566ab59)
      (en Carl_Wayne,[D@377b9411)
      [0.020986202887374784,0.025913825372300803,0.030672828343384854,0.03645337591173674,0.04267166124516336,0.04801175457525539,0.05087601811877647,0.05215330615770096,0.05441464427230214,0.05451875896304066,0.05095763592661906,0.049020515888368255,0.05473453703891255,0.06194076765541331,0.06864671334958238,0.06895577681833141,0.057625261350604896,0.04491572509462866,0.0321922402702132,0.0237242026974784,0.019034137702175304,0.016829419659030946,0.016704590374054158,0.018046100327551252]
      (ja DOUBLE,[D@2e97dbd)
      (ja %E3%82%B0%E3%83%AC%E3%82%A2%E3%83%A0%E3%83%BB%E3%83%A4%E3%83%B3%E3%82%B0,[D@2f3e04c9)
      (ja %E5%86%A5%E7%8E%8B%E6%98%9F,[D@10ac405a)
      (ja %E7%A7%8B%E8%8F%9C%E9%87%8C%E5%AD%90,[D@1cb4dd43)
      (ja %E3%83%8D%E3%82%BF,[D@262a686e)
      (ja %E3%83%AD%E3%83%AA%E3%83%BC%E3%82%BF_(1997%E5%B9%B4%E3%81%AE%E6%98%A0%E7%94%BB),[D@2a2e2155)
      (en Special:Export/Mocxi,_Coithienthai,_Tinhdonphuong,_Aitinhviet,vietvuive,vietfun,x_...,[D@3a8e6915)
      (ja %E9%87%91%E7%94%B0%E4%B8%80%E5%B0%91%E5%B9%B4%E3%81%AE%E4%BA%8B%E4%BB%B6%E7%B0%BF_(%E3%82%A2%E3%83%8B%E3%83%A1),[D@5b94ffbb)
      (ja %E3%83%80%E3%83%B3%E3%83%87%E3%82%A3,[D@13217cf6)
      (ja %E3%82%AA%E3%83%89%E3%83%AC%E3%82%A4%E3%83%BB%E3%83%88%E3%83%88%E3%82%A5,[D@7b930449)
      [0.02105278737711105,0.024065954129197053,0.02485349238047342,0.02517359365566407,0.025368991523431167,0.026346853304540542,0.027758925634391432,0.029320199392904894,0.030410586468375616,0.03323029097475547,0.034994886669638126,0.048879150764495276,0.2596917335959298,0.06789655638560067,0.0475584009431524,0.04234181717232831,0.03813238823373898,0.03320784394192066,0.03095308140893449,0.029362040986054376,0.026077684093871494,0.025055343846389466,0.02465365916353646,0.02361373795356453]
      (en Wil_Anderson,[D@365fb621)
      (en Serbian_Australian,[D@45ae4c6a)
      (en Coconut_Records_(band),[D@361ae6e3)
      (ja 1%E5%84%84%E4%BA%BA%E3%81%AE%E5%A4%A7%E8%B3%AA%E5%95%8F!%3F%E7%AC%91%E3%81%A3%E3%81%A6%E3%82%B3%E3%83%A9%E3%81%88%E3%81%A6!,[D@202c5cd5)
      (ja %E9%81%A5%E6%B4%8B%E5%AD%90,[D@4082b473)
      (en GONZO,[D@45150b1b)
      (ja %E3%83%80%E3%83%B3%E3%83%BB%E3%82%B8%E3%83%A7%E3%83%B3%E3%82%BD%E3%83%B3,[D@688c3d0b)
      (en Catostomidae,[D@4efbd2cc)
      (ja %E3%81%B2%E3%81%A8%E3%82%8A%E3%81%A7%E3%81%A7%E3%81%8D%E3%82%8B%E3%82%82%E3%82%93!,[D@76cc7796)
      (ja %E5%AE%89%E6%B0%B8%E6%B2%99%E9%83%BD%E5%AD%90,[D@69bf55c8)
      [0.034933882202940754,0.03393847352075685,0.03367474547908002,0.0350932049873991,0.035091652145168906,0.036110885706401856,0.036247809652790904,0.03919790127275771,0.10563857585418154,0.04163491525969161,0.0382802762380753,0.037902423506832275,0.03883112399347803,0.041259346328431604,0.04257982683424227,0.04359608653761666,0.043498773790812795,0.042188134848473134,0.04176698591171844,0.041714121366946535,0.041122233484715255,0.04076309932747684,0.03863508678612679,0.03630043496388475]
      (en University_of_the_Basque_Country,[D@1173d053)
      (en Peter_&_Gordon,[D@4a720218)
      (commons.m User:TwoWings/Fav,[D@2e79bd7)
      (en File:The-Smiths-cover.png,[D@33a841be)
      (en List_of_TVXQ_awards,[D@6b743b70)
      (en Gary_Hill,[D@1bea6d2d)
      (en Canadian_Automobile_Association,[D@3ba5b3ac)
      (en Special:Export/Cerita_Ngentot_Tante,[D@7bb6ffcb)
      (en WAYN,[D@2b53a7e1)
      (en List_of_Nip/Tuck_characters,[D@132bb00)
      [0.08301479674008273,0.0428025782881054,0.040834132943243964,0.0398922504423481,0.03881562901449699,0.03686217503379328,0.03413840277924007,0.03226752744213802,0.03154600511402901,0.031589674185788966,0.0313202914543305,0.03182303363503269,0.033704570164503074,0.03713513706191798,0.04068090696584198,0.04263496256231653,0.043719813137115614,0.0450913991119608,0.04647655665492104,0.047079659610252934,0.047638274871477315,0.04842503086101897,0.04714201644008733,0.04536517548595648]
      (en Vice_Chief_of_Naval_Operations,[D@7a5e2929)
      (en It%27s_a_Big_Big_World,[D@3be08182)
      (en Memphis_Championship_Wrestling,[D@10569c84)
      (en KUSA-TV,[D@263da67d)
      (es Mediterr%C3%A1neo,[D@197d6c28)
      (en Kingdom_of_France,[D@7947c32a)
      (es Palas_(mitolog%C3%ADa),[D@565e389a)
      (en Sin_Tetas_No_Hay_Para%C3%ADso,[D@43d632c2)
      (en XBCD,[D@239e8159)
      (en Chris_Tranchell,[D@7b3e0c53)
      [0.027789188595911403,0.02599470817940511,0.02623305138480848,0.02738843373573372,0.02906657698435462,0.03230052067518913,0.035553923824055214,0.04061131563372411,0.04498598056015135,0.05022777108173962,0.05085360591347905,0.05005568683224906,0.04930529270810237,0.05306853665580836,0.057210323275603575,0.05520943247887833,0.05272752381657416,0.04799192919202935,0.04563505746359424,0.045877857651690564,0.044163688036013934,0.04119753119327842,0.03584987904157225,0.030702185086053446]
      (ru %D0%9A%D0%BE%D0%BD%D1%82%D0%B5%D0%B9%D0%BD%D0%B5%D1%80%D0%BE%D0%B2%D0%BE%D0%B7,[D@5f729832)
      (ru %D0%92%D0%BE%D1%82%D0%BA%D0%B8%D0%BD%D1%81%D0%BA%D0%B8%D0%B9_%D0%B7%D0%B0%D0%B2%D0%BE%D0%B4,[D@5c662b92)
      (en Lobengula,[D@37b6ada2)
      (zh %E5%BD%AD%E4%BA%8E%E6%99%8F,[D@3a418341)
      (de Wikipedia:Meinungsbilder/Automatische_Vergabe_des_Adminstatus,[D@23103e48)
      (commons.m Category:Wax_play,[D@7f68faf3)
      (en Machine_code,[D@4533a8de)
      (en Wood,[D@4293aa50)
      (en Plantronics,[D@6c928c55)
      (en Satellite,[D@93b5823)
   </textarea>

   </div>
   </div>
   </div>
   </div>
