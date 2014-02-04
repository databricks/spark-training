---
layout: global
title: Tachyon - Reliable File Sharing at Memory Speed Across Cluster Frameworks
categories: [module]
navigation:
  weight: 75
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

<!-- In this chapter we use GraphX to analyze Wikipedia data and implement graph algorithms in Spark. As with other exercises we will work with a subset of the Wikipedia traffic statistics data from May 5-7, 2009. In particular, this dataset only includes a subset of all Wikipedia articles. -->

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

In this chapter we first go over basic operations of Tachyon, and then run two Spark programs on top
of it. For more information, please visit Tachyon's [website](http://tachyon-project.org/).

## Launch Tachyon

### Configurations

All system's configuration is under `tachyon/conf` folder. Please find them, and see how much
memory is configured on each worker node?

<div class="solution" markdown="1">
~~~
$ grep "TACHYON_WORKER_MEMORY_SIZE=" conf/tachyon-env.sh
$ export TACHYON_WORKER_MEMORY_SIZE=1.1GB
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

There are several approaches to interact with Tachyon:

1. Command Line Interface
2. Application Programming Interface
3. Web User Interface

In this section, we will go through them one by one.

### Command Line Interface

You can start to interact with Tachyon using the following command:

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

Please, could you put the `tachyon/LICENSE` file into Tachyon file system as /LICENSE.txt?

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
$ 11.40 KB  02-04-2014 01:45:31:778  Not In Memory  /LICENSE.txt
~~~
</div>

Note, unless there is a Tachyon worker running on the machine where this copyFromLocal is executed,
LICENSE.txt file will not be in the memory. Now, you want to see the file's content:

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
applications, for example: [BasicOperations.java](https://github.com/amplab/tachyon/blob/master/src/main/java/tachyon/examples/BasicOperations.java)

You have put these into our script, you can simple use the following command to run this sample
program. The following command also verifies the correct installation of the system.

<div class="solution" markdown="1">
~~~
$ ./bin/tachyon runTests
$ /home/haoyuan/Tachyon/tachyon/bin/tachyon runTest Basic MUST_CACHE
$ /BasicFile_MUST_CACHE has been removed
$ 2014-02-04 01:51:56,054 INFO   (TachyonFS.java:connect) - Trying to connect master @ localhost/127.0.0.1:19998
$ 2014-02-04 01:51:56,088 INFO   (MasterClient.java:getUserId) - User registered at the master localhost/127.0.0.1:19998 got UserId 5
$ 2014-02-04 01:51:56,088 INFO   (TachyonFS.java:connect) - Trying to get local worker host : hy-ubuntu
$ 2014-02-04 01:51:56,095 INFO   (TachyonFS.java:connect) - Connecting local worker @ hy-ubuntu/127.0.1.1:29998
$ 2014-02-04 01:51:56,114 INFO   (CommonUtils.java:printTimeTakenMs) - createFile with fileId 3 took 62 ms.
$ 2014-02-04 01:51:56,133 INFO   (TachyonFS.java:createAndGetUserTempFolder) - Folder /mnt/ramdisk/tachyonworker/users/5 was created!
$ 2014-02-04 01:51:56,138 INFO   (BlockOutStream.java:<init>) - /mnt/ramdisk/tachyonworker/users/5/3221225472 was created!
$ Passed the test!
$ ...
~~~
</div>

### Web User Interface

Now, after running all these commands and executing the applications, let's checkout Tachyon's
web user interface. You can access it by put `http://ec2masterhost:19999` into your browser.

The first page has summary information about the cluster. If you click on the `Browse File System`,
it will show you all the files you just created or copied.

You can also click a particular file or folder. e.g. `/LICENSE.txt` file, and then you will see the
detailed information about it.

## Run Spark on Tachyon

In this section, we run a Spark program to interact with Tachyon. The first one is to do a word
count on `/LICENSE.txt` file.

~~~
$ ./spark-shell
~~~

<div class="solution" markdown="1">
<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val file = spark.textFile("tachyon://...")
val counts = file.flatMap(line => line.split(" "))
                 .map(word => (word, 1))
                 .reduceByKey(_ + _)
counts.saveAsTextFile("tachyon://...")
~~~
</div>
<div data-lang="java" markdown="1">
~~~
JavaRDD<String> file = spark.textFile("tachyon://...");
JavaRDD<String> words = file.flatMap(new FlatMapFunction<String, String>()
  public Iterable<String> call(String s) { return Arrays.asList(s.split(" ")); }
});
JavaPairRDD<String, Integer> pairs = words.map(new PairFunction<String, String, Integer>()
  public Tuple2<String, Integer> call(String s) { return new Tuple2<String, Integer>(s, 1); }
});
JavaPairRDD<String, Integer> counts = pairs.reduceByKey(new Function2<Integer, Integer>()
  public Integer call(Integer a, Integer b) { return a + b; }
});
counts.saveAsTextFile("tachyon://...");
~~~
</div>
<div data-lang="python" markdown="1">
~~~
file = spark.textFile("tachyon://...")
counts = file.flatMap(lambda line: line.split(" ")) \
             .map(lambda word: (word, 1)) \
             .reduceByKey(lambda a, b: a + b)
counts.saveAsTextFile("tachyon://...")
~~~
</div>
</div>
</div>

From web user interface, or command line, you can see that the `\LICENSE.txt` file is in memory
now. So, each time when a new Spark program comes up, it will load in memory data directly from
Tachyon. In the meantime, we are also working on other features to make Tachyon further enhance
Spark's performance.

This brings us to the end of the Tachyon chapter of the tutorial. We encourage you to continue
playing with the code and to check out the [project website](http://tachyon-project.org/) for
further documentation about the system.

Bug reports and feature requests are welcomed.
