Forked from https://github.com/databricks/spark-training/
Training documentation at https://databricks-training.s3.amazonaws.com/movie-recommendation-with-mllib.html

# Requisites
* Spark installed 
* $SPARK_HOME defined
```
export SPARK_HOME=~/software/spark-1.1.1
```

# Getting started
```
git clone git@github.com:jonasanso/spark-training.git
cd spark-training/machine-learning/scala
make build
``` 

# Movie recommendation exercise
Rate some movies with your own preferences
```
make rate
```

Follow trining steps
https://databricks-training.s3.amazonaws.com/movie-recommendation-with-mllib.html

Execute your code
```
make build run
```

# Spark Camp training materials.

This project holds the resources used for Spark Camp and other related training events. 
The contents are as follows:

 * website - the jekyll-based website material hosted for training events
 * data - datasets used in the labs
 * sbt - a custom-configured version of sbt set-up to use a local cache when using a USB
 * simple-app - a toy project to test sbt builds
 * streaming - a streaming application used for labs
 * machine-learning - an ML application used for labs
 * build_usb.py - a build script for making the training usb
