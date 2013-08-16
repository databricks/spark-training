---
layout: global
title: Introduction
next: logging-into-the-cluster.html
---

Welcome to the Dry-Run of the AMP Camp 3 hands-on exercises. Thanks for helping out and we hope you find what you're about to learn helpful!

These exercises will have you working directly with components of Hadoop as well as components of our open-source software stack, called the <a href="https://amplab.cs.berkeley.edu/software/">Berkeley Data Analytics Stack (BDAS)</a>, that have been released and are ready to use.
The components we will cover today include [Spark](http://spark-project.org), [Spark Streaming](http://spark-project.org/docs/latest/streaming-programming-guide.html), [MLbase](http://mlbase.org), and [BlinkDB](http://blinkdb.org).

We have already spun up a small cluster for you on EC2 using our scripts. To access your cluster, you will need to download <a href="https://docs.google.com/file/d/0B6nc314QW_P3Wjh0ZVdxb3Bqam8/edit?usp=sharing">this private key</a>. Then, find the address of the machine in the same row as as your name in the table <a href="http://goo.gl/NRmAlZ">here</a>. You can log into the cluster with the username root using a command like:

    ssh -i /path/to/ampcamp3-all.pem root@<YOUR_MACHINE_ID>


# Providing feedback
This is a dry-run, so you can expect to run into a few issues. When you do, please call over a TA and explain what's going on. When you find a problem with the course that needs attention, please create a new issue at the <a href="https://github.com/amplab/training/issues">training docs Github issue Tracker</a> (there is also a link to this in the footer on all pages of the exercises).

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
  <td class="no">yes</td>
</tr>
</tbody>
</table>
</center>

