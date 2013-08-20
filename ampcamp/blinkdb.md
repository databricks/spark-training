---
layout: global
title: Data Exploration Using BlinkDB
prev: processing-live-data-streams-with-spark-streaming.html
next: mesos.html
skip-chapter-toc: true
---

BlinkDB is a large-scale data warehouse system like Shark that adds the ability to create and use smaller samples of large datasets to make queries even faster.  Today you're going to get a sneak peek at an Alpha release of BlinkDB.  We'll set up BlinkDB and use it to run some SQL queries against the English Wikipedia.  If you've already done the Shark exercises, you might notice that we're going to go through the same exercises.  Don't worry if you haven't used Shark, though - we haven't assumed that you have.

<b>NOTE: The version of BlinkDB used in this dry run has a bug that breaks SQL queries that use `group by`.  None of the exercises below involve `group by` queries, but you might run into this bug if you're playing around.</b>

1. First, launch the BlinkDB console:

    <pre class="prettyprint lang-bsh">
    /root/blinkdb/bin/blinkdb</pre>

2. Similar to Apache Hive, BlinkDB can query external tables (i.e. tables that are not created in BlinkDB).
   Before you do any querying, you will need to tell BlinkDB where the data is and define its schema.

   <pre class="prettyprint lang-sql">
   blinkdb> create external table wikistats (dt string, project_code string, page_name string, page_views int, bytes int) row format delimited fields terminated by ' ' location '/wiki/pagecounts';
   <span class="nocode">
   ...
   OK
   Time taken: 0.232 seconds</span></pre>

   <b>FAQ:</b> If you see the following errors, don’t worry. Things are still working under the hood.

   <pre class="nocode">
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.core.resources" but it cannot be resolved.
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.core.runtime" but it cannot be resolved.
   12/08/18 21:07:34 ERROR DataNucleus.Plugin: Bundle "org.eclipse.jdt.core" requires "org.eclipse.text" but it cannot be resolved.</pre>

   <b>FAQ:</b> If you see errors like these, you might have copied and pasted a line break, and should be able to remove it to get rid of the errors.

   <pre>13/02/05 21:22:16 INFO parse.ParseDriver: Parsing command: CR
   FAILED: Parse Error: line 1:0 cannot recognize input near 'CR' '&lt;EOF&gt;' '&lt;EOF&gt;'</pre>
   
3. Next we will compute a simple count of the number records in the original table.  For now, we're not using any sample at all.  (If you have some familiarity with databases, note that we use the "`count(1)`" syntax here since in earlier versions of Hive, the more popular "`count(*)`" operation was not supported. BlinkDB supports most of Hive SQL; its syntax is described in detail in the <a href="https://cwiki.apache.org/confluence/display/Hive/GettingStarted" target="_blank">Hive Getting Started Guide</a>.)

   <pre class="prettyprint lang-sql">
   blinkdb> select count(1) from wikistats;
   <span class="nocode">
   ...
   OK
   329641466
   Time taken: 27.889 seconds</span></pre>

4. Now let's create a 1% random sample of this table using the samplewith operator and cache it in the cluster's memory.

   <pre class="prettyprint lang-sql">
   blinkdb> create table wikistats_sample_cached as select * from wikistats samplewith 0.01;
   <span class="nocode">
   ...
Moving data to: hdfs://ec2-107-22-9-64.compute-1.amazonaws.com:9000/user/hive/warehouse/wikistats_sample_cached
OK
Time taken: 19.454 seconds</span></pre>

5. Next compute a simple count of the number of English records (<i>i.e., those with </i> "`project_code="en"`") of the original table.  For now, we're not using the sample at all.

   <pre class="prettyprint lang-sql">
   blinkdb> select count(1) from wikistats where project_code = "en";
   <span class="nocode">
   ...
   122352588
   Time taken: 21.632 seconds</span></pre>

6. Now approximate the same count using the sampled table.  In the Alpha release, you need to tell BlinkDB to compute an approximation by prepending "`approx_`" to your aggregation function.  (Also, only queries that compute `count`, `sum`, `average`, or `stddev` can be approximated.  In the future many more functions, including UDFs, will be approximable.)

   <pre class="prettyprint lang-sql">
   blinkdb> select approx_count(1) from wikistats_sample_cached;
   <span class="nocode">
   ...
   OK
   {"approx_count":122361000,"error":240887,"confidence":99}
   Time taken: 2.993 seconds</span></pre>

   Notice that our sampled query produces a slightly incorrect answer, but it runs faster.  Also, the query result now includes two additional columns, which tell us how close BlinkDB thinks it is to the true answer. The second and third columns indicate the statistical error and the confidence respectively. A way to interpret this result is that for this query, the true answer should lie between 122361000 +/- 240887, 99% of the time. More specifically, the interval \[first value - second value, first value + second value\] is a .99 confidence interval for the true count.

   <b>NOTE: There is a bug in the dry-run BlinkDB that causes the error interval to be misreported for approx_count and approx_sum.  For now, take the number you see and multiply by 100.  Sorry.</b>

7. Compute the total traffic to Wikipedia pages on May 7 between 7AM - 8AM.

   <pre class="prettyprint lang-sql">
   blinkdb> select approx_sum(page_views) from wikistats_sample_cached where dt="20090507-070000";
   <span class="nocode">
   ...
   OK
   {"approx_sum":12115600,"error":831617,"confidence":99}
   Time taken: 1.554 seconds</span></pre>

   
   As before, you can also compute an exact answer by running the same query on the table `wikistats_cached`, replacing "`approx_sum`" with "`sum`".

    <pre class="prettyprint lang-sql">
    blinkdb> select sum(page_views) from wikistats where dt="20090507-070000";
    <span class="nocode">
    ...
    OK
	13098805
	Time taken: 19.465 seconds</span></pre>

8. Next, we would like to find out the average number of hits on pages with Berkeley in the title throughout the entire period:

   <pre class="prettyprint lang-sql">
   blinkdb> select approx_avg(page_views) from wikistats_sample_cached where page_name like "%berkeley%";
   <span class="nocode">
   ...
   OK
   {"approx_avg":2.75,"error":2.7595032883541744,"confidence":99.0}
   Time taken: 2.459 seconds</span></pre>

9. With all the warm up, now it is your turn to write queries. Write Hive QL queries to answer the following questions:

   - Count the number of distinct project_codes.  Try the same query on the original table and compare the results.  Notice that the kind of sampling available in the Alpha is not very effective for queries that depend on rare values, like `count distinct`.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select count(distinct project_code) from wikistats_sample_cached;</pre>
   </div>

   - How many hits are there on pages with berkeley in the title throughout the entire period?

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select approx_count(page_views) from wikistats_sample_cached where page_name like "%berkeley%";
   /* "%" in SQL is a wildcard matching all characters. */</pre>
   </div>

   - Which day (5th, 6th or 7th May 2009) was the most popular in terms of the number of hits.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select approx_sum(page_views) from wikistats_sample_cached where dt like "20090505%";</pre>
   </div>

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select approx_sum(page_views) from wikistats_sample_cached where dt like "20090506%";</pre>
   </div>

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-sql">
   select approx_sum(page_views) from wikistats_sample_cached where dt like "20090507%";</pre>
   </div>

10. To exit BlinkDB, type the following at the BlinkDB command line (and don't forget the semicolon!).

    <pre class="prettyprint lang-bsh">
    blinkdb> exit;</pre>


<!--/*
4. Now let's create a table containing all English records and cache it in the cluster's memory.

   <pre class="prettyprint lang-sql">
   blinkdb> create table wikistats_cached as select * from wikistats where project_code="en";
   <span class="nocode">
   ...
   Moving data to: hdfs://ec2-107-22-9-64.compute-1.amazonaws.com:9000/user/hive/warehouse/wikistats_cached
OK
   Time taken: 45.547 seconds</span></pre>
*/-->
