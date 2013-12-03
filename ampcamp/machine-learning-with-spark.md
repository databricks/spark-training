---
layout: global
title: Machine Learning With Spark
categories: [module]
navigation:
  weight: 75
  show: true
---

In this chapter, we will use Spark to implement machine learning algorithms. To complete the machine learning exercises within the time available using our relatively small EC2 clusters, in this section we will work with a restricted set of the Wikipedia traffic statistics data from May 5-7, 2009. In particular, this dataset only includes a subset of all Wikipedia articles.

## Background: Featurization

To apply most machine learning algorithms, we must first preprocess and featurize the data.  That is, for each data point, we must generate a vector of numbers describing the salient properties of that data point.  In our case, each data point will consist of a unique Wikipedia article identifier (i.e., a unique combination of Wikipedia project code and page title) and associated traffic statistics.  We will generate 24-dimensional feature vectors, with each feature vector entry summarizing the page view counts for the corresponding hour of the day.

Recall that each record in our dataset consists of a string with the format "`<date_time> <project_code> <page_title> <num_hits> <page_size>`".  The format of the date-time field is YYYYMMDD-HHmmSS (where 'M' denotes month, and 'm' denotes minute).

Given our time constraints, in order to focus on the machine learning algorithms themselves, we have pre-processed the data to create the featurized dataset that we will use to implement K-means clustering.
{% comment %}
If you are interested in doing featurization on your own, you can follow [these instructions](featurization.html).
{% endcomment %}

## Clustering

[K-Means clustering](http://en.wikipedia.org/wiki/K-means_clustering) is a popular clustering algorithm that can be used to partition your dataset into K clusters. We now look at how we can implement K-Means clustering using Spark to cluster the featurized Wikipedia dataset.

## Setup
Similar to the Spark streaming exercises above, we will be using a standalone project template for this exercise. In your AMI, this has been setup in `/root/kmeans/[scala|java|python]/`. You should find the following items in the directory.

<div class="codetabs">
<div data-lang="scala">

<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>sbt</code>: Directory containing the SBT tool</li>
<li><code>build.sbt</code>: SBT project file</li>
<li><code>WikipediaKMeans.scala</code>: Main Scala program that you are going to edit, compile and run</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.scala</code>. It should look as follows:
</div>
<div data-lang="java">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>sbt</code>: Directory containing the SBT tool</li>
<li><code>build.sbt</code>: SBT project file</li>
<li><code>WikipediaKMeans.java</code>: Main Java program that you are going to edit, compile and run</li>
<li><code>JavaHelpers.java</code>: Some helper functions used by the template.</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.java</code>. It should look as follows:
</div>
<div data-lang="python">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>WikipediaKMeans.py</code>: Main Python program that you are going to edit and run</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.py</code>. It should look as follows:
</div>
</div>


<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.util.Vector

import org.apache.log4j.Logger
import org.apache.log4j.Level

import scala.util.Random
import scala.io.Source

object WikipediaKMeans {
  def parseVector(line: String): Vector = {
      return new Vector(line.split(',').map(_.toDouble))
  }

  // Add any new functions you need here

  def main(args: Array[String]) {
    Logger.getLogger("spark").setLevel(Level.WARN)
    val sparkHome = "/root/spark"
    val jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar"
    val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
    val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim

    val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))

    val K = 10
    val convergeDist = 1e-6

    val data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
            t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()

    // Your code goes here

    sc.stop();
    System.exit(0)
  }
}
~~~
</div>
<div data-lang="java" markdown="1">
~~~
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.io.*;
import java.util.*;
import com.google.common.collect.Lists;

import scala.Tuple2;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.util.Vector;


public class WikipediaKMeans {
  // Add any new functions you need here

  public static void main(String[] args) throws Exception {
    Logger.getLogger("spark").setLevel(Level.WARN);
    String sparkHome = "/root/spark";
    String jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar";
    String master = JavaHelpers.getSparkUrl();
    String masterHostname = JavaHelpers.getMasterHostname();
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans",
      sparkHome, jarFile);

    int K = 10;
    double convergeDist = .000001;

    JavaPairRDD<String, Vector> data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
      new PairFunction<String, String, Vector>() {
        public Tuple2<String, Vector> call(String in)
        throws Exception {
          String[] parts = in.split("#");
          return new Tuple2<String, Vector>(
            parts[0], JavaHelpers.parseVector(parts[1]));
        }
      }
     ).cache();

    // Your code goes here

    sc.stop();
    System.exit(0);
  }
}
~~~
</div>
<div data-lang="python" markdown="1">
~~~
import os
import sys
import numpy as np

from pyspark import SparkContext

def setClassPath():
    oldClassPath = os.environ.get('SPARK_CLASSPATH', '')
    cwd = os.path.dirname(os.path.realpath(__file__))
    os.environ['SPARK_CLASSPATH'] = cwd + ":" + oldClassPath


def parseVector(line):
    return np.array([float(x) for x in line.split(',')])

# Add any new functions you need here

if __name__ == "__main__":
    setClassPath()
    master = open("/root/spark-ec2/cluster-url").read().strip()
    masterHostname = open("/root/spark-ec2/masters").read().strip()
    sc = SparkContext(master, "PythonKMeans")
    K = 10
    convergeDist = 1e-5

    lines = sc.textFile(
	"hdfs://" + masterHostname + ":9000/wikistats_featurized")
    data = lines.map(
	lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()

    # Your code goes here
~~~
</div>
</div>

Let's first take a closer look at our template code in a text editor on the cluster itself, then we'll start adding code to the template. Locate the `WikipediaKMeans` class and open it with a text editor.

<div class="codetabs">
<div data-lang="scala">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/scala
vim WikipediaKMeans.scala  # If you don't know vim, you can use emacs or nano
</pre>
</div>
<div data-lang="java">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/java
vim WikipediaKMeans.java  # If you don't know vim, you can use emacs or nano
</pre>
</div>
<div data-lang="python">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/python
vim WikipediaKMeans.py  # If you don't know Vim, you can use emacs or nano
</pre>
</div>
</div>

The cluster machines have vim, emacs, and nano installed for editing. Alternatively, you can use your favorite text editor locally and then copy-paste content into vim, emacs, or nano before running it.

For any Spark computation, we first create a Spark context object. For Scala or Java programs, we do that by providing the Spark cluster URL, the Spark home directory, and the JAR file that will be generated when we compile our program. For Python programs, we only need to provide the Spark cluster URL. Finally, we also name our program "WikipediaKMeans" to identify it in Spark's web UI.

This is what it looks like in our template code:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans", sparkHome, jarFile);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    sc = SparkContext(master, "PythonKMeans")
~~~
</div>
</div>

Next, the code uses the SparkContext to read in our featurized dataset. The [featurization process](#command-line-preprocessing-and-featurization) created a 24-dimensional feature vector for each article in our Wikipedia dataset, with each vector entry summarizing the page view counts for the corresponding hour of the day. Each line in the file consists of the page identifier and the features separated by commas.

Next, the code reads the file in from HDFS and parses each line to create a RDD which contains pairs of `(String, Vector)`.

A quick note about the `Vector` class we will using in this exercise: For Scala and Java programs we will be using the [Vector](http://spark-project.org/docs/latest/api/core/index.html#spark.util.Vector) class provided by Spark. For Python, we will be using [NumPy arrays](http://docs.scipy.org/doc/numpy/reference/generated/numpy.array.html).

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
   val data = sc.textFile(
       "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
           t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()
   val count = data.count()
   println("Number of records " + count)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaPairRDD<String, Vector> data = sc.textFile(
      "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
        new PairFunction<String, String, Vector>() {
          public Tuple2<String, Vector> call(String in)
          throws Exception {
            String[] parts = in.split("#");
            return new Tuple2<String, Vector>(parts[0], parseVector(parts[1]));
          }
        }
      ).cache();
    long count = data.count();
    System.out.println("Number of records " + count);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    lines = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized")
    data = lines.map(
        lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()
    count = data.count()
    print "Number of records " + str(count)
~~~
</div>
</div>

Now, let's make our first edit to the file and add code to count the number of records in our dataset by running `data.count()` and print the value. Find the comment that says "Your code goes here" and replace it with:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val count = data.count()
    println("Number of records " + count)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    long count = data.count();
    System.out.println("Number of records " + count);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    count = data.count()
    print "Number of records " + str(count)
~~~
</div>
</div>

## Running the program
Before we implement the K-Means algorithm, here is quick reminder on how you can run the program at any point during this exercise. Save the `WikipediaKMeans` file run the following commands:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/scala
sbt/sbt package run
</pre>

This command will compile the `WikipediaKMeans` class and create a JAR file in `/root/kmeans/scala/target/scala-2.9.3/`. Finally, it will run the program. You should see output similar to the following on your screen:

</div>
<div data-lang="java" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/java
sbt/sbt package run
</pre>

This command will compile the `WikipediaKMeans` class and create a JAR file in `/root/kmeans/java/target/scala-2.9.3/`. Finally, it will run the program. You should see output similar to the following on your screen:

</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/python
/root/spark/pyspark ./WikipediaKMeans.py
</pre>

This command will run `WikipediaKMeans` on your Spark cluster. You should see output similar to the following on your screen:

</div>
</div>

<pre class="prettyprint lang-bsh">
Number of records 802450
</pre>

## K-Means algorithm
We are now set to start implementing the K-means algorithm, so remove or comment out the lines we just added to print the record count and let's get started.

1. The first step in the K-Means algorithm is to initialize our centers by randomly picking `K` points from our dataset. We use the `takeSample` function in Spark to do this.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var centroids = data.takeSample(false, K, 42).map(x => x._2)
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      List<Tuple2<String, Vector>> centroidTuples = data.takeSample(false, K, 42);
      final List<Vector> centroids = Lists.newArrayList();
      for (Tuple2<String, Vector> t: centroidTuples) {
        centroids.add(t._2());
      }
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      # NOTE: PySpark does not support takeSample() yet. Use first K points instead.
      centroids = map(lambda (x, y): y, data.take(K))
~~~
    </div>
    </div>

1. Next, we need to compute the closest centroid for each point and we do this by using the `map` operation in Spark. For every point we will use a function `closestPoint` (which you have to write!) to compute the closest centroid.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var closest = data.map(p => (closestPoint(p._2, centroids), p._2)) // Won't work until you write closestPoint()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      JavaPairRDD<Integer, Vector> closest = data.map(
        new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
          public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
            return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
          }
        }
      );
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      closest = data.map(
          lambda (x, y) : (closestPoint(y, centroids), y))
~~~
    </div>
    </div>

    **Exercise:** Write the `closestPoint` function in `WikipediaKMeans` to return the index of the closest centroid given a point and the set of all centroids. To get you started, we provide the type signature of the function:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      static int closestPoint(Vector p, List<Vector> centers) {
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      def closestPoint(p, centers):
~~~
    </div>
    </div>

    In case you get stuck, you can use our solution given here:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
        var bestIndex = 0
        var closest = Double.PositiveInfinity
        for (i <- 0 until centers.length) {
          val tempDist = p.squaredDist(centers(i))
          if (tempDist < closest) {
            closest = tempDist
            bestIndex = i
          }
        }
        return bestIndex
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      static int closestPoint(Vector p, List<Vector> centers) {
        int bestIndex = 0;
        double closest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < centers.size(); i++) {
          double tempDist = p.squaredDist(centers.get(i));
          if (tempDist < closest) {
            closest = tempDist;
            bestIndex = i;
          }
        }
        return bestIndex;
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      def closestPoint(p, centers):
          bestIndex = 0
          closest = float("+inf")
          for i in range(len(centers)):
              dist = np.sum((p - centers[i]) ** 2)
              if dist < closest:
                  closest = dist
                  bestIndex = i
          return bestIndex
~~~
    </div>
    </div>
    </div>

    On the line defining the variable called `closest`, the `map` operation creates a new RDD which contains a tuple for every point. The first element in the tuple is the index of the closest centroid for the point and second element is the `Vector` representing the point.

1. Now that we have the closest centroid for each point, we can cluster our points by the centroid they belong to. To do this, we use a `groupByKey` operation as shown below. The `groupByKey` operator creates a new `RDD[(Int, Array[Vector])]` where the key is the index of the centroid and the values are all the points which belong to its cluster.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var pointsGroup = closest.groupByKey()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      pointsGroup = closest.groupByKey()
~~~
    </div>
    </div>

1. We can now calculate our new centroids by computing the mean of the points that belong to a cluster. We do this using `mapValues` which allows us to apply a function on all the values for a particular key. We also use a function `average` to compute the average of an array of vectors.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
        new Function<List<Vector>, Vector>() {
          public Vector call(List<Vector> ps) throws Exception {
            return average(ps);
          }
        }).collectAsMap();
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      newCentroids = pointsGroup.mapValues(
          lambda x : average(x)).collectAsMap()
~~~
    </div>
    </div>

    **Exercise:** Write the `average` function in `WikipediaKMeans` to sum all the vectors and divide it by the number of vectors present in the input array. Your function should return a new Vector which is the average of the input vectors. You can look at our solution in case you get stuck.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      def average(ps: Seq[Vector]) : Vector = {
        val numVectors = ps.size
        var out = new Vector(ps(0).elements)
        for (i <- 1 until numVectors) {
          out += ps(i)
        }
        out / numVectors
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      static Vector average(List<Vector> ps) {
        int numVectors = ps.size();
        Vector out = new Vector(ps.get(0).elements());
        for (int i = 0; i < numVectors; i++) {
          out.addInPlace(ps.get(i));
        }
        return out.divide(numVectors);
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      def average(points):
          numVectors = len(points)
          out = np.array(points[0])
          for i in range(2, numVectors):
              out += points[i]
          out = out / numVectors
          return out
~~~
    </div>
    </div>
    </div>

1. Finally, lets calculate how different our new centroids are compared to our initial centroids. This will be used to determine if we have converged to the right set of centroids. To do this we create a variable named `tempDist` that represents the distance between the old centroids and new centroids. In Scala and Java, we use the `squaredDist` function to compute the distance between two vectors. For Python we can use `np.sum`. We sum up the distance over `K` centroids and print this value.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var tempDist = 0.0
      for (i <- 0 until K) {
        tempDist += centroids(i).squaredDist(newCentroids(i))
      }
      println("Finished iteration (delta = " + tempDist + ")")
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      double tempDist = 0.0;
      for (int i = 0; i < K; i++) {
        tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
      }
      for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
        centroids.set(t.getKey(), t.getValue());
      }
      System.out.println("Finished iteration (delta = " + tempDist + ")");
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
      print "Finished iteration (delta = " + str(tempDist) + ")"
~~~
    </div>
    </div>

    You can now save `WikipediaKMeans`, and [run it](#running-the-program) to make sure our program is working fine so far. If everything went right, you should see the output similar to the following. (NOTE: Your output may not exactly match this as we use a random set of initial centers).

    <pre class="prettyprint lang-bsh">Finished iteration (delta = 0.025900765093161377)</pre>

1. The above steps represent one iteration of the K-Means algorithm. We will need to repeat these steps until the distance between newly computed centers and the ones from the previous iteration become lesser than some small constant. (`convergeDist` in our program). To do this we put our steps inside a `do while` loop and check if `tempDist` is lower than the convergence constant. Putting this together with the previous steps, our code will look like:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      do {
        var closest = data.map(p => (closestPoint(p._2, centroids), p._2))
        var pointsGroup = closest.groupByKey()
        var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()

        tempDist = 0.0
        for (i <- 0 until K) {
          tempDist += centroids(i).squaredDist(newCentroids(i))
        }

        // Assign newCentroids to centroids
        for (newP <- newCentroids) {
          centroids(newP._1) = newP._2
        }
        iter += 1
        println("Finished iteration " + iter + " (delta = " + tempDist + ")")
      } while (tempDist > convergeDist)
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      do {
          JavaPairRDD<Integer, Vector> closest = data.map(
            new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
              public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
                return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
              }
             }
            );
          JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
          Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
            new Function<List<Vector>, Vector>() {
              public Vector call(List<Vector> ps) throws Exception {
                return average(ps);
              }
            }).collectAsMap();
          double tempDist = 0.0;
          for (int i = 0; i < K; i++) {
            tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
          }
          for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
            centroids.set(t.getKey(), t.getValue());
          }
          System.out.println("Finished iteration (delta = " + tempDist + ")");

        } while (tempDist > convergeDist);
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
    while tempDist > convergeDist:
        closest = data.map(
            lambda (x, y) : (closestPoint(y, centroids), y))
        pointsGroup = closest.groupByKey()
        newCentroids = pointsGroup.mapValues(
            lambda x : average(x)).collectAsMap()

        tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
        for (x, y) in newCentroids.iteritems():
            centroids[x] = y
        print "Finished iteration (delta = " + str(tempDist) + ")"
~~~
    </div>
    </div>

1. **Exercise:** After the `do while` loop completes, write code to print the titles of 10 articles assigned to each cluster.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      val numArticles = 10
      for((centroid, centroidI) <- centroids.zipWithIndex) {
        // print numArticles articles which are assigned to this centroid’s cluster
        data.filter(p => (closestPoint(p._2, centroids) == centroidI)).take(numArticles).foreach(
            x => println(x._1))
        println()
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      System.out.println("Cluster with some articles:");
      int numArticles = 10;
      for (int i = 0; i < centroids.size(); i++) {
        final int index = i;
        List<Tuple2<String, Vector>> samples =
          data.filter(new Function<Tuple2<String, Vector>, Boolean>() {
            public Boolean call(Tuple2<String, Vector> in) throws Exception {
              return closestPoint(in._2(), centroids) == index;
            }
          }).take(numArticles);
        for(Tuple2<String, Vector> sample: samples) {
          System.out.println(sample._1());
        }
        System.out.println();
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      print "Clusters with some articles"
      numArticles = 10
      for i in range(0, len(centroids)):
        samples = data.filter(lambda (x,y) : closestPoint(y, centroids) == i).take(numArticles)
        for (name, features) in samples:
          print name

        print " "
~~~
    </div>
    </div>
    </div>

1. You can save `WikipediaKMeans` and [run your program](#running-the-program) now. If everything goes well your algorithm will converge after some iterations and your final output should have clusters similar to the following output. Recall that our feature vector consisted of the number of times a page was visited in every hour of the day. We can see that pages are clustered together by _language_ indicating that they are accessed during the same hours of the day.

    <pre class="nocode">
    ja %E6%AD%8C%E8%97%A4%E9%81%94%E5%A4%AB
    ja %E7%8B%90%E3%81%AE%E5%AB%81%E5%85%A5%E3%82%8A
    ja %E3%83%91%E3%82%B0
    ja %E7%B4%AB%E5%BC%8F%E9%83%A8
    ja %E3%81%8A%E5%A7%89%E7%B3%BB
    ja Reina
    ja %E8%B8%8A%E3%82%8A%E5%AD%97
    ja %E3%83%90%E3%82%BB%E3%83%83%E3%83%88%E3%83%8F%E3%82%A6%E3%83%B3%E3%83%89
    ja %E3%81%BF%E3%81%9A%E3%81%BB%E3%83%95%E3%82%A3%E3%83%8A%E3%83%B3%E3%82%B7%E3%83%A3%E3%83%AB%E3%82%B0%E3%83%AB%E3%83%BC%E3%83%97
    ja %E6%96%B0%E6%BD%9F%E7%9C%8C%E7%AB%8B%E6%96%B0%E6%BD%9F%E5%8D%97%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1

    ru %D0%A6%D0%B8%D1%80%D0%BA%D0%BE%D0%BD%D0%B8%D0%B9
    de Kirchenstaat
    ru %D0%90%D0%B2%D1%80%D0%B0%D0%B0%D0%BC
    de Migr%C3%A4ne
    de Portal:Freie_Software
    de Datenflusskontrolle
    de Dornier_Do_335
    de.b LaTeX-Schnellkurs:_Griechisches_Alphabet
    de Mach-Zahl
    ru Total_Commander

    en 761st_Tank_Battalion_(United_States)
    en File:N34---Douglas-DC3-Cockpit.jpg
    en Desmond_Child
    en Philadelphia_Freeway
    en Zygon
    en File:Ruth1918.jpg
    en Sally_Rand
    en File:HLHimmler.jpg
    en Waiting_to_Exhale
    en File:Sonic1991b.jpg
    </pre>

1. In case you want to look at the complete solution, here is how `WikipediaKMeans` will look after all the above steps have been completed.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
    import spark.SparkContext
    import spark.SparkContext._
    import spark.util.Vector

    import org.apache.log4j.Logger
    import org.apache.log4j.Level

    import scala.util.Random
    import scala.io.Source

    object WikipediaKMeans {
      def parseVector(line: String): Vector = {
          return new Vector(line.split(',').map(_.toDouble))
      }

      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
        var index = 0
        var bestIndex = 0
        var closest = Double.PositiveInfinity
        for (i <- 0 until centers.length) {
          val tempDist = p.squaredDist(centers(i))
          if (tempDist < closest) {
            closest = tempDist
            bestIndex = i
          }
        }
        return bestIndex
      }

      def average(ps: Seq[Vector]) : Vector = {
        val numVectors = ps.size
        var out = new Vector(ps(0).elements)
        for (i <- 1 until numVectors) {
          out += ps(i)
        }
        out / numVectors
      }

      // Add any new functions you need here

      def main(args: Array[String]) {
        Logger.getLogger("spark").setLevel(Level.WARN)
        val sparkHome = "/root/spark"
        val jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar"
        val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
        val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim

        val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))

        val K = 10
        val convergeDist = 1e-6

        val data = sc.textFile(
            "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
                t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()

        val count = data.count()
        println("Number of records " + count)

        // Your code goes here
        var centroids = data.takeSample(false, K, 42).map(x => x._2)
        var tempDist = 1.0
        do {
          var closest = data.map(p => (closestPoint(p._2, centroids), p._2))
          var pointsGroup = closest.groupByKey()
          var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()
          tempDist = 0.0
          for (i <- 0 until K) {
            tempDist += centroids(i).squaredDist(newCentroids(i))
          }
          for (newP <- newCentroids) {
            centroids(newP._1) = newP._2
          }
          println("Finished iteration (delta = " + tempDist + ")")
        } while (tempDist > convergeDist)

        println("Clusters with some articles:")
        val numArticles = 10
        for((centroid, centroidI) <- centroids.zipWithIndex) {
          // print numArticles articles which are assigned to this centroid’s cluster
          data.filter(p => (closestPoint(p._2, centroids) == centroidI)).take(numArticles).foreach(
              x => println(x._1))
          println()
        }

        sc.stop()
        System.exit(0)
      }
    }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
    import org.apache.log4j.Logger;
    import org.apache.log4j.Level;
    import java.io.*;
    import java.util.*;
    import com.google.common.collect.Lists;

    import scala.Tuple2;
    import spark.api.java.*;
    import spark.api.java.function.*;
    import spark.util.Vector;


    public class WikipediaKMeans {
      static int closestPoint(Vector p, List<Vector> centers) {
        int bestIndex = 0;
        double closest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < centers.size(); i++) {
          double tempDist = p.squaredDist(centers.get(i));
          if (tempDist < closest) {
            closest = tempDist;
            bestIndex = i;
          }
        }
        return bestIndex;
      }

      static Vector average(List<Vector> ps) {
        int numVectors = ps.size();
        Vector out = new Vector(ps.get(0).elements());
        for (int i = 0; i < numVectors; i++) {
          out.addInPlace(ps.get(i));
        }
        return out.divide(numVectors);
      }

      public static void main(String[] args) throws Exception {
        Logger.getLogger("spark").setLevel(Level.WARN);
        String sparkHome = "/root/spark";
        String jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar";
        String master = JavaHelpers.getSparkUrl();
        String masterHostname = JavaHelpers.getMasterHostname();
        JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans",
          sparkHome, jarFile);

        int K = 10;
        double convergeDist = .000001;

        JavaPairRDD<String, Vector> data = sc.textFile(
          "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
          new PairFunction<String, String, Vector>() {
            public Tuple2<String, Vector> call(String in) throws Exception {
              String[] parts = in.split("#");
              return new Tuple2<String, Vector>(
               parts[0], JavaHelpers.parseVector(parts[1]));
            }
          }).cache();


        long count = data.count();
        System.out.println("Number of records " + count);

        List<Tuple2<String, Vector>> centroidTuples = data.takeSample(false, K, 42);
        final List<Vector> centroids = Lists.newArrayList();
        for (Tuple2<String, Vector> t: centroidTuples) {
          centroids.add(t._2());
        }

        System.out.println("Done selecting initial centroids");
        double tempDist;
        do {
          JavaPairRDD<Integer, Vector> closest = data.map(
            new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
              public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
                return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
              }
            }
          );

          JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
          Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
            new Function<List<Vector>, Vector>() {
             public Vector call(List<Vector> ps) throws Exception {
               return average(ps);
            }
          }).collectAsMap();
          tempDist = 0.0;
          for (int i = 0; i < K; i++) {
            tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
          }
          for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
            centroids.set(t.getKey(), t.getValue());
          }
          System.out.println("Finished iteration (delta = " + tempDist + ")");

        } while (tempDist > convergeDist);

        System.out.println("Cluster with some articles:");
        int numArticles = 10;
        for (int i = 0; i < centroids.size(); i++) {
          final int index = i;
          List<Tuple2<String, Vector>> samples =
          data.filter(new Function<Tuple2<String, Vector>, Boolean>() {
            public Boolean call(Tuple2<String, Vector> in) throws Exception {
            return closestPoint(in._2(), centroids) == index;
          }}).take(numArticles);
          for(Tuple2<String, Vector> sample: samples) {
           System.out.println(sample._1());
          }
          System.out.println();
        }
        sc.stop();
        System.exit(0);
      }
    }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
   import os
   import sys

   import numpy as np
   from pyspark import SparkContext

   def setClassPath():
       oldClassPath = os.environ.get('SPARK_CLASSPATH', '')
       cwd = os.path.dirname(os.path.realpath(__file__))
       os.environ['SPARK_CLASSPATH'] = cwd + ":" + oldClassPath

   def parseVector(line):
       return np.array([float(x) for x in line.split(',')])

   def closestPoint(p, centers):
       bestIndex = 0
       closest = float("+inf")
       for i in range(len(centers)):
           dist = np.sum((p - centers[i]) ** 2)
           if dist < closest:
               closest = dist
               bestIndex = i
       return bestIndex

   def average(points):
       numVectors = len(points)
       out = np.array(points[0])
       for i in range(2, numVectors):
           out += points[i]
       out = out / numVectors
       return out

   if __name__ == "__main__":
       setClassPath()
       master = open("/root/spark-ec2/cluster-url").read().strip()
       masterHostname = open("/root/spark-ec2/masters").read().strip()
       sc = SparkContext(master, "PythonKMeans")
       K = 10
       convergeDist = 1e-5

       lines = sc.textFile(
           "hdfs://" + masterHostname + ":9000/wikistats_featurized")

       data = lines.map(
           lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()
       count = data.count()
       print "Number of records " + str(count)

       # TODO: PySpark does not support takeSample(). Use first K points instead.
       centroids = map(lambda (x, y): y, data.take(K))
       tempDist = 1.0

       while tempDist > convergeDist:
           closest = data.map(
               lambda (x, y) : (closestPoint(y, centroids), y))
           pointsGroup = closest.groupByKey()
           newCentroids = pointsGroup.mapValues(
               lambda x : average(x)).collectAsMap()

           tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
           for (x, y) in newCentroids.iteritems():
               centroids[x] = y
           print "Finished iteration (delta = " + str(tempDist) + ")"
           sys.stdout.flush()


       print "Clusters with some articles"
       numArticles = 10
       for i in range(0, len(centroids)):
         samples = data.filter(lambda (x,y) : closestPoint(y, centroids) == i).take(numArticles)
         for (name, features) in samples:
           print name

         print " "
~~~
    </div>
    </div>
    </div>

1. **Challenge Exercise:** The K-Means implementation uses a `groupBy` and `mapValues` to compute the new centers. This can be optimized by using a running sum of the vectors that belong to a cluster and running counter of the number of vectors present in a cluster. How would you use the Spark API to implement this?


