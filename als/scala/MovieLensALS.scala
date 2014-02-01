import java.util.Random

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ALS, Rating, MatrixFactorizationModel}

import org.apache.log4j.Logger
import org.apache.log4j.Level

object MovieLensALS {

  def main(args: Array[String]) {
    Logger.getLogger("spark").setLevel(Level.INFO)

    if (args.length != 1) {
      println("Usage: MovieLensALS movieLensHomeDir")
      exit(1)
    }

    val movieLensHomeDir = args(0)

    // set up environment

    val sparkHome = "/root/spark"
    val jarFile = "target/scala-2.10/movielens-als_2.10-0.0.jar"
    val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
    val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim

    val sc = new SparkContext(master, "MovieLensALS", sparkHome, Seq(jarFile))

    // load ratings and movie titles

    val ratings = sc.textFile(movieLensHome + "/ratings.dat")
                 .map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(user, product, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }

    val movies = sc.textFile(movieLensHome + "/movies.dat")
                   .map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap

    val numRatings = ratings.count
    val numUsers = ratings.map(_._2.user).distinct.count
    val numMovies = ratings.map(_._2.product).distinct.count

    println("Got " + numRatings + " ratings from " 
      + numUsers + " users on " + numMovies + " movies.")

    // sample a subset of most rated movies for rating elicitation

    val mostRatedMovieIds = ratings.map(_._2.product)
                                   .countByValue
                                   .toSeq
                                   .sortBy(-_._2)
                                   .take(50)
                                   .map(_._1)
    val random = new Random(0)
    val selectedMovies = mostRatedMovieIds.filter(x => random.nextDouble() < 0.2)
                                          .map(x => (x, movies(x)))
                                          .toSeq

    // elicitate ratings
    val myRatings = elicitateRatings(selectedMovies)
    val myRatingsRDD = sc.parallelize(myRatings)

    // split ratings into train (60%), validation (20%), and test (20%) based on the 
    // last digit of the timestamp, add myRatings to train, and cache them
    val training = ratings.filter(x => x._1 < 6).values.union(myRatingsRDD).persist
    val validation = ratings.filter(x => x._1 >= 6 && x._1 < 8).values.persist
    val test = ratings.filter(x => x._1 >= 8).values.persist

    val numTraining = training.count
    val numValidation = validation.count
    val numTest = test.count

    println("Training: " + numTraining + ", validation: " + numValidation + ", test: " + numTest)

    // train models and evaluate them on the validation set
    val ranks = List(4, 8, 12)
    val lambdas = List(0.1, 1.0, 10.0)
    val numIter = 25
    var bestModel: Option[MatrixFactorizationModel] = None
    var bestValidationRmse = Double.MaxValue
    var bestRank = 0
    var bestLambda = -1.0
    for(rank <- ranks; lambda <- lambdas) {
      val model = ALS.train(training, rank, numIter, lambda)
      val validationRmse = computeRmse(model, validation, numValidation)
      if(validationRmse < bestValidationRmse) {
        bestModel = Some(model)
        bestValidationRmse = validationRmse
        bestRank = rank
        bestLambda = lambda
      }
    }

    val testRmse = computeRmse(bestModel.get, test, numTest)

    println("The best model was trained using rank " + bestRank + " and lambda " + bestLambda
      + ", and its RMSE on test is " + testRmse + ".")

    // create a naive baseline and compute its RMSE
    val meanRating = training.union(validation).map(_.rating).mean
    val baselineRmse = math.sqrt(test.map(x => (meanRating - x.rating) * (meanRating - x.rating))
                                     .reduce(_ + _) / numTest)
    // calculate improvement
    val improvement = (baselineRmse - testRmse) / baselineRmse * 100
    println("The best model improves the baseline by " + "%1.2f".format(improvement) + "%.")    

    // make personalized recommendations
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

    sc.stop();
    System.exit(0)
  }

  /** Compute RMSE (Root Mean Sqaured Error). */
  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], n: Long) = {
    val predictions: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
    val predictionsAndRatings = predictions.map(x => ((x.user, x.product), x.rating))
                                           .join(data.map(x => ((x.user, x.product), x.rating)))
                                           .values
    math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).reduce(_ + _) / n)
  }
  
  /** Elicitate ratings from command-line. */
  def elicitateRatings(movies: Seq[(Int, String)]) = {
    val prompt = "Please rate the following movie (1-5 (best), or 0 if not seen):"
    println(prompt)
    val ratings = movies.flatMap { x =>
      var rating: Option[Rating] = None
      var valid = false
      while (!valid) {
        print(x._2 + ": ")
        try {
          val r = Console.readInt
          if (r < 0 || r > 5) {
            println(prompt)
          } else {
            valid = true
            if (r > 0) {
              rating = Some(Rating(0, x._1, r))
            }
          }
        } catch {
          case e: Exception => println(prompt)
        }
      }
      rating match {
        case Some(r) => Iterator(r)
        case None => Iterator.empty
      }
    }
    if(ratings.isEmpty) {
      error("No rating provided!")
    } else {
      ratings
    }
  }
}
