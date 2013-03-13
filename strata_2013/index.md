---
layout: global
title: Introduction
next: launching-a-spark-shark-cluster-on-ec2.html
---

Welcome to the AMP Camp hands-on big data mini course. These training materials have been produced by members of the open source community, especially PhD students in the UC Berkeley AMPLab.

This mini course consists of a series of exercises that will have you working directly with components of Hadoop as well as components of our open-source software stack, called the <a href="https://amplab.cs.berkeley.edu/bdas/">Berkeley Data Analytics Stack (BDAS)</a>, that have been released and are ready to use.
These components include [Spark](http://spark-project.org), Spark Streaming, and [Shark](http://shark.cs.berkeley.edu).
We will begin by walking you through the easy process of spinning up a small cluster on EC2 using our scripts. Then we will dive into big data analytics training exercises, starting with simple interactive analysis techniques at the Spark and Shark shells, then progress to writing standalone programs with Spark Streaming, and finish by implementing some more advanced machine learning algorithms to incorporate into your analysis.

# Course Prerequisites
This mini course is meant to be hands-on introduction to Spark, Spark Streaming, and Shark. While Shark supports a simplified version of SQL, Spark and Spark Streaming both support multiple languages. For the sections about Spark and Spark Streaming, the mini course allows you to choose which language you want to use as you follow along and gain experience with the tools. The following table shows which languages this mini course supports for each section. You are welcome to mix and match languages depending on your preferences and interests.

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

