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
dataset is pre-loaded in your USB drive under `data/movielens/large`. For
quick testing of your code, you may want to use a smaller dataset under
`data/movielens/medium`, which contains 1 million ratings from 6000 users on 4000
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

##Create training examples

To make recommendation *for you*, we are going to learn your taste by asking you
to rate a few movies. We have selected a small set of movies that have received the most
ratings from users in the MovieLens dataset. You can rate those movies by running `bin/rateMovies`:

~~~
python bin/rateMovies
~~~

When you run the script, you should see prompt similar to the following:

~~~
Please rate the following movie (1-5 (best), or 0 if not seen):
Toy Story (1995):
~~~

After you're done rating the movies, we save your ratings in `personalRatings.txt` in the MovieLens format,
where a special user id `0` is assigned to you.

`rateMovies` allows you to re-rate the movies if you'd like to see how your ratings affect your
recommendations.

If you don't have python installed, please copy `personalRatings.txt.template` to
`personalRatings.txt` and replace `?`s with your ratings.

##Setup

We will be using a standalone project template for this exercise. 

<div class="codetabs">

<div data-lang="scala">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
In the training USB drive, this has been setup in <code>machine-learning/scala/</code>.<br>
You should find the following items in the directory:
<li><code>build.sbt</code>: SBT project file</li>
<li><code>MovieLensALS.scala</code>: Main Scala program that you are going to edit, compile and run</li>
<li><code>solution</code>: Directory containing the solution code</li>
</ul>
</div>
</div>

<div data-lang="python">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
  In the training USB drive, this has been setup in <code>machine-learning/python/</code>.<br>
You should find the following items in the directory:
<li><code>MovieLensALS.py</code>: Main Python program that you are going to edit, compile and run</li>
<li><code>solution</code>: Directory containing the solution code</li>
</ul>
</div>
</div>

</div>

The following is the main file you are going to edit, compile, and run.

<div class="codetabs">
<div data-lang="scala" markdown="1" data-editable="true">

<div style="margin-bottom:10px"><code>MovieLensALS.scala</code> should look as follows:</div>

~~~
import java.io.File

import scala.io.Source

import org.apache.log4j.Logger
import org.apache.log4j.Level

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ALS, Rating, MatrixFactorizationModel}

object MovieLensALS {

  def main(args: Array[String]) {

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    if (args.length != 2) {
      println("Usage: [usb root directory]/spark/bin/spark-submit --driver-memory 2g --class MovieLensALS " +
        "target/scala-*/movielens-als-ssembly-*.jar movieLensHomeDir personalRatingsFile")
      sys.exit(1)
    }

    // set up environment

    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)

    // load personal ratings

    val myRatings = loadRatings(args(1))
    val myRatingsRDD = sc.parallelize(myRatings, 1)

    // load ratings and movie titles

    val movieLensHomeDir = args(0)

    val ratings = sc.textFile(new File(movieLensHomeDir, "ratings.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }

    val movies = sc.textFile(new File(movieLensHomeDir, "movies.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect().toMap

    // your code here

    // clean up
    sc.stop()
  }

  /** Compute RMSE (Root Mean Squared Error). */
  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], n: Long): Double = {
    // ...
  }

  /** Load ratings from file. */
  def loadRatings(path: String): Seq[Rating] = {
    // ...
  }
}
~~~

</div>

<div data-lang="python" markdown="1" data-editable="true">

<div style="margin-bottom:10px"><code>MovieLensALS.py</code> should look as follows:</div>

~~~python
#!/usr/bin/env python

import sys
import itertools
from math import sqrt
from operator import add
from os.path import join, isfile, dirname

from pyspark import SparkConf, SparkContext
from pyspark.mllib.recommendation import ALS

def parseRating(line):
    """
    Parses a rating record in MovieLens format userId::movieId::rating::timestamp .
    """
    # ...

def parseMovie(line):
    """
    Parses a movie record in MovieLens format movieId::movieTitle .
    """
    # ...

def loadRatings(ratingsFile):
    """
    Load ratings from file.
    """
    # ...

def computeRmse(model, data, n):
    """
    Compute RMSE (Root Mean Squared Error).
    """
    # ...

if __name__ == "__main__":
    if (len(sys.argv) != 3):
        print "Usage: [usb root directory]/spark/bin/spark-submit --driver-memory 2g " + \
          "MovieLensALS.py movieLensDataDir personalRatingsFile"
        sys.exit(1)

    # set up environment
    conf = SparkConf() \
      .setAppName("MovieLensALS") \
      .set("spark.executor.memory", "2g")
    sc = SparkContext(conf=conf)

    # load personal ratings
    myRatings = loadRatings(sys.argv[2])
    myRatingsRDD = sc.parallelize(myRatings, 1)
    
    # load ratings and movie titles

    movieLensHomeDir = sys.argv[1]

    # ratings is an RDD of (last digit of timestamp, (userId, movieId, rating))
    ratings = sc.textFile(join(movieLensHomeDir, "ratings.dat")).map(parseRating)

    # movies is an RDD of (movieId, movieTitle)
    movies = dict(sc.textFile(join(movieLensHomeDir, "movies.dat")).map(parseMovie).collect())

    # your code here
    
    # clean up
    sc.stop()
~~~

</div>
</div>

Let's first take a closer look at our template code in a text editor, then we'll start adding code to the template. Locate the
`MovieLensALS` class and open it with a text editor.

<div class="codetabs">

<div data-lang="scala">
<pre class="prettyprint lang-bsh">
usb/$ cd machine-learning/scala
vim MovieLensALS.scala  # Or your editor of choice
</pre>
</div>

<div data-lang="python">
<pre class="prettyprint lang-bsh">
usb/$ cd machine-learning/python
vim MovieLensALS.py  # Or your editor of choice
</pre>
</div>

</div>

For any Spark computation, we first create a SparkConf object and use it to
create a SparkContext object. Since we will be using spark-submit to execute the 
programs in this tutorial (more on spark-submit in the next section), we only need to 
configure the executor memory allocation and give the program a name, e.g. "MovieLensALS", to
identify it in Spark's web UI. In local mode, the web UI can be access at [`localhost:4040`](http://localhost:4040) during 
the execution of a program.

This is what it looks like in our template code:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val conf = new SparkConf()
      .setAppName("MovieLensALS")
      .set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
~~~
</div>
<div data-lang="python" markdown="1">
~~~python
    conf = SparkConf() \
      .setAppName("MovieLensALS") \
      .set("spark.executor.memory", "2g")
    sc = SparkContext(conf=conf)
~~~
</div>
</div>

Next, the code uses the SparkContext to read in ratings. Recall that the rating
file is a text file with "`::`" as the delimiter. The code parses each line to
create a RDD for ratings that contains `(Int, Rating)` pairs. We only keep the
last digit of the timestamp as a random key. The `Rating` class is a wrapper
around the tuple `(user: Int, product: Int, rating: Double)`.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val movieLensHomeDir = args(0)

    val ratings = sc.textFile(new File(movieLensHomeDir, "ratings.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }
~~~
</div>
<div data-lang="python" markdown="1">
~~~python
    movieLensHomeDir = sys.argv[1]

    # ratings is an RDD of (last digit of timestamp, (userId, movieId, rating))
    ratings = sc.textFile(join(movieLensHomeDir, "ratings.dat")).map(parseRating)
~~~
</div>
</div>

Next, the code read in movie ids and titles, collect them into a movie id to
title map.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val movies = sc.textFile(new File(movieLensHomeDir, "movies.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap
~~~
</div>
<div data-lang="python" markdown="1">
~~~python
    def parseMovie(line):
      fields = line.split("::")
      return int(fields[0]), fields[1]

    movies = dict(sc.textFile(join(movieLensHomeDir, "movies.dat")).map(parseMovie).collect())
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
<div data-lang="python" markdown="1">
~~~python
    numRatings = ratings.count()
    numUsers = ratings.values().map(lambda r: r[0]).distinct().count()
    numMovies = ratings.values().map(lambda r: r[1]).distinct().count()

    print "Got %d ratings from %d users on %d movies." % (numRatings, numUsers, numMovies)
~~~
</div>
</div>

##Running the program

Before we compute movie recommendations, here is a quick reminder on how you can
run the program at any point during this exercise. As mentioned above, we will use `spark-submit`
to execute your program in local mode for this tutorial. 

Starting with Spark 1.0,
`spark-submit` is the recommended way for running Spark applications, both on clusters and locally
in standalone mode.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
usb/$ cd machine-learning/scala

# The following command compiles the <code>MovieLensALS</code> class 
# and creates a jar file in <code>machine-learning/scala/target/scala-2.10/</code>
[usb root directory]/sbt/sbt assembly

# change the folder name from "medium" to "large" to run on the large data set
[usb root directory]/spark/bin/spark-submit --class MovieLensALS target/scala-2.10/movielens-als-assembly-0.1.jar [usb root directory]/data/movielens/medium/ ../personalRatings.txt
</pre>
</div>

<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
usb/$ cd machine-learning/python

# change the folder name from "medium" to "large" to run on the large data set
[usb root directory]/spark/bin/spark-submit MovieLensALS.py [usb root directory]/data/movielens/medium/ ../personalRatings.txt
</pre>
</div>

</div>

You should see output similar to the following on your screen:

<pre>
Got 1000209 ratings from 6040 users on 3706 movies.
</pre>

##Splitting training data

We will use MLlib's `ALS` to train a `MatrixFactorizationModel`, which takes a
`RDD[Rating]` object as input in Scala and `RDD[(user, product, rating)]` in Python. 
ALS has training parameters such as rank for
matrix factors and regularization constants. To determine a good combination of
the training parameters, we split the data into three non-overlapping subsets,
named training, test, and validation, based on the last digit of the timestamp,
and cache them. We will train multiple models based on the training set, select
the best model on the validation set based on RMSE (Root Mean Squared Error),
and finally evaluate the best model on the test set. We also add your ratings to
the training set to make recommendations for you. We hold the training,
validation, and test sets in memory by calling `cache` because we need to
visit them multiple times.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val numPartitions = 4
    val training = ratings.filter(x => x._1 < 6)
      .values
      .union(myRatingsRDD)
      .repartition(numPartitions)
      .cache()
    val validation = ratings.filter(x => x._1 >= 6 && x._1 < 8)
      .values
      .repartition(numPartitions)
      .cache()
    val test = ratings.filter(x => x._1 >= 8).values.cache()

    val numTraining = training.count()
    val numValidation = validation.count()
    val numTest = test.count()

    println("Training: " + numTraining + ", validation: " + numValidation + ", test: " + numTest)
~~~
</div>
<div data-lang="python" markdown="1">
~~~python
    numPartitions = 4
    training = ratings.filter(lambda x: x[0] < 6) \
      .values() \
      .union(myRatingsRDD) \
      .repartition(numPartitions) \
      .cache()

    validation = ratings.filter(lambda x: x[0] >= 6 and x[0] < 8) \
      .values() \
      .repartition(numPartitions) \
      .cache()

    test = ratings.filter(lambda x: x[0] >= 8).values().cache()

    numTraining = training.count()
    numValidation = validation.count()
    numTest = test.count()

    print "Training: %d, validation: %d, test: %d" % (numTraining, numValidation, numTest)
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
<div data-lang="scala" markdown="1" data-editable="true">
~~~
object ALS {

  def train(ratings: RDD[Rating], rank: Int, iterations: Int, lambda: Double)
    : MatrixFactorizationModel = {
    // ...
  }
}
~~~
</div>
<div data-lang="python" markdown="1" data-editable="true">
~~~python
class ALS(object):

    def train(cls, ratings, rank, iterations=5, lambda_=0.01, blocks=-1):
        # ...
        return MatrixFactorizationModel(sc, mod)
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
<div data-lang="scala" class="solution" markdown="1">
~~~
    val ranks = List(8, 12)
    val lambdas = List(1.0, 10.0)
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
<div data-lang="python" markdown="1">
<div data-lang="python" class="solution" markdown="1">
~~~python
    ranks = [8, 12]
    lambdas = [1.0, 10.0]
    numIters = [10, 20]
    bestModel = None
    bestValidationRmse = float("inf")
    bestRank = 0
    bestLambda = -1.0
    bestNumIter = -1

    for rank, lmbda, numIter in itertools.product(ranks, lambdas, numIters):
        model = ALS.train(training, rank, numIter, lmbda)
        validationRmse = computeRmse(model, validation, numValidation)
        print "RMSE (validation) = %f for the model trained with " % validationRmse + \
              "rank = %d, lambda = %.1f, and numIter = %d." % (rank, lmbda, numIter)
        if (validationRmse < bestValidationRmse):
            bestModel = model
            bestValidationRmse = validationRmse
            bestRank = rank
            bestLambda = lmbda
            bestNumIter = numIter

    testRmse = computeRmse(bestModel, test, numTest)

    # evaluate the best model on the test set
    print "The best model was trained with rank = %d and lambda = %.1f, " % (bestRank, bestLambda) \
      + "and numIter = %d, and its RMSE on the test set is %f." % (bestNumIter, testRmse)
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
predictions. `0` is the special user id assigned to you.

<div class="codetabs">
<div data-lang="scala" markdown="1" data-editable="true">
~~~
class MatrixFactorizationModel {
  def predict(userProducts: RDD[(Int, Int)]): RDD[Rating] = {
    // ...
  }
}
~~~
</div>
<div data-lang="python" markdown="1" data-editable="true">
~~~python
class MatrixFactorizationModel(object):
    def predictAll(self, usersProducts):
        # ...
        return RDD(self._java_model.predict(usersProductsJRDD._jrdd),
                   self._context, RatingDeserializer())

~~~
</div>
</div>

After we get all predictions, let us list the top 50 recommendations and see
whether they look good to you.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div data-lang="scala" class="solution" markdown="1">
~~~
    val myRatedMovieIds = myRatings.map(_.product).toSet
    val candidates = sc.parallelize(movies.keys.filter(!myRatedMovieIds.contains(_)).toSeq)
    val recommendations = bestModel.get
      .predict(candidates.map((0, _)))
      .collect()
      .sortBy(- _.rating)
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
<div data-lang="python" markdown="1">
<div data-lang="python" class="solution" markdown="1">
~~~python
    myRatedMovieIds = set([x[1] for x in myRatings])
    candidates = sc.parallelize([m for m in movies if m not in myRatedMovieIds])
    predictions = bestModel.predictAll(candidates.map(lambda x: (0, x))).collect()
    recommendations = sorted(predictions, key=lambda x: x[2], reverse=True)[:50]

    print "Movies recommended for you:"
    for i in xrange(len(recommendations)):
        print ("%2d: %s" % (i + 1, movies[recommendations[i][1]])).encode('ascii', 'ignore')
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
<div data-lang="scala"  class="solution" markdown="1">
~~~
    val meanRating = training.union(validation).map(_.rating).mean
    val baselineRmse = 
      math.sqrt(test.map(x => (meanRating - x.rating) * (meanRating - x.rating)).mean)
    val improvement = (baselineRmse - testRmse) / baselineRmse * 100
    println("The best model improves the baseline by " + "%1.2f".format(improvement) + "%.")
~~~
</div>
</div>

<div data-lang="python" markdown="1">
<div data-lang="python"  class="solution" markdown="1">
~~~python
    meanRating = training.union(validation).map(lambda x: x[2]).mean()
    baselineRmse = sqrt(test.map(lambda x: (meanRating - x[2]) ** 2).reduce(add) / numTest)
    improvement = (baselineRmse - testRmse) / baselineRmse * 100
    print "The best model improves the baseline by %.2f" % (improvement) + "%."
~~~
</div>
</div>
</div>

The output should be similar to

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

## Solution code

In case you want to see your recommendation first or the complete source code,
we put the solution under 

<div class="codetabs">
<div data-lang="scala" markdown="1">
<code>machine-learning/scala/solution</code>
</div>

<div data-lang="python" markdown="1">
<code>machine-learning/python/solution</code>
</div>
</div>
