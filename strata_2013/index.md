---
layout: global
title: AMP Camp Two - Big Data Bootcamp Strata 2013
---
# Introduction

Welcome to the hands-on tutorial in our Strata 2013 one-day AMP Camp Bootcamp on Big Data.
In addition to the amazing O'Reilly Strata folks, this event has been organized by Professors and PhD students in the UC Berkeley AMPLab.

This tutorial consists of a series of exercises that will have you working directly with components of our open-source software stack, called the Berkeley Data Analytics Stack (BDAS), that have been released and are ready to use.
These components include [Spark](http://spark-project.org), Spark Streaming, and [Shark](http://shark.cs.berkeley.edu).
We have already spun up a 4-node EC2 Cluster for you with the software preinstalled, which you will be using to load and analyze a real Wikipedia dataset.
We will begin with simple interactive analysis techniques at the Spark and Shark shells, then progress to writing standalone programs with Spark Streaming, and finish by implementing some more advanced machine learning algorithms to incorporate into your analysis.

## Tutorial Developer Prerequisites
This tutorial is meant to be hands-on introduction to Spark, Spark Streaming, and Shark. While Shark supports a simplified version of SQL, Spark and Spark Streaming both support multiple languages. For the sections about Spark and Spark Streaming, the tutorial allows you to choose which language you want to use as you follow along and gain experience with the tools. The following table shows which languages this tutorial supports for each section. You are welcome to mix and match languages depending on your preferences and interests.

<center>
<style type="text/css">
table td, table th {
  padding: 5px;
}
</style>
<table class="bordered">
<thead>
<tr>
  <th>Section</th>
    <th><img src="img/scala-sm.png"/></th>
    <th><img src="img/java-sm.png"/></th>
    <th><img src="img/python-sm.png"/>
  </th>
</tr>
</thead><tbody>
<tr>
  <td>Spark Interactive</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
  <td class="yes">yes</td>
</tr><tr>
  <td>Shark (SQL)</td>
  <td colspan="3" class="yes">All SQL</td>
</tr><tr>
  <td>Spark Streaming</td>
  <td class="yes">yes</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
</tr><tr>
  <td class="dimmed"><b>Optional:</b> Machine Learning :: featurization</td>
  <td class="dimmed yes">yes</td>
  <td class="dimmed no">no</td>
  <td class="dimmed yes">yes</td>
</tr><tr>
  <td>Machine Learning :: K-Means</td>
  <td class="yes">yes</td>
  <td class="yes">yes</td>
  <td class="yes">yes</td>
</tr>
</tbody>
</table>
</center>

# Launching a Spark/Shark Cluster on EC2

For the Strata tutorial, we have provided you with login credentials for an existing EC2 cluster.
If, for some reason, you want to to launch your own cluster using your own EC2 credentials (after the event, for example), follow [these instructions](launching-a-cluster.html).

# Logging into the Cluster

<ul class="nav nav-tabs" data-tabs="tabs">
  <li class="active"><a data-toggle="tab" href="#login_linux">Linux, Cygwin, or OS X</a></li>
  <li><a data-toggle="tab" href="#login_windows">Windows</a></li>
</ul>

<div class="tab-content">
<div class="tab-pane active" id="login_linux" markdown="1">
Log into your cluster via

    ssh -i <key_file> root@<master_node_hostname>

where `key_file` here is the private keyfile that was given to you by the tutorial instructors (or your AWS EC2 keyfile if you spun up your own cluster).

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

__Answer:__ Run this command, then try to log in again:

    chmod 600 ../ampcamp.pem
</div>
<div class="tab-pane" id="login_windows" markdown="1">
You can use [PuTTY](http://www.putty.org/) to log into the cluster from Windows.

1. Download PuTTY from [here](http://the.earth.li/~sgtatham/putty/latest/x86/putty.exe).

2. Start PuTTY and enter the hostname that was mailed to you, as shown in the screenshot below.

   ![Enter username in PuTTY](img/putty-host.png)

3. Click on Connection > Data in the Category area and enter `root` as the username

   ![Enter login in PuTTY](img/putty-login.png)

4. Click on Connection > SSH > Auth in the Category area and enter the path to the private key file (`ampcamp-all.ppk`) that was sent to you by mail.

   ![Enter login in PuTTY](img/putty-private-key.png)

5. Click on Open
</div>
</div>

# Overview Of The Exercises
The exercises in this tutorial are divided into sections designed to give a hands-on experience with Spark, Shark and Spark Streaming.
For Spark, we will walk you through using the Spark shell for interactive exploration of data. You have the choice of doing the exercises using Scala or using Python.
For Shark, you will be using SQL in the Shark console to interactively explore the same data.
For Spark Streaming, we will walk you through writing stand alone Spark programs in Scala to processing Twitter's sample stream of tweets.
Finally, you will have to complete a complex machine learning exercise which will test your understanding of Spark.

## Cluster Details
Your cluster contains 6 m1.xlarge Amazon EC2 nodes.
One of these 6 nodes is the master node, responsible for scheduling tasks as well as maintaining the HDFS metadata (a.k.a. HDFS name node).
The other 5 are the slave nodes on which tasks are actually executed.
You will mainly interact with the master node.
If you haven't already, let's ssh onto the master node (see instructions above).

Once you've used SSH to log into the master, run the `ls` command and you will see a number of directories.
Some of the more important ones are listed below:

- Templates for exercises:
   - `kmeans`: Template for the k-means clustering exercise
   - `streaming`: Standalone program for Spark Streaming exercises
   - `java-app-template`: Template for standalone Spark programs written in Java
   - `scala-app-template`: Template for standalone Spark programs written in Scala
   - `shark-0.2`: Shark installation
   - `spark`: Spark installation

- Useful scripts/documentation:
   - `mesos-ec2`: Suite of scripts to manage Mesos on EC2
   - `spark-ec2`: Suite of scripts to manage Spark on EC2
   - `training`: Documentation and code used for training exercises

- Infrastructure:
   - `ephemeral-hdfs`: Hadoop installation
   - `scala-2.9.2`: Scala installation
   - `hive-0.9.0-bin`: Hive installation
   - `mesos`: Mesos installation

You can find a list of your 5 slave nodes in spark-ec2/slaves:

    cat spark-ec2/slaves

For stand-alone Spark programs, you will have to know the Spark cluster URL. You can find that in spark-ec2/cluster-url:

    cat spark-ec2/cluster-url

## Dataset For Exploration
Your HDFS cluster should come preloaded with 20GB of Wikipedia traffic statistics data obtained from http://aws.amazon.com/datasets/4182 .
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

# Introduction to the Scala Shell
This short exercise will teach you the basics of using the Scala shell and introduce you to functional programming with collections.

If you're already comfortable with Scala or plan on using the Python shell for the interactive Spark sections of this tutorial, skip ahead to the next section.

This exercise is based on a great tutorial, <a href="http://www.artima.com/scalazine/articles/steps.html" target="_blank">First Steps to Scala</a>.
However, reading through that whole tutorial and trying the examples at the console may take considerable time, so we will provide a basic introduction to the Scala shell here. Do as much as you feel you need (in particular you might want to skip the final "bonus" question).

1. Launch the Scala console by typing:

   ~~~
   scala
   ~~~

1. Declare a list of integers as a variable called "myNumbers".

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> val myNumbers = List(1, 2, 5, 4, 7, 3)
   myNumbers: List[Int] = List(1, 2, 5, 4, 7, 3)
   </pre>
   </div>

1. Declare a function, `cube`, that computes the cube (third power) of an Int.
   See steps 2-4 of First Steps to Scala.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> def cube(a: Int): Int = a * a * a
   cube: (a: Int)Int
   </pre>
   </div>

1. Apply the function to `myNumbers` using the `map` function. Hint: read about the `map` function in <a href="http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List" target="_blank">the Scala List API</a> and also in Table 1 about halfway through the <a href="http://www.artima.com/scalazine/articles/steps.html" target="_blank">First Steps to Scala</a> tutorial.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> myNumbers.map(x => cube(x))
          res: List[Int] = List(1, 8, 125, 64, 343, 27)
          // Scala also provides some shorthand ways of writing this:
          // myNumbers.map(cube(_))
          // myNumbers.map(cube)
   </pre>
   </div>

1. Then also try writing the function inline in a `map` call, using closure notation.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> myNumbers.map{x => x * x * x}
   res: List[Int] = List(1, 8, 125, 64, 343, 27)
   </pre>
   </div>

1. Define a `factorial` function that computes n! = 1 * 2 * ... * n given input n.
   You can use either a loop or recursion, in our solution we use recursion (see steps 5-7 of <a href="http://www.artima.com/scalazine/articles/steps.html" target="_blank">First Steps to Scala</a>).
   Then compute the sum of factorials in `myNumbers`. Hint: check out the `sum` function in <a href="http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List" target="_blank">the Scala List API</a>.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> def factorial(n:Int):Int = if (n==0) 1 else n * factorial(n-1) // From http://bit.ly/b2sVKI
   factorial: (Int)Int
   scala&gt; myNumbers.map(factorial).sum
   res: Int = 5193
   </pre>
   </div>

1. <i>**BONUS QUESTION.** This is a more challenging task and may require 10 minutes or more to complete, so you should consider skipping it depending on your timing so far.</i> Do a wordcount of a textfile. More specifically, create and populate a Map with words as keys and counts of the number of occurrences of the word as values.

   You can load a text file as an array of lines as shown below:

   <pre class="prettyprint lang-scala linenums">
   import scala.io.Source
   val lines = Source.fromFile("/root/mesos/README").getLines.toArray
   </pre>

   Then, instantiate a `collection.mutable.HashMap[String,Int]` and use functional methods to populate it with wordcounts. Hint, in our solution, which is inspired by <a href="http://bit.ly/6mhGvo" target="_blank">this solution online</a>, we use <a href="http://richard.dallaway.com/in-praise-of-flatmap" target="_blank">`flatMap`</a> and then `map`.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> import scala.io.Source
   import scala.io.Source

   scala> val  lines = Source.fromFile("/root/mesos/README").getLines.toArray
   lines: Array[String] = Array(hlep, help, plot, set terminal pdf, pdf, set terminal pdf color, set output "monolithic-utilization-thru-day.pdf", set terminal pdf enhanced color, set terminal pdf monochrome, set terminal pdf mono, set output "graph.pdf", set terminal x11 enhanced color, help terminal, x11, quit, plot "utilization" using 1:2, plot "utilization" using 1/2:2, #plot "utilization" using 1:($2/), RadDeg, x!, Inv, sin, ln, π, cos, log, e, tan, √, Ans, EXP, xy, (, ), %, AC, 7, 8, 9, ÷, 4, 5, 6, ×, 1, 2, 3, -, 0, ., =, +, plot "utilization" using 1:($2/278400), plot "utilization.cpu.and.mem" using 1:($3/556800), plot "7-day-mono-ya-c_0.01-stdout-UTILIZATION" using 1:2, plot "7-day-mono-ya-c_0.01-stdout-UTILIZATION" using 1:($2/12500*8), plot [0:1] "7-day-mono-ya-c_0.01-stdout...

   scala> val counts = new collection.mutable.HashMap[String, Int].withDefaultValue(0)
   counts: scala.collection.mutable.Map[String,Int] = Map()

   scala> lines.flatMap(line => line.split(" ")).map(word => counts(word) += 1)
   res: Array[Unit] = Array((), (), (), (), (), (), (), (),...

   scala> counts
   res: scala.collection.mutable.Map[String,Int] = Map(2.6) -> 1, will -> 4, contain -> 1, Once -> 1, might -> 3, (requires -> 1, cluster. -> 3, up -> 2, There -> 1, addition, -> 1, host -> 3, distribution -> 2, [build] -> 2, Then -> 1, tests -> 3, ========================== -> 1, ZooKeeper -> 1, JAVA_HOME, -> 1, deploy -> 1, is -> 3, Cluster -> 1, [build]/bin/mesos-build-env.sh.in). -> 1, require -> 1, see -> 1, specific -> 3, necessary -> 2, JAVA_CPPFLAGS, -> 1, via: -> 1, collection -> 1, always, -> 1, configure -> 3, CPPFLAGS -> 1, email: -> 1, a -> 18, for -> 8, --localstatedir -> 1, building -> 1, slave(s) -> 1, After -> 2, populated -> 1, located -> 1, necessary) -> 1, we -> 3, doing: -> 2, with -> 4, Building -> 1, are: -> 1, JAR -> 1, [build]/src -> 1, --help -> 1, JAVA_CPPFLAGS...
   </pre>
   </div>

# Data Exploration Using Spark

In this section, we will first use the Spark shell to interactively explore the Wikipedia data.
Then, we will give a brief introduction to writing standalone Spark programs. Remember, Spark is an open source computation engine built on top of the popular Hadoop Distributed File System (HDFS).

## Interactive Analysis

Let's now use Spark to do some order statistics on the data set.
First, launch the Spark shell:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
/root/spark/spark-shell</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
/root/spark/pyspark</pre>
</div>
</div>

The prompt should appear within a few seconds. __Note:__ You may need to hit `[Enter]` once to clear the log output.

1. Warm up by creating an RDD (Resilient Distributed Dataset) named `pagecounts` from the input files.
   In the Spark shell, the SparkContext is already created for you as variable `sc`.

   <div class="codetabs">
     <div data-lang="scala" markdown="1">
       scala> sc
       res: spark.SparkContext = spark.SparkContext@470d1f30

       scala> val pagecounts = sc.textFile("/wiki/pagecounts")
       12/08/17 23:35:14 INFO mapred.FileInputFormat: Total input paths to process : 74
       pagecounts: spark.RDD[String] = MappedRDD[1] at textFile at <console>:12
     </div>
     <div data-lang="python" markdown="1">
       >>> sc
       <pyspark.context.SparkContext object at 0x7f7570783350>
       >>> pagecounts = sc.textFile("/wiki/pagecounts")
       13/02/01 05:30:43 INFO mapred.FileInputFormat: Total input paths to process : 74
       >>> pagecounts
       <pyspark.rdd.RDD object at 0x217d510>
     </div>
   </div>

2. Let's take a peek at the data. You can use the take operation of an RDD to get the first K records. Here, K = 10.

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> pagecounts.take(10)
       ...
       res: Array[String] = Array(20090505-000000 aa.b ?71G4Bo1cAdWyg 1 14463, 20090505-000000 aa.b Special:Statistics 1 840, 20090505-000000 aa.b Special:Whatlinkshere/MediaWiki:Returnto 1 1019, 20090505-000000 aa.b Wikibooks:About 1 15719, 20090505-000000 aa ?14mFX1ildVnBc 1 13205, 20090505-000000 aa ?53A%2FuYP3FfnKM 1 13207, 20090505-000000 aa ?93HqrnFc%2EiqRU 1 13199, 20090505-000000 aa ?95iZ%2Fjuimv31g 1 13201, 20090505-000000 aa File:Wikinews-logo.svg 1 8357, 20090505-000000 aa Main_Page 2 9980)
   </div>
   <div data-lang="python" markdown="1">
       >>> pagecounts.take(10)
       ...
       [u'20090505-000000 aa.b ?71G4Bo1cAdWyg 1 14463', u'20090505-000000 aa.b Special:Statistics 1 840', u'20090505-000000 aa.b Special:Whatlinkshere/MediaWiki:Returnto 1 1019', u'20090505-000000 aa.b Wikibooks:About 1 15719', u'20090505-000000 aa ?14mFX1ildVnBc 1 13205', u'20090505-000000 aa ?53A%2FuYP3FfnKM 1 13207', u'20090505-000000 aa ?93HqrnFc%2EiqRU 1 13199', u'20090505-000000 aa ?95iZ%2Fjuimv31g 1 13201', u'20090505-000000 aa File:Wikinews-logo.svg 1 8357', u'20090505-000000 aa Main_Page 2 9980']
   </div>
   </div>

   Unfortunately this is not very readable because `take()` returns an array and Scala simply prints the array with each element separated by a comma.
   We can make it prettier by traversing the array to print each record on its own line.

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> pagecounts.take(10).foreach(println)
       ...
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
   </div>
   <div data-lang="python" markdown="1">
       >>> for x in pagecounts.take(10):
       ...    print x
       ...
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
   </div>
   </div>

2. Let's see how many records in total are in this data set (this command will take a while, so read ahead while it is running).

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> pagecounts.count
   </div>
   <div data-lang="python" markdown="1">
       >>> pagecounts.count()
   </div>
   </div>

   This should launch 177 Spark tasks on the Spark cluster.
   If you look closely at the terminal, the console log is pretty chatty and tells you the progress of the tasks.
   Because we are reading 20G of data from HDFS, this task is I/O bound and can take a while to scan through all the data (2 - 3 mins).
￼
   While it's running, you can open the Spark web console to see the progress.
   To do this, open your favorite browser, and type in the following URL.

   `http://<master_node_hostname>:8080`

   You should have been given `master_node_hostname` at the beginning of the tutorial, or you might have [launched your own cluster](launching-a-cluster.html) and made a note of it then. You should see the Spark Standalone mode web interface, similar to the following (yours will probably show five slaves).

   ![Spark Standalone Web UI](img/standalone-webui640.png)

   When your count does finish running, it should return the following result:

       res: Long = 329641466

4. Recall from above when we described the format of the data set, that the second field is the "project code" and contains information about the language of the pages.
   For example, the project code "en" indicates an English page.
   Let's derive an RDD containing only English pages from `pagecounts`.
   This can be done by applying a filter function to `pagecounts`.
   For each record, we can split it by the field delimiter (i.e. a space) and get the second field-– and then compare it with the string "en".

   To avoid reading from disks each time we perform any operations on the RDD, we also __cache the RDD into memory__.
    This is where Spark really starts to to shine.

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> val enPages = pagecounts.filter(_.split(" ")(1) == "en").cache
       enPages: spark.RDD[String] = FilteredRDD[2] at filter at <console>:14
   </div>
   <div data-lang="python" markdown="1">
       >>> enPages = pagecounts.filter(lambda x: x.split(" ")[1] == "en").cache()
   </div>
   </div>

   When you type this command into the Spark shell, Spark defines the RDD, but because of lazy evaluation, no computation is done yet.
   Next time any action is invoked on `enPages`, Spark will cache the data set in memory across the 5 slaves in your cluster.

5. How many records are there for English pages?

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> enPages.count
       ...
       res: Long = 122352588
   </div>
   <div data-lang="python" markdown="1">
       >>> enPages.count()
       ...
       122352588
   </div>
   </div>

   The first time this command is run, similar to the last count we did, it will take 2 - 3 minutes while Spark scans through the entire data set on disk.
   __But since enPages was marked as "cached" in the previous step, if you run count on the same RDD again, it should return an order of magnitude faster__.

   If you examine the console log closely, you will see lines like this, indicating some data was added to the cache:

       13/02/05 20:29:01 INFO storage.BlockManagerMasterActor$BlockManagerInfo: Added rdd_2_172 in memory on ip-10-188-18-127.ec2.internal:42068 (size: 271.8 MB, free: 5.5 GB)

6. Let's try something fancier.
   Generate a histogram of total page views on Wikipedia English pages for the date range represented in our dataset (May 5 to May 7, 2009).
   The high level idea of what we'll be doing is as follows.
   First, we generate a key value pair for each line; the key is the date (the first eight characters of the first field), and the value is the number of pageviews for that date (the fourth field).

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> val enTuples = enPages.map(line => line.split(" "))
       enTuples: spark.RDD[Array[java.lang.String]] = MappedRDD[3] at map at <console>:16

       scala> val enKeyValuePairs = enTuples.map(line => (line(0).substring(0, 8), line(3).toInt))
       enKeyValuePairs: spark.RDD[(java.lang.String, Int)] = MappedRDD[4] at map at <console>:18
   </div>
   <div data-lang="python" markdown="1">
       >>> enTuples = enPages.map(lambda x: x.split(" "))
       >>> enKeyValuePairs = enTuples.map(lambda x: (x[0][:8], int(x[3])))
   </div>
   </div>

   Next, we shuffle the data and group all values of the same key together.
   Finally we sum up the values for each key.
   There is a convenient method called `reduceByKey` in Spark for exactly this pattern.
   Note that the second argument to `reduceByKey` determines the number of reducers to use.
   By default, Spark assumes that the reduce function is algebraic and applies combiners on the mapper side.
   Since we know there is a very limited number of keys in this case (because there are only 3 unique dates in our data set), let's use only one reducer.

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> enKeyValuePairs.reduceByKey(_+_, 1).collect
       ...
       res: Array[(java.lang.String, Int)] = Array((20090506,204190442), (20090507,202617618), (20090505,207698578))
   </div>
   <div data-lang="python" markdown="1">
       >>> enKeyValuePairs.reduceByKey(lambda x, y: x + y, 1).collect()
       ...
       [(u'20090506', 204190442), (u'20090507', 202617618), (u'20090505', 207698578)]
   </div>
   </div>

   The `collect` method at the end converts the result from an RDD to an array.
   Note that when we don't specify a name for the result of a command (e.g. `val enTuples` above), a variable with name `res`<i>N</i> is automatically created.

   We can combine the previous three commands into one:

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> enPages.map(line => line.split(" ")).map(line => (line(0).substring(0, 8), line(3).toInt)).reduceByKey(_+_, 1).collect
       ...
       res: Array[(java.lang.String, Int)] = Array((20090506,204190442), (20090507,202617618), (20090505,207698578))
   </div>
   <div data-lang="python" markdown="1">
       >>> enPages.map(lambda x: x.split(" ")).map(lambda x: (x[0][:8], int(x[3]))).reduceByKey(lambda x, y: x + y, 1).collect()
       ...
       [(u'20090506', 204190442), (u'20090507', 202617618), (u'20090505', 207698578)]
   </div>
   </div>

7. Suppose we want to find pages that were viewed more than 200,000 times during the three days covered by our dataset.
   Conceptually, this task is similar to the previous query.
   But, given the large number of pages (23 million distinct page names), the new task is very expensive.
   We are doing an expensive group-by with a lot of network shuffling of data.

   To recap, first we split each line of data into its respective fields.
   Next, we extract the fields for page name and number of page views.
   We reduce by key again, this time with 40 reducers.
   Then we filter out pages with less than 200,000 total views over our time window represented by our dataset.

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
       scala> enPages.map(l => l.split(" ")).map(l => (l(2), l(3).toInt)).reduceByKey(_+_, 40).filter(x => x._2 > 200000).map(x => (x._2, x._1)).collect.foreach(println)
       (203378,YouTube)
       (17657352,Special:Search)
       (311465,Special:Watchlist)
       (248624,Special:Export)
       (237677,2009_swine_flu_outbreak)
       (396776,Dom_DeLuise)
       (5816953,Special:Random)
       (18730347,Main_Page)
       (534253,Swine_influenza)
       (310642,index.html)
       (464935,Wiki)
       (382510,Deadpool_(comics))
       (3521336,Special:Randompage)
       (204604,X-Men_Origins:_Wolverine)
       (695817,Cinco_de_Mayo)
       (317708,The_Beatles)
       (234855,Scrubs_(TV_series))
       (43822489,404_error/)
   </div>
   <div data-lang="python" markdown="1">
       >>> enPages.map(lambda x: x.split(" ")).map(lambda x: (x[2], int(x[3]))).reduceByKey(lambda x, y: x + y, 40).filter(lambda x: x[1] > 200000).map(lambda x: (x[1], x[0])).collect()
       [(5816953, u'Special:Random'), (18730347, u'Main_Page'), (534253, u'Swine_influenza'), (382510, u'Deadpool_(comics)'), (204604, u'X-Men_Origins:_Wolverine'), (203378, u'YouTube'), (43822489, u'404_error/'), (234855, u'Scrubs_(TV_series)'), (248624, u'Special:Export'), (695817, u'Cinco_de_Mayo'), (311465, u'Special:Watchlist'), (396776, u'Dom_DeLuise'), (310642, u'index.html'), (317708, u'The_Beatles'), (237677, u'2009_swine_flu_outbreak'), (3521336, u'Special:Randompage'), (464935, u'Wiki'), (17657352, u'Special:Search')]
   </div>
   </div>

   There is no hard and fast way to calculate the optimal number of reducers for a given problem; you will
   build up intuition over time by experimenting with different values.

   To leave the Spark shell, type `exit` at the prompt.

8. You can explore the full RDD API by browsing the [Java/Scala](http://www.cs.berkeley.edu/~pwendell/strataconf/api/core/index.html#spark.RDD) or [Python](http://www.cs.berkeley.edu/~pwendell/strataconf/api/pyspark/index.html) API docs.

## Running Standalone Spark Programs

Because of time constraints, in this tutorial we focus on ad-hoc style analytics using the Spark shell.
However, for many tasks, it makes more sense to write a standalone Spark program.
We will return to this in the section on Spark Streaming below, where you will actually write a standalone Spark Streaming job.
We aren't going to cover how to structure, build, and run standalone Spark jobs here, but before we move on, we list here a few resources about standalone Spark jobs for you to come back and explore later.

First, on the AMI for this tutorial we have included "template" projects for Scala and Java standalone programs for both Spark and Spark streaming.
The Spark ones can be found in the `/root/scala-app-template` and `/root/java-app-template` directories (we will discuss the Streaming ones later).
Feel free to browse through the contents of those directories. You can also find examples of building and running Spark standalone jobs <a href="http://www.spark-project.org/docs/0.6.0/quick-start.html#a-standalone-job-in-java">in Java</a> and <a href="http://www.spark-project.org/docs/0.6.0/quick-start.html#a-standalone-job-in-scala">in Scala</a> as part of the Spark Quick Start Guide. For even more details, see Matei Zaharia's <a href="http://ampcamp.berkeley.edu/wp-content/uploads/2012/06/matei-zaharia-part-2-amp-camp-2012-standalone-programs.pdf" target="_blank">slides</a> and <a href="http://www.youtube.com/watch?v=7k4yDKBYOcw&t=59m37s" target="_blank">talk video</a> about Standalone Spark jobs at the <a href="http://ampcamp.berkeley.edu/agenda-2012" target="blank">first AMP Camp</a>.

# Data Exploration Using Shark

Now that we've had fun with Spark, let's try out Shark. Remember Shark is a large-scale data warehouse system that runs on top of Spark and provides a SQL like interface (that is fully compatible with Apache Hive).

1. First, launch the Shark console:

   <pre class="prettyprint lang-bsh">
   /root/shark-0.2/bin/shark-withinfo
   </pre>

1. Similar to Apache Hive, Shark can query external tables (i.e., tables that are not created in Shark).
   Before you do any querying, you will need to tell Shark where the data is and define its schema.

   <pre class="prettyprint lang-sql">
   shark> create external table wikistats (dt string, project_code string, page_name string, page_views int, bytes int) row format delimited fields terminated by ' ' location '/wiki/pagecounts';
   <span class="nocode">
   ...
   Time taken: 0.232 seconds
   13/02/05 21:31:25 INFO CliDriver: Time taken: 0.232 seconds</span></pre>

   <b>FAQ:</b> If you see the following errors, don’t worry. Things are still working under the hood.

   <pre class="nocode">
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.core.resources" but it cannot be resolved.
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.core.runtime" but it cannot be resolved.
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.text" but it cannot be resolved.</pre>

   <b>FAQ:</b> If you see errors like these, you might have copied and pasted a line break, and should be able to remove it to get rid of the errors.

   <pre>13/02/05 21:22:16 INFO parse.ParseDriver: Parsing command: CR
   FAILED: Parse Error: line 1:0 cannot recognize input near 'CR' '&lt;EOF&gt;' '&lt;EOF&gt;'</pre>

1. Let's create a table containing all English records and cache it in the cluster's memory.

   <pre class="prettyprint lang-sql">
   shark> create table wikistats_cached as select * from wikistats where project_code="en";
   <span class="nocode">
   ...
   Time taken: 127.5 seconds
   13/02/05 21:57:34 INFO CliDriver: Time taken: 127.5 seconds</span></pre>

1. Do a simple count to get the number of English records. If you have some familiarity working with databases, note that we us the "`count(1)`" syntax here since in earlier versions of Hive, the more popular "`count(*)`" operation was not supported. The Hive syntax is described in detail in the <a href="https://cwiki.apache.org/confluence/display/Hive/GettingStarted" target="_blank">Hive Getting Started Guide</a>.

   <pre class="prettyprint lang-sql">
   shark> select count(1) from wikistats_cached;
   <span class="nocode">
   ...
   122352588
   Time taken: 7.632 seconds
   12/08/18 21:23:13 INFO CliDriver: Time taken: 7.632 seconds</span></pre>

1. Output the total traffic to Wikipedia English pages for each hour between May 7 and May 9, with one line per hour.

   <pre class="prettyprint lang-sql">
   shark> select dt, sum(page_views) from wikistats_cached group by dt;
   <span class="nocode">
   ...
   20090507-070000	6292754
   20090505-120000	7304485
   20090506-110000	6609124
   Time taken: 12.614 seconds
   13/02/05 22:05:18 INFO CliDriver: Time taken: 12.614 seconds</span></pre>

1. In the Spark section, we ran a very expensive query to compute pages that were viewed more than 200,000 times. It is fairly simple to do the same thing in SQL.

   To make the query run faster, we increase the number of reducers used in this query to 50 in the first command. Note that the default number of reducers, which we have been using so far in this section, is 1.

   <pre class="prettyprint lang-sql">
   shark> set mapred.reduce.tasks=50;
   shark> select page_name, sum(page_views) as views from wikistats_cached group by page_name having views > 200000;
   <span class="nocode">
   ...
   index.html      310642
   Swine_influenza 534253
   404_error/      43822489
   YouTube 203378
   X-Men_Origins:_Wolverine        204604
   Dom_DeLuise     396776
   Special:Watchlist       311465
   The_Beatles     317708
   Special:Search  17657352
   Special:Random  5816953
   Special:Export  248624
   Scrubs_(TV_series)      234855
   Cinco_de_Mayo   695817
   2009_swine_flu_outbreak 237677
   Deadpool_(comics)       382510
   Wiki    464935
   Special:Randompage      3521336
   Main_Page       18730347
   Time taken: 68.693 seconds
   13/02/26 08:12:42 INFO CliDriver: Time taken: 68.693 seconds</span></pre>


1. With all the warm up, now it is your turn to write queries. Write Hive QL queries to answer the following questions:

- Count the number of distinct date/times for English pages

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select count(distinct dt) from wikistats_cached;</pre>
   </div>

- How many hits are there on pages with Berkeley in the title throughout the entire period?

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select count(page_views) from wikistats_cached where page_name like "%berkeley%";
   /* "%" in SQL is a wildcard matching all characters. */</pre>
   </div>

- Generate a histogram for the number of hits for each hour on May 6, 2009; sort the output by date/time. Based on the output, which hour is Wikipedia most popular?

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select dt, sum(page_views) from wikistats_cached where dt like "20090506%" group by dt order by dt;</pre>
   </div>

To exit Shark, type the following at the Shark command line (and don't forget the semicolon!).

   <pre class="prettyprint lang-sql">
   shark> exit;</pre>


# Processing Live Data Streams with Spark Streaming

In this section, we will walk you through using Spark Streaming to process live data streams. Remember, Spark Streaming is a new component of Spark that provides highly scalable, fault-tolerant streaming processing. These exercises are designed as standalone Scala programs which will receive and process Twitter's real sample tweet streams. For the exercises in this section, you can choose to use Scala or Java. If you would like to use Scala but are not familiar with the language, we recommend that you see the [Introduction to the Scala Shell](#introduction-to-the-scala-shell) section to learn some basics.

## Setup
We use a modified version of the Scala standalone project template introduced in the [Intro to Running Standalone Programs](#running-standalone-spark-programs) section for the next exercise. In your AMI, this has been setup in `/root/streaming/`. You should find the following items in the directory.

<div class="sidebar">
<p style="font-size:1.2em"><b>What is SBT?</b></p>
Simple Build Tool, or SBT, is popular open-source a build tool for Scala and Java projects, written in Scala. Currently, Spark can be built using SBT or Maven, while Spark Streaming an Shark can be built with SBT only. Read more about SBT at <a href="https://github.com/harrah/xsbt/wiki" target="_blank">its Github page</a>.
</div>

- `login.txt:` File containing Twitter username and password
- For Scala users
  - `scala/sbt:` Directory containing the SBT tool
  - `scala/build.sbt:` SBT project file
  - `scala/Tutorial.scala:` Main Scala program that you are going to edit, compile and run
  - `scala/TutorialHelper.scala:` Scala file containing few helper functions for `Tutorial.scala`
- For Java users
  - `java/sbt:` Directory containing the SBT tool
  - `java/build.sbt:` SBT project file
  - `java/Tutorial.java` Main Java program that you are going to edit, compile and run
  - `java/TutorialHeler.java:` Java file containing a few helper functions
  - `java/ScalaHelper.java:` Scala file containing a few helper functions

The main file you are going to edit, compile and run for the exercises is `Tutorial.scala` or `Tutorial.java`. It should look as follows:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import spark._
import spark.streaming._
import StreamingContext._
import TutorialHelper._

object Tutorial {
  def main(args: Array[String]) {

    // Location of the Spark directory
    val sparkHome = "/root/spark"

    // URL of the Spark cluster
    val sparkUrl = getSparkUrl()

    // Location of the required JAR files
    val jarFile = "target/scala-2.9.2/tutorial_2.9.2-0.1-SNAPSHOT.jar"

    // HDFS directory for checkpointing
    val checkpointDir = TutorialHelper.getHdfsUrl() + "/checkpoint/"

    // Twitter credentials from login.txt
    val (twitterUsername, twitterPassword) = getTwitterCredentials()

    // Your code goes here
  }
}
~~~
</div>
<div data-lang="java" markdown="1">
~~~
import spark.api.java.*;
import spark.api.java.function.*;
import spark.streaming.*;
import spark.streaming.api.java.*;
import twitter4j.*;
import java.util.Arrays;
import scala.Tuple2;

public class Tutorial {
  public static void main(String[] args) throws Exception {
    // Location of the Spark directory
    String sparkHome = "/root/spark";

    // URL of the Spark cluster
    String sparkUrl = TutorialHelper.getSparkUrl();

    // Location of the required JAR files
    String jarFile = "target/scala-2.9.2/tutorial_2.9.2-0.1-SNAPSHOT.jar";

    // HDFS directory for checkpointing
    String checkpointDir = TutorialHelper.getHdfsUrl() + "/checkpoint/";

    // Twitter credentials from login.txt
    String twitterUsername = TutorialHelper.getTwitterUsername();
    String twitterPassword = TutorialHelper.getTwitterPassword();

    // Your code goes here
  }
}
~~~
</div>
</div>

For your convenience, we have added a couple of helper function to get the parameters that the exercises need.

- `getSparkUrl()` is a helper function that fetches the Spark cluster URL from the file `/root/spark-ec2/cluster-url`.
- `getTwitterCredential()` is another helper function that fetches the Twitter username and password from the file `/root/streaming/login.txt`.


<div class="alert alert-info">
<i class="icon-info-sign"> 	</i>
Since all the exercises are based on Twitter's sample tweet stream, they require you specify a Twitter account's username and password. You can either use you your own Twitter username and password, or use one of the few accounts we made for the purpose of this tutorial. The username and password needs to be set in the file `/root/streaming/login.txt`
</div>

<pre class="nocode">
my.fancy.username
my_uncrackable_password
</pre>

Be sure to delete this file after the exercises are over. Even if you don't delete them, these files will be completely destroyed along with the instance, so your password will not fall into wrong hands.


## First Spark Streaming program
Let's try to write a very simple Spark Streaming program that prints a sample of the tweets it receives from Twitter every second. First locate the
`Tutorial` class and open it with a text editor.

<div class="codetabs">
<div data-lang="scala">
<pre class="prettyprint lang-bsh">
cd /root/streaming/scala/
vim Tutorial.scala
</pre>
</div>
<div data-lang="java">
<pre class="prettyprint lang-bsh">
cd /root/streaming/java/
vim Tutorial.java
</pre>
</div>
</div>

The cluster machines have both vim and emacs installed for editing. Alternatively, you can use your favorite text editor locally and then copy-paste content using vim or emacs before running it.


To express any Spark Streaming computation, a StreamingContext object needs to be created.
This object serves as the main entry point for all Spark Streaming functionality.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val ssc = new StreamingContext(sparkUrl, "Tutorial", Seconds(1), sparkHome, Seq(jarFile))
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaStreamingContext ssc = new JavaStreamingContext(
      sparkUrl, "Tutorial", new Duration(1000), sparkHome, new String[]{jarFile});
~~~
</div>
</div>

Here, we create a StreamingContext object by providing the Spark cluster URL, the batch duration we'd like to use for streams, the Spark home directory, and the list of JAR files that are necessary to run the program. "Tutorial" is a unique name given to this application to identify it the Spark's web UI. We elect for a batch duration of 1 second. Next, we use this context and the login information to create a stream of tweets:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val tweets = ssc.twitterStream(twitterUsername, twitterPassword)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaDStream<Status> tweets =
      ssc.twitterStream(twitterUsername, twitterPassword);
~~~
</div>
</div>

The object `tweets` is a DStream of tweet statuses. More specifically, it is continuous stream of RDDs containing objects of type [twitter4j.Status](http://twitter4j.org/javadoc/twitter4j/Status.html). As a very simple processing step, let's try to print the status text of the some of the tweets.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val statuses = tweets.map(status => status.getText())
    statuses.print()
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaDStream<String> statuses = tweets.map(
      new Function<Status, String>() {
        public String call(Status status) { return status.getText(); }
      }
    );
    statuses.print();
~~~
</div>
</div>

Similar to RDD transformation in the earlier Spark exercises, the `map` operation on `tweets` maps each Status object to its text to create a new 'transformed' DStream named `statuses`. The `print` output operation tells the context to print first 10 records in each RDD in a DStream, which in this case, are 1 second batches of received status texts.

We also need to set an HDFS for periodic checkpointing of the intermediate data.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    ssc.checkpoint(checkpointDir)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    ssc.checkpoint(checkpointDir);
~~~
</div>
</div>

Finally, we need to tell the context to start running the computation we have setup.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    ssc.start()
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    ssc.start();
~~~
</div>
</div>

__Note that all DStream operations must be done before calling this statement.__

After saving `Tutorial.scala`, it can be run from the command prompt using the following command (from within the `/root/streaming/[language]` directory).

<pre><code>sbt/sbt package run</code></pre>

This command will automatically compile the `Tutorial` class and create a JAR file in `/root/streaming/[language]/target/scala-2.9.2/`. Finally, it will run the program. You should see output similar to the following on your screen:

<pre class="nocode">
-------------------------------------------
Time: 1359886325000 ms
-------------------------------------------
RT @__PiscesBabyyy: You Dont Wanna Hurt Me But Your Constantly Doing It
@Shu_Inukai ?????????????????????????????????????????
@Condormoda Us vaig descobrir a la @080_bcn_fashion. Molt bona desfilada. Salutacions des de #Manresa
RT @dragon_itou: ?RT???????3000???????????????????????????????????10???????

?????????????????2?3???9???? #???? http://t.co/PwyA5dsI ? h ...
Sini aku antar ke RSJ ya "@NiieSiiRenii: Memang (?? ?`? )"@RiskiMaris: Stresss"@NiieSiiRenii: Sukasuka aku donk:p"@RiskiMaris: Makanya jgn"
@brennn_star lol I would love to come back, you seem pretty cool! I just dont know if I could ever do graveyard again :( It KILLs me
????????????????????????????????????????????????????????????????????????????????????????ww
??????????
When the first boats left the rock with the artificers employed on.
@tgs_nth ????????????????????????????
...


-------------------------------------------
Time: 1359886326000 ms
-------------------------------------------
???????????
???????????
@amatuki007 ????????????????????????????????
?????????????????
RT @BrunoMars: Wooh!
Lo malo es qe no tiene toallitas
Sayang beb RT @enjaaangg Piye ya perasaanmu nyg aku :o
Baz? ?eyler yar??ma ya da reklam konusu olmamal? d???ncesini yenemiyorum.
?????????????MTV???????the HIATUS??
@anisyifaa haha. Cukupla merepek sikit2 :3
@RemyBot ?????????
...
</pre>

To stop the application, use `Ctrl + c` .

__FAQ__: If you see the following message, it means that the authentication with Twitter failed.

<pre class="nocode">
13/02/04 23:41:57 INFO streaming.NetworkInputTracker: De-registered receiver for network stream 0 with message 401:Authentication credentials (https://dev.twitter.com/pages/auth) were missing or incorrect. Ensure that you have set valid consumer key/secret, access token/secret, and the system clock is in sync.
&lt;html&gt;
&lt;head&gt;
&lt;meta http-equiv="Content-Type" content="text/html; charset=utf-8"/&gt;
&lt;title&gt;Error 401 Unauthorized&lt;/title&gt;
&lt;/head&gt;
&lt;body&gt;
&lt;h2&gt;HTTP ERROR: 401&lt;/h2&gt;
&lt;p&gt;Problem accessing '/1.1/statuses/sample.json?stall_warnings=true'. Reason:
&lt;pre&gt;    Unauthorized&lt;/pre&gt;



&lt;/body&gt;
&lt;/html&gt;


Relevant discussions can be found on the Internet at:
	http://www.google.co.jp/search?q=d0031b0b or
	http://www.google.co.jp/search?q=1db75513
TwitterException{exceptionCode=[d0031b0b-1db75513], statusCode=401, message=null, code=-1, retryAfter=-1, rateLimitStatus=null, version=3.0.3}
</pre>

__Answer:__ Please verify whether the Twitter username and password has been set correctly in the file `login.txt` as instructed earlier. Make sure you do not have unnecessary trailing spaces.


## Further exercises
Next, let's try something more interesting, say, try printing the 10 most popular hashtags in the last 5 minutes. These next steps explain the set of the DStream operations required to achieve our goal. As mentioned before, the operations explained in the next steps must be added in the program before `ssc.start()`. After every step, you can see the contents of new DStream you created by using the `print()` operation and running Tutorial in the same way as explained earlier (that is, `sbt/sbt package run`).

1. __Get the stream of hashtags from the stream of tweets__:
   To get the hashtags from the status string, we need to identify only those words in the message that start with "#". This can be done as follows.


   <div class="codetabs">
   <div data-lang="scala" markdown="1">

   ~~~
       val words = statuses.flatMap(status => status.split(" "))
       val hashtags = words.filter(word => word.startsWith("#"))
   ~~~

   </div>
   <div data-lang="java" markdown="1">

   ~~~
      JavaDStream<String> words = statuses.flatMap(
        new FlatMapFunction<String, String>() {
          public Iterable<String> call(String in) {
            return Arrays.asList(in.split(" "));
          }
        }
      );

      JavaDStream<String> hashTags = words.filter(
        new Function<String, Boolean>() {
          public Boolean call(String word) { return word.startsWith("#"); }
        }
      );
   ~~~

   </div>
   </div>

   The `flatMap` operation applies a one-to-many operation to each record in a DStream and then flattens the records to create a new DStream.
   In this case, each status string is split by space to produce a DStream whose each record is a word.
   Then we apply the `filter` function to retain only the hashtags. The resulting `hashtags` DStream is a stream of RDDs having only the hashtags.
   If you want to see the result, add `hashtags.print()` and try running the program.
   You should see something like this (assuming no other DStream has `print` on it).

   <pre class="nocode">
   -------------------------------------------
   Time: 1359886521000 ms
   -------------------------------------------
   #njnbg
   #njpw
   #?????
   #algeria
   #Annaba
   </pre>

2. __Count the hashtags over a 5 minute window__: Next, we'd like to count these hashtags over a 5 minute moving window.
   A simple way to do this would be to gather together last 5 minutes of data and process them using the usual map-reduce way - map each tag to a (tag, 1) key-value pair and
   then reduce by adding the counts. However, in this case, counting over a sliding window can be done more intelligently. As the window moves, the counts of the new data can
   be added to the previous window's counts, and the counts of the old data that falls out of the window can be 'subtracted' from the previous window's counts. This can be
   done using DStreams as follows.

   <div class="codetabs">
   <div data-lang="scala" markdown="1">

   ~~~
       val counts = hashtags.map(tag => (tag, 1))
                            .reduceByKeyAndWindow(_ + _, _ - _, Seconds(60 * 5), Seconds(1))
   ~~~

   The `_ + _` and `_ - _` are Scala short-hands for specifying functions to add and subtract two numbers. `Seconds(60 * 5)` specifies
   the window size and `Seconds(1)` specifies the movement of the window.

   </div>
   <div data-lang="java" markdown="1">

   ~~~
      JavaPairDStream<String, Integer> tuples = hashTags.map(
         new PairFunction<String, String, Integer>() {
           public Tuple2<String, Integer> call(String in) {
             return new Tuple2<String, Integer>(in, 1);
           }
         }
       );

       JavaPairDStream<String, Integer> counts = tuples.reduceByKeyAndWindow(
         new Function2<Integer, Integer, Integer>() {
           public Integer call(Integer i1, Integer i2) { return i1 + i2; }
         },
         new Function2<Integer, Integer, Integer>() {
           public Integer call(Integer i1, Integer i2) { return i1 - i2; }
         },
         new Duration(60 * 5 * 1000),
         new Duration(1 * 1000)
       );
   ~~~

   There are two functions that are being defined for adding and subtracting the counts. `new Duration(60 * 5 * 1000)`
   specifies the window size and `new Duration(1 * 1000)` specifies the movement of the window.

   </div>
   </div>

   Note, that only 'invertible' reduce operations that have 'inverse' functions (like subtracting is the inverse function of adding)
   can be optimized in this manner. The generated `counts` DStream will have records that are (hashtag, count) tuples.
   If you `print` counts and run this program, you should see something like this.

   <pre class="nocode">
   -------------------------------------------
   Time: 1359886694000 ms
   -------------------------------------------
   (#epic,1)
   (#WOWSetanYangTerbaik,1)
   (#recharged,1)
   (#??????????,1)
   (#jaco,1)
   (#Blondie,1)
   (#TOKIO,1)
   (#fili,1)
   (#jackiechanisamazing,1)
   (#DASH,1)
   ...
   </pre>


3. __Find the top 10 hashtags based on their counts__:
   Finally, these counts have to be used to find the popular hashtags.
   A simple (but not the most efficient) way to do this is to sort the hashtags based on their counts and
   take the top 10 records. Since this requires sorting by the counts, the count (i.e., the second item in the
   (hashtag, count) tuple) needs to be made the key. Hence, we need to first use a `map` to flip the tuple and
   then sort the hashtags. Finally, we need to get the top 10 hashtags and print them. All this can be done as follows:

   <div class="codetabs">
   <div data-lang="scala" markdown="1">
   ~~~
       val sortedCounts = counts.map { case(tag, count) => (count, tag) }
                                .transform(rdd => rdd.sortByKey(false))
       sortedCounts.foreach(rdd =>
         println("\nTop 10 hashtags:\n" + rdd.take(10).mkString("\n")))
   ~~~
   </div>
   <div data-lang="java" markdown="1">
   ~~~
      JavaPairDStream<Integer, String> swappedCounts = counts.map(
        new PairFunction<Tuple2<String, Integer>, Integer, String>() {
          public Tuple2<Integer, String> call(Tuple2<String, Integer> in) {
            return in.swap();
          }
        }
      );

      JavaPairDStream<Integer, String> sortedCounts = swappedCounts.transform(
        new Function<JavaPairRDD<Integer, String>, JavaPairRDD<Integer, String>>() {
          public JavaPairRDD<Integer, String> call(JavaPairRDD<Integer, String> in) throws Exception {
            return in.sortByKey(false);
          }
        });

      sortedCounts.foreach(
        new Function<JavaPairRDD<Integer, String>, Void> () {
          public Void call(JavaPairRDD<Integer, String> rdd) {
            String out = "\nTop 10 hashtags:\n";
            for (Tuple2<Integer, String> t: rdd.take(10)) {
              out = out + t.toString() + "\n";
            }
            System.out.println(out);
            return null;
          }
        }
      );
   ~~~
   </div>
   </div>

   The `transform` operation allows any arbitrary RDD-to-RDD operation to be applied to each RDD of a DStream to generate a new DStream.
   The resulting 'sortedCounts' DStream is a stream of RDDs having sorted hashtags.
   The `foreach` operation applies a given function on each RDD in a DStream, that is, on each batch of data. In this case,
   `foreach` is used to get the first 10 hashtags from each RDD in `sortedCounts` and print them, every second.
   If you run this program, you should see something like this.

   <pre class="nocode">
   Top 10 hashtags:
   (2,#buzzer)
   (1,#LawsonComp)
   (1,#wizkidleftEMEcos)
   (1,#???????)
   (1,#NEVERSHUTMEUP)
   (1,#reseteo.)
   (1,#casisomoslamismapersona)
   (1,#job)
   (1,#????_??_?????_??????)
   (1,#?????RT(*^^*))
   </pre>

   Note that there are more efficient ways to get the top 10 hashtags. For example, instead of sorting the entire of
   5-minute-counts (thereby, incurring the cost of a data shuffle), one can get the top 10 hashtags in each partition,
   collect them together at the driver and then find the top 10 hashtags among them.
   We leave this as an exercise for the reader to try.

4. __API Reference__: You can explore the full streaming API by referencing the [Java/Scala](http://www.cs.berkeley.edu/~pwendell/strataconf/api/streaming/index.html#spark.streaming.DStream) API docs.

# Machine Learning

In this section, we will use Spark to implement machine learning algorithms. To complete the machine learning exercises within the time available using our relatively small EC2 clusters, in this section we will work with a restricted set of the Wikipedia traffic statistics data from May 5-7, 2009. In particular, this dataset only includes a subset of all Wikipedia articles. This restricted dataset is pre-loaded in the HDFS on your cluster in `/wikistats_20090505-07_restricted`.

## Command Line Preprocessing and Featurization

To apply most machine learning algorithms, we must first preprocess and featurize the data.  That is, for each data point, we must generate a vector of numbers describing the salient properties of that data point.  In our case, each data point will consist of a unique Wikipedia article identifier (i.e., a unique combination of Wikipedia project code and page title) and associated traffic statistics.  We will generate 24-dimensional feature vectors, with each feature vector entry summarizing the page view counts for the corresponding hour of the day.

Recall that each record in our dataset consists of a string with the format "`<date_time> <project_code> <page_title> <num_hits> <page_size>`".  The format of the date-time field is YYYYMMDD-HHmmSS (where 'M' denotes month, and 'm' denotes minute).

Given our time constraints, in order to focus on the machine learning algorithms themselves, we have pre-processed the data to create the featurized dataset that we will use to implement K-means clustering. If you are interested in doing featurization on your own, you can follow [these instructions](featurization.html).

## Clustering

[K-Means clustering](http://en.wikipedia.org/wiki/K-means_clustering) is a popular clustering algorithm that can be used to partition your dataset into K clusters. We now look at how we can implement K-Means clustering using Spark to cluster the featurized Wikipedia dataset.

## Setup
Similar to the Spark streaming exercises above, we will be using a standalone project template for this exercise. In your AMI, this has been setup in `/root/kmeans/[scala|java|python]/`. You should find the following items in the directory.

<div class="codetabs">
<div data-lang="scala">

<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>sbt</code>: Directory containing the SBT tool</li>
<li><code>build.sbt</code>: SBT project file</li>
<li><code>WikipediaKMeans.scala</code>: Main Scala program that you are going to edit, compile and run</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.scala</code>. It should look as follows:
</div>
<div data-lang="java">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>sbt</code>: Directory containing the SBT tool</li>
<li><code>build.sbt</code>: SBT project file</li>
<li><code>WikipediaKMeans.java</code>: Main Java program that you are going to edit, compile and run</li>
<li><code>JavaHelpers.java</code>: Some helper functions used by the template.</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.java</code>. It should look as follows:
</div>
<div data-lang="python">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>WikipediaKMeans.py</code>: Main Python program that you are going to edit and run</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.py</code>. It should look as follows:
</div>
</div>


<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import spark.SparkContext
import spark.SparkContext._
import spark.util.Vector

import org.apache.log4j.Logger
import org.apache.log4j.Level

import scala.util.Random
import scala.io.Source

object WikipediaKMeans {
  def parseVector(line: String): Vector = {
      return new Vector(line.split(',').map(_.toDouble))
  }

  // Add any new functions you need here

  def main(args: Array[String]) {
    Logger.getLogger("spark").setLevel(Level.WARN)
    val sparkHome = "/root/spark"
    val jarFile = "target/scala-2.9.2/wikipedia-kmeans_2.9.2-0.0.jar"
    val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
    val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim

    val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))

    val K = 10
    val convergeDist = 1e-6

    val data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
            t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()

    // Your code goes here

    sc.stop();
    System.exit(0)
  }
}
~~~
</div>
<div data-lang="java" markdown="1">
~~~
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.io.*;
import java.util.*;
import com.google.common.collect.Lists;

import scala.Tuple2;
import spark.api.java.*;
import spark.api.java.function.*;
import spark.util.Vector;


public class WikipediaKMeans {
  // Add any new functions you need here

  public static void main(String[] args) throws Exception {
    Logger.getLogger("spark").setLevel(Level.WARN);
    String sparkHome = "/root/spark";
    String jarFile = "target/scala-2.9.2/wikipedia-kmeans_2.9.2-0.0.jar";
    String master = JavaHelpers.getSparkUrl();
    String masterHostname = JavaHelpers.getMasterHostname();
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans",
      sparkHome, jarFile);

    int K = 10;
    double convergeDist = .000001;

    JavaPairRDD<String, Vector> data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
      new PairFunction<String, String, Vector>() {
        public Tuple2<String, Vector> call(String in)
        throws Exception {
          String[] parts = in.split("#");
          return new Tuple2<String, Vector>(
            parts[0], JavaHelpers.parseVector(parts[1]));
        }
      }
     ).cache();

    // Your code goes here

    sc.stop();
    System.exit(0);
  }
}
~~~
</div>
<div data-lang="python" markdown="1">
~~~
import os
import sys
import numpy as np

from pyspark import SparkContext

def setClassPath():
    oldClassPath = os.environ.get('SPARK_CLASSPATH', '')
    cwd = os.path.dirname(os.path.realpath(__file__))
    os.environ['SPARK_CLASSPATH'] = cwd + ":" + oldClassPath


def parseVector(line):
    return np.array([float(x) for x in line.split(',')])

# Add any new functions you need here

if __name__ == "__main__":
    setClassPath()
    master = open("/root/spark-ec2/cluster-url").read().strip()
    masterHostname = open("/root/spark-ec2/masters").read().strip()
    sc = SparkContext(master, "PythonKMeans")
    K = 10
    convergeDist = 1e-5

    lines = sc.textFile(
	"hdfs://" + masterHostname + ":9000/wikistats_featurized")
    data = lines.map(
	lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()

    # Your code goes here
~~~
</div>
</div>

Let's first take a closer look at our template code in a text editor on the cluster itself, then we'll start adding code to the template. Locate the `WikipediaKMeans` class and open it with a text editor.

<div class="codetabs">
<div data-lang="scala">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/scala
vim WikipediaKMeans.scala  # If you don't know vim, you can use emacs or nano
</pre>
</div>
<div data-lang="java">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/java
vim WikipediaKMeans.java  # If you don't know vim, you can use emacs or nano
</pre>
</div>
<div data-lang="python">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/python
vim WikipediaKMeans.py  # If you don't know Vim, you can use emacs or nano
</pre>
</div>
</div>

The cluster machines have vim, emacs, and nano installed for editing. Alternatively, you can use your favorite text editor locally and then copy-paste content into vim, emacs, or nano before running it.

For any Spark computation, we first create a Spark context object. For Scala or Java programs, we do that by providing the Spark cluster URL, the Spark home directory, and the JAR file that will be generated when we compile our program. For Python programs, we only need to provide the Spark cluster URL. Finally, we also name our program "WikipediaKMeans" to identify it in Spark's web UI.

This is what it looks like in our template code:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans", sparkHome, jarFile);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    sc = SparkContext(master, "PythonKMeans")
~~~
</div>
</div>

Next, the code uses the SparkContext to read in our featurized dataset. The [featurization process](#command-line-preprocessing-and-featurization) created a 24-dimensional feature vector for each article in our Wikipedia dataset, with each vector entry summarizing the page view counts for the corresponding hour of the day. Each line in the file consists of the page identifier and the features separated by commas.

Next, the code reads the file in from HDFS and parses each line to create a RDD which contains pairs of `(String, Vector)`.

A quick note about the `Vector` class we will using in this exercise: For Scala and Java programs we will be using the [Vector](http://spark-project.org/docs/latest/api/core/index.html#spark.util.Vector) class provided by Spark. For Python, we will be using [NumPy arrays](http://docs.scipy.org/doc/numpy/reference/generated/numpy.array.html).

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
   val data = sc.textFile(
       "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
           t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()
   val count = data.count()
   println("Number of records " + count)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaPairRDD<String, Vector> data = sc.textFile(
      "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
        new PairFunction<String, String, Vector>() {
          public Tuple2<String, Vector> call(String in)
          throws Exception {
            String[] parts = in.split("#");
            return new Tuple2<String, Vector>(parts[0], parseVector(parts[1]));
          }
        }
      ).cache();
    long count = data.count();
    System.out.println("Number of records " + count);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    lines = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized_hash_text")
    data = lines.map(
        lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()
    count = data.count()
    print "Number of records " + str(count)
~~~
</div>
</div>

Now, let's make our first edit to the file and add code to count the number of records in our dataset by running `data.count()` and print the value. Find the comment that says "Your code goes here" and replace it with:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val count = data.count()
    println("Number of records " + count)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    long count = data.count();
    System.out.println("Number of records " + count);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    count = data.count()
    print "Number of records " + str(count)
~~~
</div>
</div>

## Running the program
Before we implement the K-Means algorithm, here is quick reminder on how you can run the program at any point during this exercise. Save the `WikipediaKMeans` file run the following commands:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/scala
sbt/sbt package run
</pre>

This command will compile the `WikipediaKMeans` class and create a JAR file in `/root/kmeans/scala/target/scala-2.9.2/`. Finally, it will run the program. You should see output similar to the following on your screen:

</div>
<div data-lang="java" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/java
sbt/sbt package run
</pre>

This command will compile the `WikipediaKMeans` class and create a JAR file in `/root/kmeans/java/target/scala-2.9.2/`. Finally, it will run the program. You should see output similar to the following on your screen:

</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/python
/root/spark/pyspark ./WikipediaKMeans.py
</pre>

This command will run `WikipediaKMeans` on your Spark cluster. You should see output similar to the following on your screen:

</div>
</div>

<pre class="prettyprint lang-bsh">
Number of records 802450
</pre>

## K-Means algorithm
We are now set to start implementing the K-means algorithm, so remove or comment out the lines we just added to print the record count and let's get started.

1. The first step in the K-Means algorithm is to initialize our centers by randomly picking `K` points from our dataset. We use the `takeSample` function in Spark to do this.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var centroids = data.takeSample(false, K, 42).map(x => x._2)
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      List<Tuple2<String, Vector>> centroidTuples = data.takeSample(false, K, 42);
      final List<Vector> centroids = Lists.newArrayList();
      for (Tuple2<String, Vector> t: centroidTuples) {
        centroids.add(t._2());
      }
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      # NOTE: PySpark does not support takeSample() yet. Use first K points instead.
      centroids = map(lambda (x, y): y, data.take(K))
~~~
    </div>
    </div>

1. Next, we need to compute the closest centroid for each point and we do this by using the `map` operation in Spark. For every point we will use a function `closestPoint` (which you have to write!) to compute the closest centroid.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var closest = data.map(p => (closestPoint(p._2, centroids), p._2)) // Won't work until you write closestPoint()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      JavaPairRDD<Integer, Vector> closest = data.map(
        new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
          public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
            return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
          }
        }
      );
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      closest = data.map(
          lambda (x, y) : (closestPoint(y, centroids), y))
~~~
    </div>
    </div>

    **Exercise:** Write the `closestPoint` function in `WikipediaKMeans` to return the index of the closest centroid given a point and the set of all centroids. To get you started, we provide the type signature of the function:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      static int closestPoint(Vector p, List<Vector> centers) {
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      def closestPoint(p, centers):
~~~
    </div>
    </div>

    In case you get stuck, you can use our solution given here:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
        var bestIndex = 0
        var closest = Double.PositiveInfinity
        for (i <- 0 until centers.length) {
          val tempDist = p.squaredDist(centers(i))
          if (tempDist < closest) {
            closest = tempDist
            bestIndex = i
          }
        }
        return bestIndex
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      static int closestPoint(Vector p, List<Vector> centers) {
        int bestIndex = 0;
        double closest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < centers.size(); i++) {
          double tempDist = p.squaredDist(centers.get(i));
          if (tempDist < closest) {
            closest = tempDist;
            bestIndex = i;
          }
        }
        return bestIndex;
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      def closestPoint(p, centers):
          bestIndex = 0
          closest = float("+inf")
          for i in range(len(centers)):
              dist = np.sum((p - centers[i]) ** 2)
              if dist < closest:
                  closest = dist
                  bestIndex = i
          return bestIndex
~~~
    </div>
    </div>
    </div>

    On the line defining the variable called `closest`, the `map` operation creates a new RDD which contains a tuple for every point. The first element in the tuple is the index of the closest centroid for the point and second element is the `Vector` representing the point.

1. Now that we have the closest centroid for each point, we can cluster our points by the centroid they belong to. To do this, we use a `groupByKey` operation as shown below. The `groupByKey` operator creates a new `RDD[(Int, Array[Vector])]` where the key is the index of the centroid and the values are all the points which belong to its cluster.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var pointsGroup = closest.groupByKey()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      pointsGroup = closest.groupByKey()
~~~
    </div>
    </div>

1. We can now calculate our new centroids by computing the mean of the points that belong to a cluster. We do this using `mapValues` which allows us to apply a function on all the values for a particular key. We also use a function `average` to compute the average of an array of vectors.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
        new Function<List<Vector>, Vector>() {
          public Vector call(List<Vector> ps) throws Exception {
            return average(ps);
          }
        }).collectAsMap();
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      newCentroids = pointsGroup.mapValues(
          lambda x : average(x)).collectAsMap()
~~~
    </div>
    </div>

    **Exercise:** Write the `average` function in `WikipediaKMeans` to sum all the vectors and divide it by the number of vectors present in the input array. Your function should return a new Vector which is the average of the input vectors. You can look at our solution in case you get stuck.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      def average(ps: Seq[Vector]) : Vector = {
        val numVectors = ps.size
        var out = new Vector(ps(0).elements)
        for (i <- 1 until numVectors) {
          out += ps(i)
        }
        out / numVectors
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      static Vector average(List<Vector> ps) {
        int numVectors = ps.size();
        Vector out = new Vector(ps.get(0).elements());
        for (int i = 0; i < numVectors; i++) {
          out.addInPlace(ps.get(i));
        }
        return out.divide(numVectors);
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      def average(points):
          numVectors = len(points)
          out = np.array(points[0])
          for i in range(2, numVectors):
              out += points[i]
          out = out / numVectors
          return out
~~~
    </div>
    </div>
    </div>

1. Finally, lets calculate how different our new centroids are compared to our initial centroids. This will be used to determine if we have converged to the right set of centroids. To do this we create a variable named `tempDist` that represents the distance between the old centroids and new centroids. In Scala and Java, we use the `squaredDist` function to compute the distance between two vectors. For Python we can use `np.sum`. We sum up the distance over `K` centroids and print this value.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var tempDist = 0.0
      for (i <- 0 until K) {
        tempDist += centroids(i).squaredDist(newCentroids(i))
      }
      println("Finished iteration (delta = " + tempDist + ")")
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      double tempDist = 0.0;
      for (int i = 0; i < K; i++) {
        tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
      }
      for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
        centroids.set(t.getKey(), t.getValue());
      }
      System.out.println("Finished iteration (delta = " + tempDist + ")");
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
      print "Finished iteration (delta = " + str(tempDist) + ")"
~~~
    </div>
    </div>

    You can now save `WikipediaKMeans`, and [run it](#running-the-program) to make sure our program is working fine so far. If everything went right, you should see the output similar to the following. (NOTE: Your output may not exactly match this as we use a random set of initial centers).

    <pre class="prettyprint lang-bsh">Finished iteration (delta = 0.025900765093161377)</pre>

1. The above steps represent one iteration of the K-Means algorithm. We will need to repeat these steps until the distance between newly computed centers and the ones from the previous iteration become lesser than some small constant. (`convergeDist` in our program). To do this we put our steps inside a `do while` loop and check if `tempDist` is lower than the convergence constant. Putting this together with the previous steps, our code will look like:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      do {
        var closest = data.map(p => (closestPoint(p._2, centroids), p._2))
        var pointsGroup = closest.groupByKey()
        var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()

        tempDist = 0.0
        for (i <- 0 until K) {
          tempDist += centroids(i).squaredDist(newCentroids(i))
        }

        // Assign newCentroids to centroids
        for (newP <- newCentroids) {
          centroids(newP._1) = newP._2
        }
        iter += 1
        println("Finished iteration " + iter + " (delta = " + tempDist + ")")
      } while (tempDist > convergeDist)
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      do {
          JavaPairRDD<Integer, Vector> closest = data.map(
            new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
              public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
                return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
              }
             }
            );
          JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
          Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
            new Function<List<Vector>, Vector>() {
              public Vector call(List<Vector> ps) throws Exception {
                return average(ps);
              }
            }).collectAsMap();
          double tempDist = 0.0;
          for (int i = 0; i < K; i++) {
            tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
          }
          for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
            centroids.set(t.getKey(), t.getValue());
          }
          System.out.println("Finished iteration (delta = " + tempDist + ")");

        } while (tempDist > convergeDist);
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
    while tempDist > convergeDist:
        closest = data.map(
            lambda (x, y) : (closestPoint(y, centroids), y))
        pointsGroup = closest.groupByKey()
        newCentroids = pointsGroup.mapValues(
            lambda x : average(x)).collectAsMap()

        tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
        for (x, y) in newCentroids.iteritems():
            centroids[x] = y
        print "Finished iteration (delta = " + str(tempDist) + ")"
~~~
    </div>
    </div>

1. **Exercise:** After the `do while` loop completes, write code to print the titles of 10 articles assigned to each cluster.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      val numArticles = 10
      for((centroid, centroidI) <- centroids.zipWithIndex) {
        // print numArticles articles which are assigned to this centroid’s cluster
        data.filter(p => (closestPoint(p._2, centroids) == centroidI)).take(numArticles).foreach(
            x => println(x._1))
        println()
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      System.out.println("Cluster with some articles:");
      int numArticles = 10;
      for (int i = 0; i < centroids.size(); i++) {
        final int index = i;
        List<Tuple2<String, Vector>> samples =
          data.filter(new Function<Tuple2<String, Vector>, Boolean>() {
            public Boolean call(Tuple2<String, Vector> in) throws Exception {
              return closestPoint(in._2(), centroids) == index;
            }
          }).take(numArticles);
        for(Tuple2<String, Vector> sample: samples) {
          System.out.println(sample._1());
        }
        System.out.println();
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      print "Clusters with some articles"
      numArticles = 10
      for i in range(0, len(centroids)):
        samples = data.filter(lambda (x,y) : closestPoint(y, centroids) == i).take(numArticles)
        for (name, features) in samples:
          print name

        print " "
~~~
    </div>
    </div>
    </div>

1. You can save `WikipediaKMeans` and [run your program](#running-the-program) now. If everything goes well your algorithm will converge after some iterations and your final output should have clusters similar to the following output. Recall that our feature vector consisted of the number of times a page was visited in every hour of the day. We can see that pages are clustered together by _language_ indicating that they are accessed during the same hours of the day.

    <pre class="nocode">
    ja %E6%AD%8C%E8%97%A4%E9%81%94%E5%A4%AB
    ja %E7%8B%90%E3%81%AE%E5%AB%81%E5%85%A5%E3%82%8A
    ja %E3%83%91%E3%82%B0
    ja %E7%B4%AB%E5%BC%8F%E9%83%A8
    ja %E3%81%8A%E5%A7%89%E7%B3%BB
    ja Reina
    ja %E8%B8%8A%E3%82%8A%E5%AD%97
    ja %E3%83%90%E3%82%BB%E3%83%83%E3%83%88%E3%83%8F%E3%82%A6%E3%83%B3%E3%83%89
    ja %E3%81%BF%E3%81%9A%E3%81%BB%E3%83%95%E3%82%A3%E3%83%8A%E3%83%B3%E3%82%B7%E3%83%A3%E3%83%AB%E3%82%B0%E3%83%AB%E3%83%BC%E3%83%97
    ja %E6%96%B0%E6%BD%9F%E7%9C%8C%E7%AB%8B%E6%96%B0%E6%BD%9F%E5%8D%97%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1

    ru %D0%A6%D0%B8%D1%80%D0%BA%D0%BE%D0%BD%D0%B8%D0%B9
    de Kirchenstaat
    ru %D0%90%D0%B2%D1%80%D0%B0%D0%B0%D0%BC
    de Migr%C3%A4ne
    de Portal:Freie_Software
    de Datenflusskontrolle
    de Dornier_Do_335
    de.b LaTeX-Schnellkurs:_Griechisches_Alphabet
    de Mach-Zahl
    ru Total_Commander

    en 761st_Tank_Battalion_(United_States)
    en File:N34---Douglas-DC3-Cockpit.jpg
    en Desmond_Child
    en Philadelphia_Freeway
    en Zygon
    en File:Ruth1918.jpg
    en Sally_Rand
    en File:HLHimmler.jpg
    en Waiting_to_Exhale
    en File:Sonic1991b.jpg
    </pre>

1. In case you want to look at the complete solution, here is how `WikipediaKMeans` will look after all the above steps have been completed.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
    import spark.SparkContext
    import spark.SparkContext._
    import spark.util.Vector

    import org.apache.log4j.Logger
    import org.apache.log4j.Level

    import scala.util.Random
    import scala.io.Source

    object WikipediaKMeans {
      def parseVector(line: String): Vector = {
          return new Vector(line.split(',').map(_.toDouble))
      }

      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
        var index = 0
        var bestIndex = 0
        var closest = Double.PositiveInfinity
        for (i <- 0 until centers.length) {
          val tempDist = p.squaredDist(centers(i))
          if (tempDist < closest) {
            closest = tempDist
            bestIndex = i
          }
        }
        return bestIndex
      }

      def average(ps: Seq[Vector]) : Vector = {
        val numVectors = ps.size
        var out = new Vector(ps(0).elements)
        for (i <- 1 until numVectors) {
          out += ps(i)
        }
        out / numVectors
      }

      // Add any new functions you need here

      def main(args: Array[String]) {
        Logger.getLogger("spark").setLevel(Level.WARN)
        val sparkHome = "/root/spark"
        val jarFile = "target/scala-2.9.2/wikipedia-kmeans_2.9.2-0.0.jar"
        val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
        val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim

        val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))

        val K = 10
        val convergeDist = 1e-6

        val data = sc.textFile(
            "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
                t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()

        val count = data.count()
        println("Number of records " + count)

        // Your code goes here
        var centroids = data.takeSample(false, K, 42).map(x => x._2)
        var tempDist = 1.0
        do {
          var closest = data.map(p => (closestPoint(p._2, centroids), p._2))
          var pointsGroup = closest.groupByKey()
          var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()
          tempDist = 0.0
          for (i <- 0 until K) {
            tempDist += centroids(i).squaredDist(newCentroids(i))
          }
          for (newP <- newCentroids) {
            centroids(newP._1) = newP._2
          }
          println("Finished iteration (delta = " + tempDist + ")")
        } while (tempDist > convergeDist)

        println("Clusters with some articles:")
        val numArticles = 10
        for((centroid, centroidI) <- centroids.zipWithIndex) {
          // print numArticles articles which are assigned to this centroid’s cluster
          data.filter(p => (closestPoint(p._2, centroids) == centroidI)).take(numArticles).foreach(
              x => println(x._1))
          println()
        }

        sc.stop()
        System.exit(0)
      }
    }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
    import org.apache.log4j.Logger;
    import org.apache.log4j.Level;
    import java.io.*;
    import java.util.*;
    import com.google.common.collect.Lists;

    import scala.Tuple2;
    import spark.api.java.*;
    import spark.api.java.function.*;
    import spark.util.Vector;


    public class WikipediaKMeans {
      static int closestPoint(Vector p, List<Vector> centers) {
        int bestIndex = 0;
        double closest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < centers.size(); i++) {
          double tempDist = p.squaredDist(centers.get(i));
          if (tempDist < closest) {
            closest = tempDist;
            bestIndex = i;
          }
        }
        return bestIndex;
      }

      static Vector average(List<Vector> ps) {
        int numVectors = ps.size();
        Vector out = new Vector(ps.get(0).elements());
        for (int i = 0; i < numVectors; i++) {
          out.addInPlace(ps.get(i));
        }
        return out.divide(numVectors);
      }

      public static void main(String[] args) throws Exception {
        Logger.getLogger("spark").setLevel(Level.WARN);
        String sparkHome = "/root/spark";
        String jarFile = "target/scala-2.9.2/wikipedia-kmeans_2.9.2-0.0.jar";
        String master = JavaHelpers.getSparkUrl();
        String masterHostname = JavaHelpers.getMasterHostname();
        JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans",
          sparkHome, jarFile);

        int K = 10;
        double convergeDist = .000001;

        JavaPairRDD<String, Vector> data = sc.textFile(
          "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
          new PairFunction<String, String, Vector>() {
            public Tuple2<String, Vector> call(String in) throws Exception {
              String[] parts = in.split("#");
              return new Tuple2<String, Vector>(
               parts[0], JavaHelpers.parseVector(parts[1]));
            }
          }).cache();


        long count = data.count();
        System.out.println("Number of records " + count);

        List<Tuple2<String, Vector>> centroidTuples = data.takeSample(false, K, 42);
        final List<Vector> centroids = Lists.newArrayList();
        for (Tuple2<String, Vector> t: centroidTuples) {
          centroids.add(t._2());
        }

        System.out.println("Done selecting initial centroids");
        double tempDist;
        do {
          JavaPairRDD<Integer, Vector> closest = data.map(
            new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
              public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
                return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
              }
            }
          );

          JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
          Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
            new Function<List<Vector>, Vector>() {
             public Vector call(List<Vector> ps) throws Exception {
               return average(ps);
            }
          }).collectAsMap();
          tempDist = 0.0;
          for (int i = 0; i < K; i++) {
            tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
          }
          for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
            centroids.set(t.getKey(), t.getValue());
          }
          System.out.println("Finished iteration (delta = " + tempDist + ")");

        } while (tempDist > convergeDist);

        System.out.println("Cluster with some articles:");
        int numArticles = 10;
        for (int i = 0; i < centroids.size(); i++) {
          final int index = i;
          List<Tuple2<String, Vector>> samples =
          data.filter(new Function<Tuple2<String, Vector>, Boolean>() {
            public Boolean call(Tuple2<String, Vector> in) throws Exception {
            return closestPoint(in._2(), centroids) == index;
          }}).take(numArticles);
          for(Tuple2<String, Vector> sample: samples) {
           System.out.println(sample._1());
          }
          System.out.println();
        }
        sc.stop();
        System.exit(0);
      }
    }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
   import os
   import sys

   import numpy as np
   from pyspark import SparkContext

   def setClassPath():
       oldClassPath = os.environ.get('SPARK_CLASSPATH', '')
       cwd = os.path.dirname(os.path.realpath(__file__))
       os.environ['SPARK_CLASSPATH'] = cwd + ":" + oldClassPath

   def parseVector(line):
       return np.array([float(x) for x in line.split(',')])

   def closestPoint(p, centers):
       bestIndex = 0
       closest = float("+inf")
       for i in range(len(centers)):
           dist = np.sum((p - centers[i]) ** 2)
           if dist < closest:
               closest = dist
               bestIndex = i
       return bestIndex

   def average(points):
       numVectors = len(points)
       out = np.array(points[0])
       for i in range(2, numVectors):
           out += points[i]
       out = out / numVectors
       return out

   if __name__ == "__main__":
       setClassPath()
       master = open("/root/spark-ec2/cluster-url").read().strip()
       masterHostname = open("/root/spark-ec2/masters").read().strip()
       sc = SparkContext(master, "PythonKMeans")
       K = 10
       convergeDist = 1e-5

       lines = sc.textFile(
           "hdfs://" + masterHostname + ":9000/wikistats_featurized")

       data = lines.map(
           lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()
       count = data.count()
       print "Number of records " + str(count)

       # TODO: PySpark does not support takeSample(). Use first K points instead.
       centroids = map(lambda (x, y): y, data.take(K))
       tempDist = 1.0

       while tempDist > convergeDist:
           closest = data.map(
               lambda (x, y) : (closestPoint(y, centroids), y))
           pointsGroup = closest.groupByKey()
           newCentroids = pointsGroup.mapValues(
               lambda x : average(x)).collectAsMap()

           tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
           for (x, y) in newCentroids.iteritems():
               centroids[x] = y
           print "Finished iteration (delta = " + str(tempDist) + ")"
           sys.stdout.flush()


       print "Clusters with some articles"
       numArticles = 10
       for i in range(0, len(centroids)):
         samples = data.filter(lambda (x,y) : closestPoint(y, centroids) == i).take(numArticles)
         for (name, features) in samples:
           print name

         print " "
~~~
    </div>
    </div>
    </div>

1. **Challenge Exercise:** The K-Means implementation uses a `groupBy` and `mapValues` to compute the new centers. This can be optimized by using a running sum of the vectors that belong to a cluster and running counter of the number of vectors present in a cluster. How would you use the Spark API to implement this?

# Where to Go From Here - More Resources and Further Reading

- [Spark API reference](http://www.cs.berkeley.edu/~pwendell/strataconf/api/core/index.html#spark.package) (Java/Scala)
- [Spark API reference](http://www.cs.berkeley.edu/~pwendell/strataconf/api/pyspark/index.html) (Python)
- [Spark Streaming API Reference](http://www.cs.berkeley.edu/~pwendell/strataconf/api/streaming/index.html#spark.streaming.package) (Java/Scala)
- [Official Spark website](http://spark-project.org) - Find examples, documentation, downloads, research papers, news, and more!
- [Official Shark website](http://shark.cs.berkeley.edu)
- [Spark on github](http://github.com/mesos/spark) - The official repository.
- Check out talks (videos and slides) from the [first AMP Camp Big Data Bootcamp](http://ampcamp.berkeley.edu)
- The Spark mailing lists:
    - [spark-users](http://groups.google.com/group/spark-users) is for usage questions, help, and announcements
    - [spark-developers](http://groups.google.com/group/spark-developers) is for people who want to contribute code to Spark
- The [Spark issue tracker](https://spark-project.atlassian.net/browse/SPARK)
- Various [Spark related articles on Quora](http://www.quora.com/Spark-Cluster-Computing)
- Some blog posts about Spark:
    - [Top 5 Open Source Projects in Big Data](http://siliconangle.com/blog/2013/02/04/top-5-open-source-projects-in-big-data-breaking-analysis/). By Molly Sassmann, February 4, 2013
    - [Big Data Up to 100X Faster – Researchers Crank Up the Speed Dial](http://siliconangle.com/blog/2012/11/29/big-data-up-to-100x-faster-researchers-crank-up-the-speed-dial/). By John Casaretto, November 29, 2012
    - [Introduction to Spark, Shark, BDAS and AMPLab](http://www.dbms2.com/2012/12/13/introduction-to-spark-shark-bdas-and-amplab/). On DBMS2, a blog by Curt Monash, December 13, 2012
    - [Spark, Shark, and RDDs — technology notes](http://www.dbms2.com/2012/12/13/spark-shark-and-rdds-technology-notes/). On DBMS2, a blog by Curt Monash, December 13, 2012
    - [Unit testing with Spark](http://blog.quantifind.com/posts/spark-unit-test/). On Quantfind’s Blog, by Imran Rashid, January 4, 2013
    - [Configuring Spark’s logs](http://blog.quantifind.com/posts/logging-post/). On Quantfind’s Blog, by Imran Rashid, January 4, 2013
    - [What Makes Spark Exciting](http://dev.bizo.com/2013/01/what-makes-spark-exciting.html). On Bizo Development Blog by Stephen Haberman, January 21, 2013
    - [The future of big data with BDAS, the Berkeley Data Analytics Stack](http://strata.oreilly.com/2013/02/the-future-of-big-data-with-bdas-the-berkeley-data-analytics-stack.html#more-54859). On the O’Reilly Strata blog, by Andy Konwinski, Ion Stoica, Matei Zaharia, February 18, 2013
    - [Five big data predictions for 2013](http://strata.oreilly.com/2013/01/five-big-data-predictions-for-2013.html). On the O’Reilly Strata blog, by Ed Dumbill, January 16, 2013
    - [Shark: Real-time queries and analytics for big data](http://strata.oreilly.com/2012/11/shark-real-time-queries-and-analytics-for-big-data.html). On the O’Reilly Strata blog, by Ben Lorica, November 27, 2012
    - [Seven reasons why I like Spark](http://strata.oreilly.com/2012/08/seven-reasons-why-i-like-spark.html). On the O’Reilly Strata blog, by Ben Lorica, August 21, 2012 
