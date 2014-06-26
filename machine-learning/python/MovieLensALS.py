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

    # set up environment

    conf = SparkConf()
    conf.setAppName("MovieLensALS")
    conf.set("spark.executor.memory", "2g")
    sc = SparkContext(conf=conf, pyFiles=['Rating.py'])

    # load ratings and movie titles

    movieLensHomeDir = sys.argv[1]

    ratings = sc.textFile(movieLensHomeDir + "/ratings.dat").map(parseRating)

    movies = dict(sc.textFile(movieLensHomeDir + "/movies.dat").map(parseMovie).collect())

    # your code here

    # clean up

    sc.stop()
