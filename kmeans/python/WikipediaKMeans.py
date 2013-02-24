"""
KMeans clustering of Wikipedia pages using Spark.
"""
import sys
import numpy as np

from pyspark import SparkContext

def parseVector(line):
    return np.array([float(x) for x in line.split(',')])

# Add any new functions you need here

if __name__ == "__main__":
    master = open("/root/spark-ec2/cluster-url").read().strip()
    masterHostname = open("/root/spark-ec2/masters").read().strip()
    sc = SparkContext(master, "PythonKMeans")
    K = 4
    convergeDist = 1e-5

    lines = sc.textFile(
	"hdfs://" + masterHostname + ":9000/wikistats_featurized")
    data = lines.map(
	lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()

    # Your code goes here
