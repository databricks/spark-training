---
layout: global
title: Machine Learning With Spark
prev: data-exploration-using-shark.html
next: where-to-go-from-here---more-resources-and-further-reading.html
---

In this chapter, we will use MLI and Spark to tackle a machine learning problem. To complete the machine learning exercises within the time available using our relatively small EC2 clusters, in this section we will work with a restricted set of the Wikipedia article data from July 2013. This dataset represents a subset of English Wikipedia articles that have categories associated with them.

Our task will be to come up with a model that's capable of taking an piece of text (say, a newspaper article) and identifying what high level class it belongs to - Sports, Arts, Technology, Math, etc. This is a *supervised* learning problem. To build such a model, we'll need to provide the system with *examples* of articles that already have classes associated with them. For this particular exercise, we're going to focus on *binary* classification - that is, telling whether an article belongs to one class or the other.

Before we begin, we'll need to set up a spark console to use MLI - an API providing user-friendly access to a set of data structures and algorithms designed to make performing machine learning tasks easier. MLI uses Spark under the hood, so we'll do our work from the spark shell.

##Setup

*NOTE* Here I'm assuming that spark has been published-local, and that MLI-assembly-1.0.jar has been built!

From a console - register the MLI library with spark, start a spark shell, and load up an "MLContext" - which is similar to a SparkContext. 

<div class ="codetabs">
<div data-lang="scala" markdown="1">
~~~
#Note - this is in your bash prompt.
$ export SPARK_CLASSPATH=/root/MLI/target/MLI-assembly-1.0.jar
$ spark/spark-shell

//Note - everything from here on out happens in the spark shell!
> sc.addJar("/root/MLI/target/MLI-assembly-1.0.jar")
> import mli.interface._
> val mc = new MLContext(sc)
~~~
</div>
</div>

## Command Line Preprocessing and Featurization

To apply most machine learning algorithms, we must first preprocess and featurize our input data.  That is, for each data point, we must generate a vector of numbers describing the salient properties of that data point.  In our case, each data point will consist of a top-level Wikipedia category and a set of "bigrams" - that is, pairs of words - that are present in each article.  We will generate 10000-dimensional feature vectors, with each feature vector entry summarizing the words used in the underlying article. The choice of *10000* here is arbitrary, but can be an important design decision. Too many features and you risk overfitting your model to the training data - too few and you. 

Each record in our dataset consists of a string with the format "`<category> <subcategory> <page_contents>`".  This raw dataset was derived from raw XML dumps of the wikipedia database.  If you are interested in how we mapped subcategories to higher categories and actually read through the XML dumps you can [read about the process we followed](wiki-featurization.html).

### Data loading and featurization.

The first thing we'll want to do is load our data and take a look at it. MLI offers several convenient ways to load data directly into distributed tables. We'll use one of those to load and take a look at the data.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
> val inputTable = mc.loadFile(sys.env("HDFS_URL")+"/enwiki_txt").filter(r => List("ARTS","LIFE") contains r(0).toString).cache()
> val firstFive = inputTable.take(5)
> val taggedInputTable = inputTable.project(Seq(0,2)).map(r => {
    val label = if(r(0) == "ARTS") 1.0 else 0.0
    MLRow(label, r(1))
}).cache()
~~~
</div>
</div>

The first command loads an <code>MLTable</code> - an <code>MLTable</code> is a distributed collection of <code>MLRow</code>, all of which follow the same <code>Schema</code>. This means that every row has the same shape and data types associated with it. These data types - <code>MLValue</code> (String, Int, Double, Empty), are the elements in the table. Rows can be thought of as arrays of values, and can be indexed with single values or sequences of values. You can select a few columns from a table in the same way. <code>MLTable</code> make up the basic inputs for our machine learning problems. We can apply feature extractors to them to prepare data for input to an <code>Algorithm</code> which learns a <code>Model</code>.

The second command takes a few rows from that table for you to inspect. The third command selects only the first and third columns of that table, and then maps the first column to "1" if the article is an "ART" article, and a "0" if it is a "LIFE" article.

To featurize the data we'll use an N-Gram feature extractor that comes bundled with MLI. N-Gram featurization consists of breaking down documents into "n-grams" or pairs, triples, etc. (bigrams, trigrams, 4-grams) of words that exist in a document, once common words (or, *stop words*) and punctuation have been removed. 

For example - the sentence, "Machine learning is really fun!" would consist of the bigrams {"machine_learning", "learning_really", "really_fun"}. Notice that we removed the word "is". 

Let's get this running while you're reading the next section. This job will take about 5 minutes on your cluster, so while that's running we'll do some exercises to understand what's going on under the hood.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
> import mli.feat.NGrams
> val (featurizedData, featurizer) = NGrams.extractNGrams(taggedInputTable, c=1, n=2, k=10000, stopWords = NGrams.stopWords)
~~~
</div>
</div>

Now that you have an understanding of how ngram processing works, let's do a couple of exercises related to it.

    **Exercise:** In a new scala prompt, write a "bigrams" function that mimics what NGrams is doing to a single string. 
    Given a string, convert it to lower case, tokenize it by splitting on word boundaries (spaces), and output a set of all "word1_word2" pairs that occur in sequence in the base string.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
      def bigram(s: String): Set[String] = {
          s.toLowerCase.split(" ").sliding(2).map(_.mkString("_")).toSet
      }
      
      > bigram("How does bigramming work on a test string")
      res2: Set[String] = Set(on_a, a_test, test_string, does_bigramming, bigramming_work, how_does, work_on)
~~~
</div>
</div>
</div>

We then take that set of *items that exist in this document* and project them onto a common space. To decide on this space, we first look at the whole corpus, and rank N-grams according to their frequency. This ranking gives us an explicit order for the N-grams to be projected onto, and we can now create a binary feature vector for each document, where the features indicate the existence of a particular string in a document.

Word frequency calculation is the classic "word count" followed by a sort, which you already saw in the [interactive analysis with Spark](#interactive-analysis) section. 

Once these word frequencies have been computed, projecting the set of N-grams in a document to the feature space is just a map over that set. 

    **Exercise:** In the same scala prompt, write a function that, given the output of your "bigrams" function and an ordered list of bigrams (bigrams ordered by frequency), produces a binary feature vector in the same order as the ordered list, indicating whether or not the set from the document contains that bigram. The output of this function is a "feature vector".

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
      def bigramFeature(s: Set[String], orderedGrams: Seq[String]): Seq[Double] = {
          orderedGrams.map(g => if(s.contains(g)) 1.0 else 0.0)
      }
      
      > bigramFeature(bigram("This is a test string"), List("is_a", "test_string", "flying_porpoise"))
      res5: Seq[Double] = List(1.0, 1.0, 0.0)
~~~
</div>
</div>
</div>

A feature vector of 1's and 0's is kind of boring. We haven't done anything to add more weight to terms that are less frequently used but may be more important when they are used. For example, the N-Gram "scottie_pippen" might be highly indicative of a sports article, but probably only shows up in a small fraction of the dataset. When we do see it in a document, we want to classify the document as Sports with higher probability. 

Let's do some feature engineering, and compute the term frequency-inverse document frequency statistic of our features. That is, we weight terms by the *inverse* of the frequency with which they show up in the raw documents. This weight is simply the reciprocal of the number of times a particular 

    **Bonus Exercise:** Given a collection of identically shaped bigram feature vectors, compute their total document frequency. Hint - MLVector supports the "plus" operation for elementwise addition. Once you have this, compute the TF-IDF statistic on the original vector.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
      def documentFrequency(features: Set[MLVector]): MLVector = {
          features.reduce(_ plus _)
      }
      
      def tfIdf(features: Set[MLVector]): Set[MLVector] = {
          val df = documentFrequency(features)
          features.map(_ over df)
      }
~~~
</div>
</div>
</div>

## Training vs. Testing Sets
*TODO* Add a note about training sets vs. testing sets. Add code to split the training vector into two sets. 


## Support Vector Machines

[Support Vector Machines (SVMs)](http://en.wikipedia.org/wiki/Support_vector_machine) are a popular machine learning algorithm used for classification. MLI and Spark contain an implementation of linear SVMs based on [Stochastic Gradient Descent (SGD)](http://en.wikipedia.org/wiki/Stochastic_gradient_descent), a convex optimization algorithm that often applies to problems that are non-convex. This implementation has tuned to run well under Spark's distributed architecture. 

*TODO* Add note about SVMs here.

## Building a model using SVMs

Now that we have the data loaded and featurized.. The [featurization process](#command-line-preprocessing-and-featurization) created a 10000-dimensional feature vector for each article in our Wikipedia dataset, with each vector entry summarizing the contents of that page. Now let's train a model on those features.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
> import mli.ml.classification._
> val model = SVMAlgorithm.train(featurizedData, SVMParameters(learningRate=0.001, maxIterations=100))
~~~
</div>
</div>

## Model assessment
Now that we have a model built, what can we do with it? First, let's compute its "training error" - that is, how well does it do on the data it was trained on.

*TODO* Add explanation of test set vs. train set.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
//This gives us an MLTable where the actual label is lined up with the predicted label of our model.
val trainVsTest = featurizedData.map(r => MLRow(r(0), model.predict(r(1 until r.length))))

//This will calculate our training error - the proportion of incorrect answers.
val trainError = trainVsTest.filter(r(0) != r(1)).numRows.toDouble/trainVsTest.numRows
~~~
</div>
</div>

What does this mean? It means that if we give the model the same points that it was trained with, it will classify *trainError* percent of them incorrectly.

## Feature importance.
Let's drill into which features are important. Let's sort the features by their weight and take a look at those that are most important and least important.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
//This will match the features to our N-Grams and tell us which ones are most interesting.
val topFeatures = model.features.sortWith(_._2 < _._2).take(10)
val bottomFeatures = model.features.sortWith(_._2 > _._2).take(10)
~~~
</div>
</div>


## Parameter Tuning
We've seen what happens when we run with the magic parameter "learningRate=0.001" - what happens when we vary this parameter? It turns out that we may get better results depending on how we set this parameter. Too high and we may never find an optimal model - too low and we might not get to a good model in the number of iterations required.

    **Exercise:** Train a model for several different values of learningRate - calculate the training error for each one, and report the one that has the best error. (Hint: Try moving in steps of 10x = e.g. learning rate = 0.0001, 0.001, 0.01, ...)

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
val learningRates = List(0.00001, 0.0001, 0.001, 0.01, 0.1, 1.0, 10.0, 100.0, 1000.0)

val models = learningRates.map({lr => 
    SVMAlgorithm(featurizedData, SVMParameters(learningRate=0.001, numIterations=100))
    })
    
val modelErrors = models.map(model => {
    val trainVsTest = featurizedData.map(r => MLRow(r(0), model.predict(r(1 until r.length))))
    trainVsTest.filter(r(0) != r(1)).numRows.toDouble/trainVsTest.numRows
    })
    
val sortedParms = learningRates.zip(modelErrors)

//Best model is the one with lowest error.
val bestModel = //
~~~
</div>
</div>
</div>


## Test the model on new data
Now that we're pretty sure we have a good model, let's see how well it does at actually predicting based on some new text.

Let's create a new TextModel, which expects a *Model* and a *Featurizer* and will be able to predict the class of arbitrary text.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
> val textModel = new TextModel(bestModel, featurizer) //you should still have your featurizer from the initial featurization process!

//TextModels have a *predict* method which takes a string as input. Try it out on this article from Wikipedia: 
> textModel.predict(scala.io.Source.fromURL("http://en.wikipedia.org/wiki/Baroque").mkString)

//What class did the model predict? Was it right? Note that that article has NO category information associated with it. Try on other articles - is it reasonable?
~~~
</div>
</div>
</div>

## Cross-Validation

