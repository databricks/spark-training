---
layout: global
title: Data Exploration Using Shark
categories: [module]
navigation:
  weight: 60
  show: true
skip-chapter-toc: true
---

Now that we've had fun with Spark, let's try out Shark. Remember Shark is a large-scale data warehouse system that runs on top of Spark and provides a SQL like interface (that is fully compatible with Apache Hive).

1. First, launch the Shark console:

   <pre class="prettyprint lang-bsh">
   /root/shark/bin/shark-withinfo
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

   <b>FAQ:</b> If you partially complete the exercises and then restart them later, your tables will stick around.  You will see errors like this if you try to create them again:

   <pre>FAILED: Error in metadata: AlreadyExistsException(message:Table wikistats already exists)
   FAILED: Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask</pre>

   To fix this, drop the table (`wikistats` or, introduced shortly, `wikistats_cached`), then create it again using the same command described above.  You can drop a table with:
   
   <pre>drop table wikistats;</pre>

1. Let's create a table containing all English records and cache it in the cluster's memory. Shark automatically caches any table having a name with the suffix "`_cached`". While running the following command, you can revisit the Spark web UI (hosted at port 4040 on the master node) and click on the Storage tab to see a new cached dataset being created.

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
   select sum(page_views) from wikistats_cached where page_name like "%berkeley%";
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
