#!/usr/bin/env python

import sys
import itertools
from math import sqrt
from operator import add

from pyspark import SparkConf, SparkContext
from pyspark.mllib.recommendation import ALS

from Rating import Rating

def parseRating(line):
    fields = line.split("::")
    return long(fields[3]) % 10, Rating(int(fields[0]), int(fields[1]), float(fields[2]))

def parseMovie(line):
    fields = line.split("::")
    return int(fields[0]), fields[1]

def flattenRatings(ratingsRDD):
    return ratingsRDD.map(lambda x: (x.user, x.product, x.rating))

def toRatings(rdd):
    return rdd.map(lambda x: Rating(x[0], x[1], x[2]))

def loadPersonalRatings():
    """
    Load ratings from file.
    """
    ratings = []
    f = open('../userRatings/userRatings.txt', 'r')
    for line in f.readlines():
        ls = line.strip().split(",")
        if len(ls) == 3:
            ratings.append(Rating(0, int(ls[0]), float(ls[2])))
    if len(ratings) == 0:
        raise RuntimeError("No ratings provided.")
    else:
        return ratings

def computeRmse(model, data, n):
    """
    Compute RMSE (Root Mean Squared Error).
    """
    predictions = model.predictAll(data.map(lambda x: (x.user, x.product)))
    predictionsAndRatings = predictions.map(lambda x: ((x[0], x[1]), x[2])) \
      .join(data.map(lambda x: ((x.user, x.product), x.rating))) \
      .values()
    return sqrt(predictionsAndRatings.map(lambda x: (x[0] - x[1]) ** 2).reduce(add) / float(n))

if __name__ == "__main__":
    if (len(sys.argv) != 2):
        print "Usage: /path/to/spark/bin/spark-submit MovieLensALS.py movieLensHomeDir"
        sys.exit(1)
    
    conf = SparkConf()
    conf.setAppName("MovieLensALS")
    conf.set("spark.executor.memory", "2g")
    sc = SparkContext(conf=conf, pyFiles=['Rating.py'])

    movieLensHomeDir = sys.argv[1]

    ratings = sc.textFile(movieLensHomeDir + "/ratings.dat").map(parseRating)

    movies = dict(sc.textFile(movieLensHomeDir + "/movies.dat").map(parseMovie).collect())

    numRatings = ratings.count()
    numUsers = ratings.values().map(lambda r: r.user).distinct().count()
    numMovies = ratings.values().map(lambda r: r.product).distinct().count()

    print "Got %d ratings from %d users on %d movies." % (numRatings, numUsers, numMovies)

    # load ratings

    myRatings = loadPersonalRatings()
    myRatingsRDD = sc.parallelize(myRatings, 1)

    # split ratings into train (60%), validation (20%), and test (20%) based on the 
    # last digit of the timestamp, add myRatings to train, and cache them

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

    # train models and evaluate them on the validation set

    ranks = [8, 12]
    lambdas = [0.1, 10.0]
    numIters = [10, 20]
    bestModel = None
    bestValidationRmse = float("inf")
    bestRank = 0
    bestLambda = -1.0
    bestNumIter = -1

    for rank, lmbda, numIter in itertools.product(ranks, lambdas, numIters):
        model = ALS.train(flattenRatings(training), rank, numIter, lmbda)
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

    meanRating = training.union(validation).map(lambda x: x.rating).mean()
    baselineRmse = sqrt(test.map(lambda x: (meanRating - x.rating) ** 2).reduce(add) / numTest)
    improvement = (baselineRmse - testRmse) / baselineRmse * 100
    print "The best model improves the baseline by %.2f" % (improvement) + "%."

    # make personalized recommendations
    myRatedMovieIds = set([x.product for x in myRatings])
    candidates = sc.parallelize([m for m in movies if m not in myRatedMovieIds])
    predictions = toRatings(bestModel.predictAll(candidates.map(lambda x: (0, x)))).collect()
    recommendations = sorted(predictions, key=lambda x: x.rating, reverse=True)[:50]

    print "Movies recommended for you:"
    for i in xrange(len(recommendations)):
        print ("%2d: %s" % (i, movies[recommendations[i].product])).encode('ascii', 'ignore')

    # clean up
    sc.stop()
