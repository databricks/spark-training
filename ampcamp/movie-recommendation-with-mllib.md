---
layout: global
title: Movie Recommendation with MLlib
categories: [module]
navigation:
  weight: 70
  show: true
---

In this chapter, we will use MLlib to make personalized movie recommendations
tailored *for you*. We will work with 10 million ratings from 72,000 users on
10,000 movies, collected by [MovieLens](http://movielens.umn.edu/).  This
dataset is pre-loaded in the HDFS on your cluster in `/movielens/large`. For
quick testing of your code, you may want to use a smaller dataset under
`/movielens/medium`, which contains 1 million ratings from 6000 users on 4000
movies.

##Data set

We will use two files from this MovieLens dataset: "`ratings.dat`" and
"`movies.dat`". All ratings are contained in the file "`ratings.dat`" and are in
the following format:

~~~
UserID::MovieID::Rating::Timestamp
~~~

Movie information is in the file "`movies.dat`" and is in the following format:

~~~
MovieID::Title::Genres
~~~

##Collaborative filtering

Collaborative filtering is commonly used for recommender systems. These
techniques aim to fill in the missing entries of a user-item association matrix,
in our case, the user-movie rating matrix. MLlib currently supports model-based
collaborative filtering, in which users and products are described by a small
set of latent factors that can be used to predict missing entries. In
particular, we implement the alternating least squares (ALS) algorithm to learn
these latent factors.

<p style="text-align: center;">
  <img src="img/matrix_factorization.png"
       title="Matrix Factorization"
       alt="Matrix Factorization"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

##Setup

We will be using a standalone project template for this exercise. In your AMI,
this has been setup in `/root/machine-learning/scala/`. You should find the following items
in the directory.

<div class="codetabs">
<div data-lang="scala">

<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>sbt</code>: Directory containing the SBT tool</li>
<li><code>build.sbt</code>: SBT project file</li>
<li><code>MovieLensALS.scala</code>: Main Scala program that you are going to edit, compile and run</li>
</ul>
</div>

The main file you are going to edit, compile, and run for the exercises is
<code>MovieLensALS.scala</code>. It should look as follows:
</div>
</div>

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import java.util.Random

import org.apache.log4j.Logger
import org.apache.log4j.Level

import scala.io.Source

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ALS, Rating, MatrixFactorizationModel}

object MovieLensALS {

  def main(args: Array[String]) {

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    if (args.length != 1) {
      println("Usage: sbt/sbt package \"run movieLensHomeDir\"")
      exit(1)
    }

    // set up environment

    val jarFile = "target/scala-2.10/movielens-als_2.10-0.0.jar"
    val sparkHome = "/root/spark"
    val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
    val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim
    val conf = new SparkConf()
      .setMaster(master)
      .setSparkHome(sparkHome)
      .setAppName("MovieLensALS")
      .set("spark.executor.memory", "8g")
      .setJars(Seq(jarFile))
    val sc = new SparkContext(conf)

    // load ratings and movie titles

    val movieLensHomeDir = "hdfs://" + masterHostname + ":9000" + args(0)

    val ratings = sc.textFile(movieLensHomeDir + "/ratings.dat").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }

    val movies = sc.textFile(movieLensHomeDir + "/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap

    // your code here

    // clean up

    sc.stop();
    System.exit(0)
  }
  
  /** Compute RMSE (Root Mean Squared Error). */
  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], n: Long) = {
    // ...
  }
  
  /** Elicitate ratings from command-line. */
  def elicitateRatings(movies: Seq[(Int, String)]) = {
    // ...
  }
}
~~~
</div>
</div>

Let's first take a closer look at our template code in a text editor on the
cluster itself, then we'll start adding code to the template. Locate the
`MovieLensALS` class and open it with a text editor.

<div class="codetabs">
<div data-lang="scala">
<pre class="prettyprint lang-bsh">
cd /root/machine-learning/scala
vim MovieLensALS.scala  # If you don't know vim, you can use emacs or nano
</pre>
</div>
</div>

The cluster machines have vim, emacs, and nano installed for
editing. Alternatively, you can use your favorite text editor locally and then
copy-paste content into vim, emacs, or nano before running it.

For any Spark computation, we first create a SparkConf object and use it to
create a Spark context object. For Scala or Java programs, we do that by
providing the Spark cluster URL, the Spark home directory, and the JAR file that
will be generated when we compile our program. For Python programs, we only need
to provide the Spark cluster URL. Finally, we also name our program
"MovieLensALS" to identify it in Spark's web UI.

This is what it looks like in our template code:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val conf = new SparkConf()
      .setMaster(master)
      .setSparkHome(sparkHome)
      .setAppName("MovieLensALS")
      .set("spark.executor.memory", "8g")
      .setJars(Seq(jarFile))
    val sc = new SparkContext(conf)
~~~
</div>
</div>

Next, the code uses the SparkContext to read in ratings. Recall that the rating
file is a text file with "`::`" as the delimiter. The code parses each line to
create a RDD for ratings that contains `(Int, Rating)` pairs. We only keep the
last digit of the timestamp as a random key. The `Rating` class is a wrapper
around tuple `(user: Int, product: Int, rating: Double)` defined in
`org.apache.spark.mllib.recommendation` package.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val movieLensHomeDir = "hdfs://" + masterHostname + ":9000" + args(0)

    val ratings = sc.textFile(movieLensHomeDir + "/ratings.dat").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }
~~~
</div>
</div>

Next, the code read in movie ids and titles, collect them into a movie id to
title map.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val movies = sc.textFile(movieLensHomeDir + "/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap
~~~
</div>
</div>

Now, let's make our first edit to add code to get a summary of the ratings.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val numRatings = ratings.count
    val numUsers = ratings.map(_._2.user).distinct.count
    val numMovies = ratings.map(_._2.product).distinct.count

    println("Got " + numRatings + " ratings from " 
      + numUsers + " users on " + numMovies + " movies.")
~~~
</div>
</div>

##Running the program

Before we compute movie recommendations, here is a quick reminder on how you can
run the program at any point during this exercise. Save the `MovieLensALS`
file run the following commands:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/machine-learning/scala
# change the folder name from "medium" to "large" to run on the large data set
sbt/sbt package "run /movielens/medium"
</pre>

This command will compile the `MovieLensALS` class and create a JAR file in
`/root/machine-learning/scala/target/scala-2.10/`. Finally, it will run the program. You
should see output similar to the following on your screen:

</div>

<pre>
Got 1000209 ratings from 6040 users on 3706 movies.
</pre>

</div>

##Rating elicitation

To make recommendation *for you*, we are going to learn your taste by asking you
to rate a few movies. The movies should be popular ones to increase the chance
of receiving ratings from you. To do this, we need to count ratings received for
each movie and sort movies by rating counts. Then, take the top, e.g., 50, most
rated movies and sample a small subset for rating elicitation.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
    val mostRatedMovieIds = ratings.map(_._2.product) // extract movie ids
                                   .countByValue      // count ratings per movie
                                   .toSeq             // convert map to Seq
                                   .sortBy(- _._2)    // sort by rating count
                                   .take(50)          // take 50 most rated
                                   .map(_._1)         // get their ids
    val random = new Random(0)
    val selectedMovies = mostRatedMovieIds.filter(x => random.nextDouble() < 0.2)
                                          .map(x => (x, movies(x)))
                                          .toSeq
~~~
</div>
</div>
</div>

Then for each of the selected movies, we will ask you to give a rating (1-5) or
0 if you have never watched this movie. The method `eclicitateRatings` returns
your ratings, where you receive a special user id `0`. The ratings are converted
to a `RDD[Rating]` instance via `sc.parallelize`.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val myRatings = elicitateRatings(selectedMovies)
    val myRatingsRDD = sc.parallelize(myRatings)
~~~
</div>
</div>

When you run the application, you should see prompt similar to the following:

~~~
Please rate the following movie (1-5 (best), or 0 if not seen):
Raiders of the Lost Ark (1981): 
~~~

##Splitting training data

We will use MLlib's `ALS` to train a `MatrixFactorizationModel`, which takes a
`RDD[Rating]` object as input. ALS has training parameters such as rank for
matrix factors and regularization constants. To determine a good combination of
the training parameters, we split the data into three non-overlapping subsets,
named training, test, and validation, based on the last digit of the timestamp,
and cache them. We will train multiple models based on the training set, select
the best model on the validation set based on RMSE (Root Mean Squared Error),
and finally evaluate the best model on the test set. We also add your ratings to
the training set to make recommendations for you. We hold the training,
validation, and test sets in memory by calling `persist` because we need to
visit them multiple times.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val numPartitions = 20
    val training = ratings.filter(x => x._1 < 6)
                          .values
                          .union(myRatingsRDD)
                          .repartition(numPartitions)
                          .persist
    val validation = ratings.filter(x => x._1 >= 6 && x._1 < 8)
                            .values
                            .repartition(numPartitions)
                            .persist
    val test = ratings.filter(x => x._1 >= 8).values.persist

    val numTraining = training.count
    val numValidation = validation.count
    val numTest = test.count

    println("Training: " + numTraining + ", validation: " + numValidation + ", test: " + numTest)
~~~
</div>
</div>

After the split, you should see

~~~
Training: 602251, validation: 198919, test: 199049.
~~~

##Training using ALS

In this section, we will use `ALS.train` to train a bunch of models, and select
and evaluate the best.  Among the training paramters of ALS, the most important
ones are rank, lambda (regularization constant), and number of iterations. The
`train` method of ALS we are going to use is defined as the following:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
object ALS {

  def train(ratings: RDD[Rating], rank: Int, iterations: Int, lambda: Double)
    : MatrixFactorizationModel = {
    // ...
  }
}
~~~
</div>
</div>

Ideally, we want to try a large number of combinations of them in order to find
the best one. Due to time constraint, we will test only 8 combinations resulting
from the cross product of 2 different ranks (8 and 12), 2 different lambdas (1.0
and 10.0), and two different numbers of iterations (10 and 20). We use the
provided method `computeRmse` to compute the RMSE on the validation set for each
model. The model with the smallest RMSE on the validation set becomes the one
selected and its RMSE on the test set is used as the final metric.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
    val ranks = List(8, 12)
    val lambdas = List(0.1, 10.0)
    val numIters = List(10, 20)
    var bestModel: Option[MatrixFactorizationModel] = None
    var bestValidationRmse = Double.MaxValue
    var bestRank = 0
    var bestLambda = -1.0
    var bestNumIter = -1
    for (rank <- ranks; lambda <- lambdas; numIter <- numIters) {
      val model = ALS.train(training, rank, numIter, lambda)
      val validationRmse = computeRmse(model, validation, numValidation)
      println("RMSE (validation) = " + validationRmse + " for the model trained with rank = " 
        + rank + ", lambda = " + lambda + ", and numIter = " + numIter + ".")
      if (validationRmse < bestValidationRmse) {
        bestModel = Some(model)
        bestValidationRmse = validationRmse
        bestRank = rank
        bestLambda = lambda
        bestNumIter = numIter
      }
    }

    val testRmse = computeRmse(bestModel.get, test, numTest)

    println("The best model was trained with rank = " + bestRank + " and lambda = " + bestLambda
      + ", and numIter = " + bestNumIter + ", and its RMSE on the test set is " + testRmse + ".")
~~~
</div>
</div>
</div>

Spark might take a minute or two to train the models. You should see the
following on the screen:

~~~
The best model was trained using rank 8 and lambda 10.0, and its RMSE on test is 0.8808492431998702.
~~~

##Recommending movies for you

As the last part of our tutorial, let's take a look at what movies our model
recommends for you. This is done by generating `(0, movieId)` pairs for all
movies you haven't rated and calling the model's `predict` method to get
predictions. Recall that `0` is the special user id assigned to you.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
class MatrixFactorizationModel {
  def predict(userProducts: RDD[(Int, Int)]): RDD[Rating] = {
    // ...
  }
}
~~~
</div>
</div>

After we get all predictions, let us list the top 50 recommendations and see
whether they look good to you.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
    val myRatedMovieIds = myRatings.map(_.product).toSet
    val candidates = sc.parallelize(movies.keys.filter(!myRatedMovieIds.contains(_)).toSeq)
    val recommendations = bestModel.get
                                   .predict(candidates.map((0, _)))
                                   .collect
                                   .sortBy(-_.rating)
                                   .take(50)

    var i = 1
    println("Movies recommended for you:")
    recommendations.foreach { r =>
      println("%2d".format(i) + ": " + movies(r.product))
      i += 1
    }
~~~
</div>
</div>
</div>

The output should be similar to

~~~
Movies recommended for you:
 1: Silence of the Lambs, The (1991)
 2: Saving Private Ryan (1998)
 3: Godfather, The (1972)
 4: Star Wars: Episode IV - A New Hope (1977)
 5: Braveheart (1995)
 6: Schindler's List (1993)
 7: Shawshank Redemption, The (1994)
 8: Star Wars: Episode V - The Empire Strikes Back (1980)
 9: Pulp Fiction (1994)
10: Alien (1979)
...
~~~

YMMV, and don't expect to see movies from this decade, becaused the data set is old.

##Exercises

### Comparing to a naive baseline ###

Does ALS output a non-trivial model? We can compare the evaluation result with a
naive baseline model that only outputs the average rating (or you may try one
that outputs the average rating per movie). Computing the baseline's RMSE is
straightforward:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
    val meanRating = training.union(validation).map(_.rating).mean
    val baselineRmse = math.sqrt(test.map(x => (meanRating - x.rating) * (meanRating - x.rating))
                                     .reduce(_ + _) / numTest)
    val improvement = (baselineRmse - testRmse) / baselineRmse * 100
    println("The best model improves the baseline by " + "%1.2f".format(improvement) + "%.")
~~~
</div>
</div>
</div>

The output should be

~~~
The best model improves the baseline by 20.96%.
~~~

It seems obvious that the trained model would outperform the naive
baseline. However, a bad combination of training parameters would lead to a
model worse than this naive baseline. Choosing the right set of parameters is
quite important for this task.

### Augmenting matrix factors ###

In this tutorial, we add your ratings to the training set. A better way to get
the recommendations for you is training a matrix factorization model first and
then augmenting the model using your ratings. If this sounds interesting to you,
you can take a look at the implementation of MatrixFactorizationModel and see
how to update the model for new users and new movies.
