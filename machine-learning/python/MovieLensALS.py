import os
import sys
import numpy as np

from pyspark import SparkConf, SparkContext
from Rating import Rating

def parseRatings(line):
    fields = line.split("::")
    return long(fields[3]) % 10, Rating(int(fields[0]), int(fields[1]), float(fields[2]))

def parseMovies(line):
    fields = line.split("::")
    return int(fields[0]), fields[1]

# Add any new functions you need here

if __name__ == "__main__":
    if (len(sys.argv) != 2):
        print "Usage: /root/spark/bin/spark-submit --master " + \
        "`cat ~/spark-ec2/cluster-url` MovieLensALS.py movieLensHomeDir"
        sys.exit(1)
    
    masterHostname = open("/root/spark-ec2/masters").read().strip()
    conf = (SparkConf().setAppName("MovieLensALS").set("spark.executor.memory", "8g"))
    sc = SparkContext(conf = conf)

    movieLensHomeDir = "hdfs://" + masterHostname + ":9000" + sys.argv[1]

    ratings = sc.textFile(movieLensHomeDir + "/ratings.dat").map( \
              lambda line: parseRatings(line))

    movies = dict(sc.textFile(movieLensHomeDir + "/movies.dat").map( \
             lambda line: parseMovies(line)).collect())


    numRatings = ratings.count()
    print "Got [" + numRatings + "] ratings."