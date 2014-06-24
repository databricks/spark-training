import os
import sys
import random

from pyspark import SparkConf, SparkContext
from Rating import Rating

def parseRatings(line):
    fields = line.split("::")
    return long(fields[3]) % 10, Rating(int(fields[0]), int(fields[1]), float(fields[2]))

def parseMovies(line):
    fields = line.split("::")
    return int(fields[0]), fields[1]

def elicitateRatings(movies):
    prompt = "Please rate the following movie (1-5 (best), or 0 if not seen):"
    print prompt
    ratings = list()
    for movie in movies:
        valid = False
        while not valid:
            r = raw_input()
            if r < 0 or r > 5:
                print prompt
            else:
                valid = True
                if r > 0:
                    ratings.append(Rating(0, movie, r))
    if len(ratings) == 0:
        raise RuntimeError("No rating provided")
    else:
        return ratings


# Add any new functions you need here

if __name__ == "__main__":
    if (len(sys.argv) != 2):
        print "Usage: /root/spark/bin/spark-submit --master " + \
        "`cat ~/spark-ec2/cluster-url` MovieLensALS.py movieLensHomeDir"
        sys.exit(1)
    
    masterHostname = open("/root/spark-ec2/masters").read().strip()
    conf = SparkConf()
    conf.setAppName("MovieLensALS")
    conf.set("spark.executor.memory", "8g")
    sc = SparkContext(conf = conf, pyFiles=['Rating.py'])

    movieLensHomeDir = "hdfs://" + masterHostname + ":9000" + sys.argv[1]

    ratings = sc.textFile(movieLensHomeDir + "/ratings.dat").map( \
              lambda line: parseRatings(line))

    movies = dict(sc.textFile(movieLensHomeDir + "/movies.dat").map( \
             lambda line: parseMovies(line)).collect())


    numRatings = ratings.count()
    numUsers = ratings.map(lambda r: r[1].user).distinct().count()
    numMovies = ratings.map(lambda r: r[1].product).distinct().count()

    print "Got %d ratings from %d users on %d movies"%(numRatings, numUsers, numMovies)

    # sample a subset of most rated movies for rating elicitation

    mostRatedMovieIds = [x[0] for x in sorted(ratings.map(lambda r: r[1].product).countByValue().items(), key=lambda x: - x[1])[:50]]
    random.seed(0)
    selectedMovies = [(x, movies[x]) for x in mostRatedMovieIds if random.random < 0.2]

    # elicitate ratings

    myRatings = elicitateRatings(selectedMovies)
    myRatingsRDD = sc.parallelize(myRatings, 1)

    # split ratings into train (60%), validation (20%), and test (20%) based on the 
    # last digit of the timestamp, add myRatings to train, and cache them

    numPartitions = 20
    training = ratings.filter(lambda x: x[0] < 6).values().union(myRatingsRDD).repartition(numPartitions).persist()
    validation = ratings.filter(lambda x: x[0] >= 6 and x[0] < 8).values().repartition(numPartitions).persist()
    test = ratings.filter(lambda x: x[0] >= 8).values().persist()

    numTraining = training.count()
    numValidation = validation.count()
    numTest = test.count()

    print "Training: %d, validation: %d, test: %d"%(numTraining, numValidation, numTest)



    



