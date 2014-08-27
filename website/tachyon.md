---
layout: global
title: Tachyon - Reliable File Sharing at Memory Speed Across Cluster Frameworks
categories: [module]
navigation:
  weight: 90
  show: true
---

{:toc}
<!--
<p style="text-align: center;">
  <img src="img/tachyon-logo.jpg"
       title="Tachyon Logo"
       alt="Tachyon"
       width="65%" />
</p>
 -->

In-memory data processing has gained tremendous attention recently. People use in-memory computation
frameworks to perform fast and interactive queries. However, there are still several problems that
remain unsolved.

1. Slow data sharing in a workflow: Companies build complicated workflows to process data, and use
distributed file systems, such as HDFS or S3, to share one job's output as other jobs' input. Even
though in-memory computation is fast, writing output data is slow, which is bounded by either disk
or network bandwidth.

2. Duplicated input data for different jobs: Without a data sharing service that operates at memory
speed, applications, written in frameworks such as Spark or MapReduce, are forced to store things
in-memory local to their own, even when they share the same input. This multiplies the amount of
memory needed.

3. Lost cache when JVM crashes: In-memory storage engine and execution engine co-exist in the same
JVM in computation frameworks, such as Spark. In this case, if the JVM crashes, all the in-memory
data is lost, and the next program has to load the data into memory again, which could be time
consuming.

The Tachyon project to address the above issues. Tachyon is a fault tolerant distributed file
system, which enables reliable file sharing at memory-speed across cluster frameworks, such as Spark
and MapReduce. It achieves high performance by leveraging lineage (alpha) information and using
memory aggressively. Tachyon caches working set files in memory, and enables different jobs/queries
and frameworks to access cached files at memory speed. Thus, Tachyon avoids going to disk to load
datasets that are frequently read.

In this chapter we first go over basic operations of Tachyon, and then run a Spark program on top
of it. For more information, please visit Tachyon's [website](http://tachyon-project.org) or Github
[repository](https://github.com/amplab/tachyon).

## Launch Tachyon

### Configurations

All system's configuration is under `tachyon/conf` folder. Please find them, and see how much
memory is configured on each worker node?

<div class="solution" markdown="1">
~~~
$ grep "TACHYON_WORKER_MEMORY_SIZE=" conf/tachyon-env.sh
$ export TACHYON_WORKER_MEMORY_SIZE=12936MB
~~~
</div>

You can also read the through the file and try to understand those parameters. For more information
on configuration, you can visit
[Tachyon's Configuration Settings webpage](http://tachyon-project.org/Configuration-Settings.html).

### Format the storage

Before starting Tachyon for the first time, we need to format the system. It can be done by using
`tachyon` script in the `tachyon/bin` folder. Please type the following command first, and then
learn how to format the Tachyon file system for the first time.

~~~
$ ./bin/tachyon
~~~

<div class="solution" markdown="1">
~~~
$ ./bin/tachyon format
$ Formatting Tachyon @ localhost
$ Deleting /home/haoyuan/Tachyon/tachyon/libexec/../journal/
$ Formatting hdfs://localhost:9000/tmp/tachyon/data
$ Formatting hdfs://localhost:9000/tmp/tachyon/workers
~~~
</div>

### Start the system

After formatting the storeage, we can finally try to start the system. This can be done by using
`tachyon/bin/tachyon-start.sh` script.

~~~
$ ./bin/tachyon-start.sh all Mount
$ Killed 0 processes
$ Killed 0 processes
$ localhost: Killed 0 processes
$ Formatting RamFS: /mnt/ramdisk (1.1gb)
$ Starting master @ localhost
$ Starting worker @ hy-ubuntu
~~~

## Interacting with Tachyon

In this section, we will go over three approaches to interact with Tachyon:

1. Command Line Interface
2. Application Programming Interface
3. Web User Interface

### Command Line Interface

You can interact with Tachyon using the following command:

~~~
$ ./bin/tachyon tfs
~~~

Then, it will return a list of options:

~~~
$ Usage: java TFsShell
$        [cat <path>]
$        [count <path>]
$        [ls <path>]
$        [lsr <path>]
$        [mkdir <path>]
$        [rm <path>]
$        [tail <path>]
$        [touch <path>]
$        [mv <src> <dst>]
$        [copyFromLocal <src> <remoteDst>]
$        [copyToLocal <src> <localDst>]
$        [fileinfo <path>]
$        [location <path>]
$        [report <path>]
$        [request <tachyonaddress> <dependencyId>]
~~~

Please try to put the local file `tachyon/LICENSE` into Tachyon file system as /LICENSE.txt using
command line.

<div class="solution" markdown="1">
~~~
$ ./bin/tachyon tfs copyFromLocal LICENSE /LICENSE.txt
$ Copied LICENSE to /LICENSE.txt
~~~
</div>

You can also use command line interface to verify this:

<div class="solution" markdown="1">
~~~
$ ./bin/tachyon tfs ls /
$ 11.40 KB  02-07-2014 23:23:44:008  In Memory      /LICENSE.txt
~~~
</div>

Now, you want to check out the conent of the file:

<div class="solution" markdown="1">
~~~
$ ./bin/tachyon tfs cat /LICENSE.txt
$                                  Apache License
$                           Version 2.0, January 2004
$                        http://www.apache.org/licenses/
$
$   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION
$ ....
~~~
</div>

### Application Programming Interface

After using command line to interact with Tachyon, you can also use its API. We have several sample
[applications](https://github.com/amplab/tachyon/tree/master/main/src/main/java/tachyon/examples).
For example, [BasicOperations.java](https://github.com/amplab/tachyon/blob/master/main/src/main/java/tachyon/examples/BasicOperations.java)
shows how to user file create, write, and read operations.

You have put these into our script, you can simply use the following command to run this sample
program. The following command runs [BasicOperations.java], and also verifies Tachyon's
installation.

<div class="solution" markdown="1">
~~~
$ ./bin/tachyon runTests
$ /root/tachyon/bin/tachyon runTest Basic MUST_CACHE
$ /BasicFile_MUST_CACHE has been removed
$ 2014-02-07 23:46:57,529 INFO   (TachyonFS.java:connect) - Trying to connect master @ ec2-23-20-202-253.compute-1.amazonaws.com/10.91.151.150:19998
$ 2014-02-07 23:46:57,599 INFO   (MasterClient.java:getUserId) - User registered at the master ec2-23-20-202-253.compute-1.amazonaws.com/10.91.151.150:19998 got UserId 6
$ 2014-02-07 23:46:57,600 INFO   (TachyonFS.java:connect) - Trying to get local worker host : ip-10-91-151-150.ec2.internal
$ 2014-02-07 23:46:57,618 INFO   (TachyonFS.java:connect) - Connecting local worker @ ip-10-91-151-150.ec2.internal/10.91.151.150:29998
$ 2014-02-07 23:46:57,661 INFO   (CommonUtils.java:printTimeTakenMs) - createFile with fileId 3 took 133 ms.
$ 2014-02-07 23:46:57,707 INFO   (TachyonFS.java:createAndGetUserTempFolder) - Folder /mnt/ramdisk/tachyonworker/users/6 was created!
$ 2014-02-07 23:46:57,714 INFO   (BlockOutStream.java:<init>) - /mnt/ramdisk/tachyonworker/users/6/3221225472 was created!
$ Passed the test!
$ /root/tachyon/bin/tachyon runTest BasicRawTable MUST_CACHE
$ /BasicRawTable_MUST_CACHE has been removed
$ 2014-02-07 23:46:58,633 INFO   (TachyonFS.java:connect) - Trying to connect master @ ec2-23-20-202-253.compute-1.amazonaws.com/10.91.151.150:19998
$ 2014-02-07 23:46:58,705 INFO   (MasterClient.java:getUserId) - User registered at the master ec2-23-20-202-253.compute-1.amazonaws.com/10.91.151.150:19998 got UserId 8
$ 2014-02-07 23:46:58,706 INFO   (TachyonFS.java:connect) - Trying to get local worker host : ip-10-91-151-150.ec2.internal
$ 2014-02-07 23:46:58,725 INFO   (TachyonFS.java:connect) - Connecting local worker @ ip-10-91-151-150.ec2.internal/10.91.151.150:29998
$ 2014-02-07 23:46:58,859 INFO   (TachyonFS.java:createAndGetUserTempFolder) - Folder /mnt/ramdisk/tachyonworker/users/8 was created!
$ 2014-02-07 23:46:58,866 INFO   (BlockOutStream.java:<init>) - /mnt/ramdisk/tachyonworker/users/8/8589934592 was created!
$ 2014-02-07 23:46:58,904 INFO   (BlockOutStream.java:<init>) - /mnt/ramdisk/tachyonworker/users/8/9663676416 was created!
$ 2014-02-07 23:46:58,914 INFO   (BlockOutStream.java:<init>) - /mnt/ramdisk/tachyonworker/users/8/10737418240 was created!
$ Passed the test!
$ ...
~~~
</div>

### Web User Interface

After using commands and API to interact with Tachyon, let's take a look at its web user interface.
The URI is `http://ec2masterhost:19999`.

The first page is the cluster's summary. If you click on the `Browse File System`, it shows you
all the files you just created and copied.

You can also click a particular file or folder. e.g. `/LICENSE.txt` file, and then you will see the
detailed information about it.

## Run Spark on Tachyon

In this section, we run a Spark program to interact with Tachyon. The first one is to do a word
count on `/LICENSE.txt` file. In `/root/spark` folder, execute the following command to start
Spark shell.

~~~
$ ./bin/spark-shell
~~~

<div class="solution" markdown="1">
<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
var file = sc.textFile("tachyon://ec2masterhostname:19998/LICENSE.txt")
val counts = file.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey(_ + _)
counts.saveAsTextFile("tachyon://ec2masterhostname:19998/result")
~~~
</div>
<div data-lang="java" markdown="1">
~~~
JavaRDD<String> file = spark.textFile("tachyon://ec2masterhostname:19998/LICENSE.txt");
JavaRDD<String> words = file.flatMap(new FlatMapFunction<String, String>()
  public Iterable<String> call(String s) { return Arrays.asList(s.split(" ")); }
});
JavaPairRDD<String, Integer> pairs = words.map(new PairFunction<String, String, Integer>()
  public Tuple2<String, Integer> call(String s) { return new Tuple2<String, Integer>(s, 1); }
});
JavaPairRDD<String, Integer> counts = pairs.reduceByKey(new Function2<Integer, Integer>()
  public Integer call(Integer a, Integer b) { return a + b; }
});
counts.saveAsTextFile("tachyon://ec2masterhostname:19998/result");
~~~
</div>
<div data-lang="python" markdown="1">
~~~
file = spark.textFile("tachyon://ec2masterhostname:19998/LICENSE.txt")
counts = file.flatMap(lambda line: line.split(" ")) \
             .map(lambda word: (word, 1)) \
             .reduceByKey(lambda a, b: a + b)
counts.saveAsTextFile("tachyon://ec2masterhostname:19998/result")
~~~
</div>
</div>
</div>

The results are stored in `/result` folder. You can verfy the results through Web UI or commands.
Because `\LICENSE.txt` is in memory, when a new Spark program comes up, it can load in memory data
directly from Tachyon. In the meantime, we are also working on other features to make Tachyon
further enhance Spark's performance.

This brings us to the end of the Tachyon chapter of the tutorial. We encourage you to continue
playing with the code and to check out the [project website](http://tachyon-project.org/) or Github
[repository](https://github.com/amplab/tachyon) for further information.

Bug reports and feature requests are welcomed.
