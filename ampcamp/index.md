---
layout: global
title: Introduction
navigation:
  weight: 10
  show: true
---

# Welcome
Welcome to the Spark Summit 2014 Training hands-on exercises! These exercises
are extended and enhanced from those given at previous <a
href="http://ampcamp.berkeley.edu">AMP Camp Big Data Bootcamps</a>. The
exercises we cover today will have you working directly with the Spark specific
components of the AMPLab's open-source software stack, called the <a
href="https://amplab.cs.berkeley.edu/software/">Berkeley Data Analytics Stack
(BDAS)</a>.

<!--
You can navigate around the exercises by looking in the page header or footer
and clicking on the arrows or the dropdown button that shows the current page
title.

<p style="margin-bottom:15px"><img src="img/header-nav-dropdown-button-summit.png" class="shadow" style="height:auto; width:498px"/></p>-->

<!-- ## Introductory Exercises
The tutorial begins with a set of introductory excercises which should be done _**sequentially**_.

1. [Scala](introduction-to-the-scala-shell.html) - a quick crashcourse on the Scala language and command line interface.
2. [Spark](data-exploration-using-spark.html) [(programming guide)](http://spark.apache.org/docs/latest/programming-guide.html) - Use the Spark shell to interactively explore Wikipedia data. 

## Advanced Exercises
-->
The components we will cover at the advanced Spark training are listed below.
These can be done _**in any order**_ according to your interests.

<ol start="1">
  <li><a href="data-exploration-using-spark-sql.html">Spark SQL</a> <a href="http://spark.apache.org/docs/latest/sql-programming-guide.html">(programming guide)</a> - Use the Spark shell to write interactive SQL queries.</li>
  <li><a href="realtime-processing-with-spark-streaming.html">Spark Streaming</a> <a href="http://spark.apache.org/docs/latest/streaming-programming-guide.html">(programming guide)</a> - Process a sample of Twitter tweet streams.</li>
  <li><a href="movie-recommendation-with-mllib.html">Machine Learning with MLlib</a> <a href="http://spark.apache.org/docs/latest/mllib-guide.html">(programming guide)</a> - Build a movie recommender with Spark.</li>
  <li><a href="graph-analytics-with-graphx.html">Graph Analytics with GraphX</a> <a href="http://spark.apache.org/docs/latest/graphx-programming-guide.html">(programming guide)</a> - Explore graph-structured data and graph algorithms. (note: this exercise is significantly longer than the rest.)</li>
</ol>


# Course Prerequisites
In several sections of this training material, you can choose which language you want to use as you follow along and gain experience with the tools. The following table shows which languages this mini course supports for each section. You are welcome to mix and match languages depending on your preferences and interests.

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
<!--  <td>Spark Interactive</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
  <td class="yes">yes</td>
-->
</tr><tr>
  <td>Spark SQL Interactive</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
  <td class="yes">yes</td>
</tr><tr>
  <td>Spark Streaming</td>
  <td class="yes">yes</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
</tr><tr>
  <td>MLlib - Machine Learning</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
  <td class="yes">yes</td>
</tr><tr>
  <td>GraphX - Graph Analytics</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
  <td class="no">no</td>
</tr>
</tbody>
</table>
</center>

# Providing feedback
<!--
We are using the cutting edge versions (i.e., the master branches) of most of our software components, which means you may run into a few issues. If you do, please call over a TA and explain what's going on. To report a problem, please create a new issue at the <a href="https://github.com/amplab/training/issues">AMPLab's training docs Github issue Tracker</a> (there is also a link to this in the footer on all pages of the exercises).
-->
TODO: UPDATE INSTRUCTIONS ON FEEDBACK 

# Getting Started

If you are attending Spark Training in person, you should have a USB key containing training material. After loading the USB key, you should perform the following steps:

1. Rename the USB key from <code>NO NAME</code> to <code>SparkSummit</code> 

<p class="alert alert-warn">
<i class="icon-info-sign">    </i>
For the rest of this training session, all command-line instructions assume that the terminal is in the <code>SparkSummit</code> directory.
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


<!--If you are participating in the exercises from a remote location, you will want
to download the [training material](FIX BROKEN LINK.html).-->
