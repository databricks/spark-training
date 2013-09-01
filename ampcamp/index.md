---
layout: global
title: Introduction
navigation:
  weight: 10
  show: true
---

# Welcome
Welcome to the AMP Camp 3 hands-on exercises. These exercises will have you working directly with components of our open-source software stack,
called the <a href="https://amplab.cs.berkeley.edu/software/">Berkeley Data Analytics Stack
(BDAS)</a>.


The components we will cover over the two days of AMP Camp 3 are:

## Day 1

* [Scala](introduction-to-the-scala-shell.html) - a quick crashcourse on the Scala language and command line interface.
* [Spark](data-exploration-using-spark.html) [(project homepage)](http://spark.incubator.apache.org) - a fast cluster compute engine.
* [Shark](data-exploration-using-shark.html) [(project homepage)](http://shark.cs.berkeley.edu) - a SQL layer on top of Spark.
* [Spark Streaming](realtime-processing-with-spark-streaming.html) [(project overview page)](http://spark-project.org/docs/latest/streaming-programming-guide.html) - A stream processing layer on top of Spark.

## Day 2
* [BlinkDB](blinkdb.html) [(project page)](http://blinkdb.org) - A SQL processing system providing approximate results and bounded time and errors.
* [MLbase](mli-document-categorization.html) [(project page)](http://mlbase.org) - A machine learning system including a library of ML algorithms and tools to make ML easy to use.
* [Mesos](mesos.html) [(project page)](http://mesos.apache.org) - A fault tolerant cluster-level operating system for managing cluster resources and frameworks.


# Course Prerequisites
Several components support multiple languages. For the sections about Spark and Spark Streaming, you can choose which language you want to use as you follow along and gain experience with the tools. The following table shows which languages this mini course supports for each section. You are welcome to mix and match languages depending on your preferences and interests.

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
  <td>Shark Interactive</td>
  <td colspan="3" class="yes">All SQL</td>
</tr><tr>
  <td>Spark Streaming</td>
  <td class="yes">yes</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
</tr><tr>
  <td>BlinkDB</td>
  <td colspan="3" class="yes">All SQL</td>
</tr><tr>
  <td>MLbase</td>
  <td class="yes">yes</td>
  <td class="no">no</td>
  <td class="no">no</td>
</tr><tr>
  <td>Mesos</td>
  <td colspan="3" class="yes">Command Line and WebUI</td>
</tr>
</tbody>
</table>
</center>

# Providing feedback
We are using the cutting edge versions (i.e., the master branches) of most of our software components, which means you may run into a few issues. If you do, please call over a TA and explain what's going on. To report a problem, please create a new issue at the <a href="https://github.com/amplab/training/issues">training docs Github issue Tracker</a> (there is also a link to this in the footer on all pages of the exercises).

# Getting Started

Ok, If you are attending AMP Camp in-person, we have probably given you a hostname and instructions for downloading the private key you will need in the next section to access your cluster.
If you are participating in the exercises from a remote location, you will want to [launch a BDAS cluster on Amazon EC2](launching-a-bdas-cluster-on-ec2.html) for yourself.
