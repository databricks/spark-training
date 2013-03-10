---
layout: global
title: Introduction
next: launching-a-spark-shark-cluster-on-ec2.html
---

Welcome to the hands-on tutorial AMP Camp Bootcamp on Big Data.
In addition to the amazing O'Reilly Strata folks, this event has been organized by Professors and PhD students in the UC Berkeley AMPLab.

This tutorial consists of a series of exercises that will have you working directly with components of our open-source software stack, called the Berkeley Data Analytics Stack (BDAS), that have been released and are ready to use.
These components include [Spark](http://spark-project.org), Spark Streaming, and [Shark](http://shark.cs.berkeley.edu).
We have already spun up a 4-node EC2 Cluster for you with the software preinstalled, which you will be using to load and analyze a real Wikipedia dataset.
We will begin with simple interactive analysis techniques at the Spark and Shark shells, then progress to writing standalone programs with Spark Streaming, and finish by implementing some more advanced machine learning algorithms to incorporate into your analysis.

# Tutorial Developer Prerequisites
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

