---
layout: global
title: Command Line Preprocessing and Featurization
prev: machine-learning-with-spark.html
next: machine-learning-with-spark.html
---

In this chapter, we will walk you through the steps to preprocess and featurize the Wikipedia dataset.

## Command Line Walkthrough

   -  We will start by entering the shell and loading the data.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
        cd /root/
        /root/spark/spark-shell
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
        cd /root/
        /root/spark/pyspark
        import numpy as np
      ~~~
      </div>
      </div>

   -  Next, load the data.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
        val data = sc.textFile("/wikistats_20090505-07_restricted")
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
        data = sc.textFile("/wikistats_20090505-07_restricted")
      ~~~
      </div>
      </div>

   -  Next, for every line of data, we collect a tuple with elements described next. The first element is what we will call the "full document title", a concatenation of the project code and page title. The second element is a key-value pair whose key is the hour from the `<date-time>` field and whose value is the number of views that occurred in this hour.

      There are a few new points to note about the code below. First, `data.map` takes each line of data in the RDD data and applies the function passed to it. The first step splits the line of data into the five data fields we discussed in the Spark exercises above. The second step extracts the hour information from the `<date-time>` string and we then form the output tuple.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
      val featureMap = data.map(line => {
        val Array(dateTime, projectCode, pageTitle, numViews, numBytes) = line.trim.split("\\s+")
        val hour = dateTime.substring(9, 11).toInt
        (projectCode+" "+pageTitle, hour -> numViews.toInt)
      })
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
      featureMap = data.map(lambda x: x.strip().split(" ")).map(lambda x: (x[1]+" "+x[2], (int(x[0][9:11]), int(x[3]))))
      ~~~
      </div>
      </div>

      Now we want to find the average hourly views for each article (average for the same hour across different days).

      In the code below, we first take our tuples in the RDD `featureMap` and, treating the first elements (i.e., article name) as keys and the second elements (i.e., hoursViewed) as values, group all the values for a single key (i.e., a single article) together using `groupByKey`.  We put the article name in a variable called `article` and the multiple tuples of hours and pageviews associated with the current `article` in a variable called `hoursViews`. The for loop then collects the number of days for which we have a particular hour of data in `counts[hour]` and the total pageviews at hour across all these days in `sums[hour]`. Finally, we use the syntax `sums zip counts` to make an array of tuples with parallel elements from the sums and counts arrays and use this to calculate the average pageviews at particular hours across days in the data set.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
      val featureGroup = featureMap.groupByKey.map(grouped => {
        val (article, hoursViews) = grouped
        val sums = Array.fill[Int](24)(0)
        val counts = Array.fill[Int](24)(0)
        for((hour, numViews) <- hoursViews) {
          counts(hour) += 1
          sums(hour) += numViews
        }
        val avgs: Array[Double] =
          for((sum, count) <- sums zip counts) yield
            if(count > 0) sum/count.toDouble else 0.0
        article -> avgs
      })
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
      def groupFeatures(articles, hoursView):
        sums = np.zeros(24)
        counts = np.zeros(24)
        for (hour, numViews) in hoursView:
          counts[hour] += 1
          sums[hour] += numViews
        avgs = np.zeros(24)
        avgs = np.array(map(lambda (x,y): float(x)/float(y) if y != 0 else 0, zip(sums, counts)))
        return (articles, avgs)

      featureGroup = featureMap.groupByKey(16).map(lambda (x,y): groupFeatures(x,y))
      ~~~
      </div>
      </div>

   -  Now suppose we’re only interested in those articles that were viewed at least once in each hour during the data collection time.

      To do this, we filter to find those articles with an average number of views (the second tuple element in an article tuple) greater than zero in every hour.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
      val featureGroupFiltered = featureGroup.filter(t => t._2.forall(_ > 0))
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
      featureGroupFiltered = featureGroup.filter(lambda (x,y): np.count_nonzero(y) == len(y))
      ~~~
      </div>
      </div>

   -  So far article popularity is still implicitly in our feature vector (the sum of the average views per hour is the average views per day if the number of days of data is constant across hours).  Since we are interested only in which times are more popular viewing times for each article, we next divide out by this sum.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
      val featurizedRDD = featureGroupFiltered.map(t => {
        val avgsTotal = t._2.sum
        t._1 -> t._2.map(_ /avgsTotal)
      })
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
      featurizedRDD = featureGroupFiltered.map(lambda (x,y): (x, y/y.sum()))
      ~~~
      </div>
      </div>

   -  Now we can cache and save the RDD to a file for later use. To save our features to a file, we first create a string of comma-separated values for each data point and then save it in HDFS as file named `wikistats_featurized`.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
      featurizedRDD.cache.map(t => t._1 + "#" + t._2.mkString(",")).saveAsTextFile("/wikistats_featurized")
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
      featurizedRDD.cache().map(lambda (x,y): (x + '#' + ','.join(map(str, y)))).saveAsTextFile("/wikistats_featurized")
      ~~~
      </div>
      </div>

## Exercises
2. In this exercise, we examine the preprocessed data.

    - Count the number of records in the preprocessed data.  Recall that we potentially threw away some data when we filtered out records with zero views in a given hour.

      <div class="codetabs">
      <div data-lang="scala" markdown="1">
      ~~~
      featurizedRDD.count()
      ~~~
      </div>
      <div data-lang="python" markdown="1">
      ~~~
      featurizedRDD.count()
      ~~~
      </div>
      </div>

      <div class="solution" markdown="1">
      Number of records in the preprocessed data: 802450
      </div>


   - Print the feature vectors for the Wikipedia articles with project code “en” and the following titles: Computer_science, Machine_learning.

     <div class="codetabs">
     <div data-lang="scala" markdown="1">
     ~~~
     val featuresCSML = featurizedRDD.filter(t => t._1 == "en Computer_science" || t._1 == "en Machine_learning").collect
     featuresCSML.foreach(x => println(x._1 + "," + x._2.mkString(" ")))
     ~~~
     </div>
     <div data-lang="python" markdown="1">
     ~~~
     featuresCSML = featurizedRDD.filter(lambda (x,y): x == "en Computer_science" || x == "en Machine_learning").collect() 
     for (name, features) in featuresCSML:
       print name + "," + ','.join(map(str, features))
     ~~~
     </div>
     </div>

     <div class="solution">
     <textarea rows="12" style="width: 100%" readonly>
     (en Machine_learning, [0.03708182184602984,0.027811366384522376,0.031035872632003234,0.033454252317613876,0.033051189036678766,0.023780733575171308,0.03224506247480856,0.029826682789197912,0.04997984683595326,0.04433696090286176,0.04997984683595326,0.04474002418379687,0.04272470777912134,0.054816606207174545,0.054816606207174545,0.04474002418379687,0.054010479645304324,0.049173720274083045,0.049173720274083045,0.05038291011688836,0.04594921402660219,0.04957678355501815,0.03667875856509473,0.030632809351068126])
     (en Computer_science, [0.03265137425087828,0.057656540607563554,0.03306468278569953,0.033374664186815464,0.03709444100020666,0.03947096507542881,0.03502789832610044,0.03637115106426948,0.036577805331680105,0.0421574705517669,0.04267410622029345,0.03885100227319695,0.03885100227319695,0.046083901632568716,0.04691051870221121,0.050320314114486474,0.05259351105600331,0.04649721016738996,0.04732382723703245,0.048357098574085565,0.04236412481917752,0.043190741888820015,0.03626782393056417,0.03626782393056417])
     </textarea>
     </div>

## Standalone Spark program
Finally, if you wish to create a standalone Spark program to perform featurization, you can just copy and paste all of the code from our solution below.


<div class="codetabs">
  <div data-lang="scala" markdown="1">
   <div class="solution" markdown="1">

   Place the following code within a Scala `object` and call the `featurization` function from a `main` function:

    import scala.io.Source
    import spark.SparkContext
    import SparkContext._
    lazy val hostname = Source.fromFile("/root/mesos-ec2/masters").mkString.trim
    def featurization(sc: SparkContext) {
      val featurizedRdd = sc.textFile("hdfs://"+hostname+":9000/wikistats_20090505-07_restricted").map{line => {
        val Array(dateTime, projectCode, pageTitle, numViews, numBytes) = line.trim.split("\\s+")
        val hour = dateTime.substring(dateTime.indexOf("-")+1, dateTime.indexOf("-")+3).toInt
        (projectCode+" "+pageTitle, hour -> numViews.toInt)
      }}.groupByKey.map{ grouped => {
        val (article, hoursViews) = grouped
        val sums = Array.fill[Int](24)(0)
        val counts = Array.fill[Int](24)(0)
        for((hour, numViews) <- hoursViews) {
          sums(hour) += numViews
          counts(hour) += 1
        }
        val avgs: Array[Double] =
          for((sum, count) <- sums zip counts) yield
            if(count > 0) sum/count.toDouble else 0.0
        article -> avgs
      }}.filter{ t => {
        t._2.forall(_ > 0)
      }}.map{ t => {
        val avgsTotal = t._2.sum
        t._1 -> t._2.map(_ / avgsTotal)
      }}
      featurizedRdd.cache()
      println("Number of records in featurized dataset: " + featurizedRdd.count)
      println("Selected feature vectors:")
      featurizedRdd.filter{ t => {
        t._1 == "en Computer_science" || t._1 == "en Machine_learning"
      }}.collect.map{t => t._1 -> t._2.mkString("[",",","]")}.foreach(println)
      featurizedRDD.cache.map(t => t._1 + "#" + t._2.mkString(",")).saveAsTextFile(
          "hdfs://"+hostname+":9000/wikistats_featurized")
    }

   </div>
  </div>
  <div data-lang="python" markdown="1">
   <div class="solution" markdown="1">

~~~
import sys

import numpy as np
from pyspark import SparkContext

def groupFeatures(articles, hoursView):
  sums = np.zeros(24)
  counts = np.zeros(24)
  for (hour, numViews) in hoursView:
    counts[hour] += 1
    sums[hour] += numViews
  avgs = np.zeros(24)
  avgs = np.array(map(lambda (x,y): float(x)/float(y) if y != 0 else 0, zip(sums, counts)))
  return (articles, avgs)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print >> sys.stderr, \
            "Usage: FeaturizeWikipedia <master> <file>"
        exit(-1)
    sc = SparkContext(sys.argv[1], "FeaturizeWikipedia")

    data = sc.textFile(sys.argv[2])
    featureMap = data.map(lambda x: x.strip().split(" ")).map(
        lambda x: (x[1]+" "+x[2], (int(x[0][9:11]), int(x[3]))))
    featureGroup = featureMap.groupByKey(16).map(
        lambda (x,y): groupFeatures(x,y))

    featureGroupFiltered = featureGroup.filter(
        lambda (x,y): np.count_nonzero(y) == len(y))
    featurizedRDD = featureGroupFiltered.map(
        lambda (x,y): (x, y/y.sum()))

    featurizedRDD.cache().map(
        lambda (x,y): (x + '#' + ','.join(map(str, y)))).saveAsTextFile("/wikistats_featurized")
~~~
   </div>
  </div>
</div>
