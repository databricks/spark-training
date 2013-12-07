---
layout: global
title: Introduction
navigation:
  weight: 10
  show: true
---

# Welcome
Welcome to the Spark Summit hands-on exercises. These exercises are adapted from similar exercises that were prepared for and run at <a href="http://ampcamp.berkeley.edu">AMP Camp Big Data Bootcamps</a>. They were written by volunteer graduate students and postdocs in the <a href="https://amplab.cs.berkeley.edu/">UC Berkelay AMPLab</a>. Many of those same graduate students are also volunteers here on the Spark Summit Training day team as well. The exercises we cover today will have you working directly with the Spark specific components of the AMPLab's open-source software stack,
called the <a href="https://amplab.cs.berkeley.edu/software/">Berkeley Data Analytics Stack
(BDAS)</a>.


You can navigate around the exercises by looking in the page header or footer and clicking on the arrows or the dropdown button that shows the current page title (as shown in the figure below).

<p style="margin-bottom:15px"><img src="img/header-nav-dropdown-button-summit.png" class="shadow" style="height:auto; width:498px"/></p>

The components we will cover at the first Spark Training are listed below.

## Introductory Exercises

1. [Scala](introduction-to-the-scala-shell.html) - a quick crashcourse on the Scala language and command line interface.
2. [Spark](data-exploration-using-spark.html) [(project homepage)](http://spark.incubator.apache.org) - a fast cluster compute engine.
3. [Shark](data-exploration-using-shark.html) [(project homepage)](http://shark.cs.berkeley.edu) - a SQL layer on top of Spark.

## Advanced Exercises
<ol start="4">
  <li><a href="realtime-processing-with-spark-streaming.html">Spark Streaming</a> <a href="http://spark-project.org/docs/latest/streaming-programming-guide.html">(project overview page)</a> - A stream processing layer on top of Spark.</li>
  <li><a href="machine-learning-with-spark.html">Machine Learning with Spark</a> - Implement K-Means clustering with Spark.</li>
</ol>


# Course Prerequisites
Several components support multiple languages. In some sections of this training material, you can choose which language you want to use as you follow along and gain experience with the tools. The following table shows which languages this mini course supports for each section. You are welcome to mix and match languages depending on your preferences and interests.

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
  <td>Machine Learning - K-Means Clustering</td>
  <td class="yes">yes</td>
  <td class="yes">yes</td>
  <td class="yes">yes</td>
</tr>
</tbody>
</table>
</center>

# Providing feedback
We are using the cutting edge versions (i.e., the master branches) of most of our software components, which means you may run into a few issues. If you do, please call over a TA and explain what's going on. To report a problem, please create a new issue at the <a href="https://github.com/amplab/training/issues">AMPLab's training docs Github issue Tracker</a> (there is also a link to this in the footer on all pages of the exercises).

# Getting Started

If you are attending Spark Training in person, we have emailed you a cluster hostname and private keys, which you can use to [log into your cluster](logging-into-the-cluster.html).

<p class="alert alert-warn">
<i class="icon-info-sign">    </i>
We sent the email to the address you used to registered with. If you don't see the email, first check your spam filter, then ask a TA, and finally you can try emailing {{ site.organizers-email }}, though during the training event we may not be able to keep up on email, so talking to us in person is definitely best.
</p>

If you are participating in the exercises from a remote location, you will want to [launch a BDAS cluster on Amazon EC2](launching-a-bdas-cluster-on-ec2.html) for yourself.
