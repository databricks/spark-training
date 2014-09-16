---
layout: global
title: Data Exploration Using Spark SQL
categories: [module]
navigation:
  weight: 60
  show: true
skip-chapter-toc: true
---

Spark SQL is the newest component of Spark and provides a SQL like interface.
Spark SQL is tightly integrated with the the various spark programming languages 
so we will start by launching the Spark shell from the root directory of the provided USB drive:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
usb/$ spark/bin/spark-shell</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
usb/$ spark/bin/pyspark</pre>
</div>
</div>


Once you have launched the Spark shell, the next step is to create a SQLContext.  A SQLConext wraps the SparkContext, which you used in the previous lesson, and adds functions for working with structured data.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
scala> val sqlContext = new org.apache.spark.sql.SQLContext(sc)
sqlContext: org.apache.spark.sql.SQLContext = org.apache.spark.sql.SQLContext@52955821
</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
from pyspark.sql import SQLContext
sqlCtx = SQLContext(sc)</pre>
</div>
</div>

Now we can load a set of data in that is stored in the Parquet format.  Parquet is a self-describing columnar format.  Since it is self-describing, Spark SQL will automatically be able to infer all of the column names and their datatypes.  For this exercise we have provided a set of data that contains all of the pages on wikipedia that contain the word "berkeley".  You can load this data using the parquetFile method provided by the SQLContext.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
scala> val wikiData = sqlContext.parquetFile("data/wiki_parquet")
wikiData: org.apache.spark.sql.SchemaRDD = 
SchemaRDD[0] at RDD at SchemaRDD.scala:98
== Query Plan ==
ParquetTableScan [id#0,title#1,modified#2L,text#3,username#4], (ParquetRelation data/wiki_parquet), []
</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
>>> wikiData = sqlCtx.parquetFile("data/wiki_parquet")
</pre>
</div>
</div>

The result of loading in a parquet file is a SchemaRDD.  A SchemaRDD has all of the functions of a normal RDD.  For example, lets figure out how many records are in the data set.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
scala> wikiData.count()
res9: Long = 39365
</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
>>> wikiData.count()
39365L
</pre>
</div>
</div>

In addition to standard RDD operatrions, SchemaRDDs also have extra information about the names and types of the columns in the dataset.  This extra schema information makes it possible to run SQL queries against the data after you have registered it as a table.  Below is an example of counting the number of records using a SQL query.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
scala> wikiData.registerTempTable("wikiData")
scala> val countResult = sqlContext.sql("SELECT COUNT(*) FROM wikiData").collect()
countResult: Array[org.apache.spark.sql.Row] = Array([39365])
</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
>>> wikiData.registerTempTable("wikiData")
>>> result = sqlCtx.sql("SELECT COUNT(*) AS pageCount FROM wikiData").collect()
</pre>
</div>
</div>

The result of SQL queries is always a collection of Row objects.  From a row object you can access the individual columns of the result.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
scala> val sqlCount = countResult.head.getLong(0)
sqlCount: Long = 39365
</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
>>> result[0].pageCount
39365</pre>
</div>
</div>

SQL can be a powerfull tool from performing complex aggregations.  For example, the following query returns the top 10 usersnames by the number of pages they created.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
scala> sqlContext.sql("SELECT username, COUNT(*) AS cnt FROM wikiData WHERE username <> '' GROUP BY username ORDER BY cnt DESC LIMIT 10").collect().foreach(println)
[Waacstats,2003]
[Cydebot,949]
[BattyBot,939]
[Yobot,890]
[Addbot,853]
[Monkbot,668]
[ChrisGualtieri,438]
[RjwilmsiBot,387]
[OccultZone,377]
[ClueBot NG,353]
</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
>>> sqlCtx.sql("SELECT username, COUNT(*) AS cnt FROM wikiData WHERE username <> '' GROUP BY username ORDER BY cnt DESC LIMIT 10").collect()
[{u'username': u'Waacstats', u'cnt': 2003}, {u'username': u'Cydebot', u'cnt': 949}, {u'username': u'BattyBot', u'cnt': 939}, {u'username': u'Yobot', u'cnt': 890}, {u'username': u'Addbot', u'cnt': 853}, {u'username': u'Monkbot', u'cnt': 668}, {u'username': u'ChrisGualtieri', u'cnt': 438}, {u'username': u'RjwilmsiBot', u'cnt': 387}, {u'username': u'OccultZone', u'cnt': 377}, {u'username': u'ClueBot NG', u'cnt': 353}]</pre>
</div>
</div>

__NOTE: java.lang.OutOfMemoryError__ : If you see a `java.lang.OutOfMemoryError`, you will need to restart the Spark shell with the following command line option:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
usb/$ spark/bin/spark-shell --driver-memory 1G</pre>
</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
usb/$ spark/bin/pyspark --driver-memory 1G</pre>
</div>
</div>

This increases the amount of memory allocated for the Spark driver. Since we are running Spark in local mode, all operations are performed by the driver, so the driver memory is all the memory Spark has to work with.

- How many articles contain the word "california"?

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">SELECT COUNT(*) FROM wikiData WHERE text LIKE '%california%'</pre>
   </div>


