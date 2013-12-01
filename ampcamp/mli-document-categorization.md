---
layout: global
title: Machine Learning With MLI
categories: [module]
navigation:
  weight: 90
  show: false
---

In this chapter, we will use MLI and Spark to tackle a machine learning
problem. Recall that MLI, a component of <a href="http://www.mlbase.org"
target="_blank">MLbase</a>, is an API providing user-friendly access to a set
of data structures and algorithms designed to simplify the execution of machine
learning tasks and the development of new featurization and learning
algorithms.

To complete these machine learning exercises within the available time
using our relatively small EC2 clusters, we will work
with a restricted set of the Wikipedia article data from July 2013. This
dataset represents a subset of English Wikipedia articles that have categories
associated with them.

Our task will be to build a model that's capable of taking a piece of text (say, a newspaper article) and identifying what high-level class it belongs to --- Sports, Arts, Technology, Math, etc. We frame this task as a *supervised* learning problem whereby we train a model by providing the system with *labeled examples*, i.e., articles that already have classes associated with them. For this particular exercise, we're going to focus on *binary* classification --- that is, assigning articles to one of two classes.

Before we begin, we'll need to configure a Spark console to use MLI. MLI uses
Spark under the hood, so we'll do our work from the Spark shell (as reviewed in
a [previous section](introduction-to-the-scala-shell.html)).

##Setup

From a console, we'll register the MLI library with Spark, start a Spark shell,
and create an `MLContext`, which is similar to a `SparkContext`.


<div class ="codetabs">
<div data-lang="scala" markdown="1">
At the bash shell prompt, run

~~~
export ADD_JARS=/root/MLI/target/MLI-assembly-1.0.jar
/root/spark/spark-shell
~~~

In the Spark shell, run

~~~
import mli.interface._
val mc = new MLContext(sc)
~~~

</div>
</div>

From here onwards, we'll run all of our commands in the Spark shell.

## Command Line Preprocessing and Featurization

To apply most machine learning algorithms, we must first preprocess and
featurize our input data.  That is, for each data point, we must generate a
vector of numbers describing the salient properties of that data point.  In our
case, each data point will consist of its top-level Wikipedia category (its
*label*) and a set of *features* in the form of "bigrams" (pairs of words) that
are present in each article.  Specifically, we first pick the top 1,000 bigrams
across the entire corpus of documents, and then for each document we compute
the number of times each of these 1,000 bigrams appears in the document.
Hence, we represent each data point as a vector of 1,000 dimensions (each of
these vectors may be very sparse, i.e., contain lots of zeros).  Note that the
choice of *1,000* here is arbitrary, but can be an important design decision.
Using too many features could potentially lead to overfitting your model to the
training data and also increases the computational burden, while with too few
features there may not be enough signal to discriminate between classes.

Each record in our raw dataset consists of a string in the format "`<category> <subcategory> <page_contents>`".
This dataset was derived from raw XML dumps of the Wikipedia database.
<!--If you are interested in how we mapped subcategories to higher categories and actually read through the XML dumps you can [read about the process we followed](wiki-featurization.html). -->

### Data loading and featurization

First, we'll load our data and take a look at it.
MLI offers several convenient ways to load data directly into distributed tables.
We'll use one of those methods, `mc.loadFile`, to load and inspect our data.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val inputTable = mc.loadFile("/enwiki_txt").filter(r => List("ARTS","LIFE") contains r(0).toString).cache()
val firstFive = inputTable.take(5)
val taggedInputTable = inputTable.project(Seq(0,2)).map(r => {
    val label = if(r(0).toString == "ARTS") 1.0 else -1.0
    MLRow(label, r(1))
}).cache()
~~~
</div>
</div>

The first command loads data into an `MLTable`.
An `MLTable` is a distributed collection of `MLRow`, all of which
follow the same `Schema`. This means that every row has the same
shape and data types associated with it. These data types ---
`MLValue` (String, Int, Double, Empty) --- are the elements in the
table.
Rows can be thought of as arrays of values, and can be indexed with
single values or sequences of values.
You can select a few columns from a table in the same way.
`MLTable` is the common input for all of our machine learning algorithms.
Moreover, we can apply feature extractors to `MLTables` during preprocessing.

The second command takes a few rows from the table for you to inspect.

The third command selects only the first and third columns of that table, and
then maps the first column to "1" if the article is an "ART" article or "-1"
if it is a "LIFE" article.

To featurize the data, we'll use an N-Gram feature extractor that's bundled
with MLI.
N-Gram featurization breaks documents into "N-grams" or subsequences of _n_
adjacent words, once common words (or *stop words*) and punctuation have been
removed.
For example, the sentence "Machine learning is really fun!" consists of
the following bigrams: {"machine_learning", "learning_really", "really_fun"}.
Notice that we removed the stop word "is". 
Finally, we remove data points that don't have at least 5 features and scale the data. Scaling is necessary here because our learning algorithm is sensitive to the scale of its inputs.

The featurization job will take about 5 minutes to run on your cluster, so
let's start it now:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import mli.feat._
// c is the column on which we want to perform N-gram extraction
// n is the N-gram length, e.g., n=2 corresponds to bigrams
// k is the number of top N-grams we want to use (sorted by N-gram frequency)
val (featurizedData, ngfeaturizer) = NGrams.extractNGrams(taggedInputTable, c=1, n=2, k=1000, stopWords = NGrams.stopWords)
val (scaledData, featurizer) = Scale.scale(featurizedData.filter(_.nonZeros.length > 5).cache(), 0, ngfeaturizer)
~~~
</div>
</div>

While we wait for this job to finish, let's do a couple of exercises to get
some more intuition about N-grams and understand what's happening under the
hood.

**Exercise**: Open a new `scala` prompt (locally if you have `scala` installed
on your machine, or otherwise by [logging into your
cluster](logging-into-the-cluster.html) in a new terminal).  In the new `scala`
prompt, write a `bigrams` function that mimics what `NGrams` does for a single
string.  Given a string, convert it to lower case, tokenize it by splitting on
word boundaries (spaces), and output a set of all adjacent "word1_word2" pairs that
occur in the input string.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
def bigram(s: String): Set[String] = {
    s.toLowerCase.split(" ").sliding(2).map(_.mkString("_")).toSet
}

bigram("How does bigramming work on a test string")
// This should output
// res2: Set[String] = Set(on_a, a_test, test_string, does_bigramming, bigramming_work, how_does, work_on)
~~~
</div>
</div>
</div>

Once we compute all of a document's N-grams, we could theoretically represent
the document using these N-grams.  However, the full set of N-grams in
a corpus is typically quite large, and in our Wikipedia prediction task we
choose (largely for computational reasons) to consider a restricted subset of
N-grams to represent each document. To decide on this subset of N-grams,
we first rank each N-gram by its frequency across the entire corpus
of documents. This ranking gives us an explicit ordering for selecting
a restricted subset of top N-grams, and we can now create a binary feature
vector for each document, where the features indicate the presence of these
top N-grams.

Word frequency calculation is the classic "word count" followed by a sort, which you already saw in the [interactive analysis with Spark](data-exploration-using-spark.html) section.

Once these word frequencies have been computed, we can just map over this set to project a document's N-grams into the feature space of top N-grams.

**Exercise:** In the same `scala` prompt, write a `bigramFeature` function
that, given the output of your `bigrams` function and an ordered list of
bigrams (bigrams ordered by frequency), produces a binary feature vector in the
same order as the ordered list, indicating whether or not the set from the
document contains that bigram. The output of this function is a "feature
vector".


<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
def bigramFeature(s: Set[String], orderedGrams: Seq[String]): Seq[Double] = {
    orderedGrams.map(g => if(s.contains(g)) 1.0 else 0.0)
}

bigramFeature(bigram("This is a test string"), List("is_a", "test_string", "flying_porpoise"))
// This should output
// res5: Seq[Double] = List(1.0, 1.0, 0.0)
~~~
</div>
</div>
</div>

By now, hopefully the cluster has completed our featurization job.

Note that the outputs of `NGrams.extractNGrams` are `featurizedData` and
`featurizer`. `featurizedData` is an `MLTable` containing N-gram
representations of each document, using bigrams and only including the
1,000 most frequent bigrams. `featurizer` is a function that takes raw
documents and applies the aforementioned featurization process.  This function
will be used along with a trained classifier to make predictions on new text
documents in a [later section](#test-the-model-on-new-data).


## Training vs. Testing Sets

An important concept in supervised machine learning is *training error* vs. *testing error*. Since we are generally less interested in how our model works on the data it was trained on than how it works on *new data*, we need to create a *test set* (sometimes also called a *holdout set*). MLI provides a function to split up your data randomly into train set vs. test set. By default, it will make the training set 90% of your input data, and the testing set the remaining 10%.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val (trainData, testData) = MLTableUtils.trainTest(scaledData)
~~~
</div>
</div>

We will train our model on the *training set* and evaluate it on the *testing set.*


## Building a model using SVMs

<a href="http://en.wikipedia.org/wiki/Support_vector_machine" target="_blank">Support Vector Machines (SVMs)</a> are a popular machine learning algorithm used for binary classification. The MLI implementation of linear SVMs is based on <a href="http://en.wikipedia.org/wiki/Stochastic_gradient_descent" target="_blank">Stochastic Gradient Descent (SGD)</a>, a robust, highly-scalable optimization algorithm. Our implementation has been tuned to run well under Spark's distributed architecture.

Recall that our [featurization process](#command-line-preprocessing-and-featurization) created a 1,000-dimensional feature vector for each article in our Wikipedia dataset, with each vector entry summarizing the contents of that page in the form of the top 1,000 bigrams appearing in the corpus. Now, let's train a model on those features:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import mli.ml.classification._
val model = SVMAlgorithm.train(trainData, SVMParameters(learningRate=10.0, regParam=1.0, maxIterations=50))
~~~
</div>
</div>

## Model assessment
Now that we've built a model, what can we do with it?

First, let's see how to make predictions using our model.  The following code demonstrates how to generate a prediction from the first data point in our training set.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
// note: take(1) returns a sequence with a single MLRow, and we want this MLRow
val firstDataPoint = trainData.take(1)(0)
model.predict(firstDataPoint.tail)
~~~
</div>
</div>

**Exercise:** Next, create an `MLTable` where the first column is the true label and the second column is the predicted label generated by our trained model. Hint: map each row of `featurizedData` into a two-dimensional `MLRow`, with the first element being the true label (the first column of `featurizedData`) and the second element being the model prediction (generated as shown above).

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
val trainVsPred = trainData.map(r => MLRow(r(0), model.predict(r.tail)))
~~~
</div>
</div>
</div>

Now, let's compute the model's "training error" --- that is, how well it performs on the data it was trained on.
We'll calculate the training error by computing the fraction of incorrect predictions.
To do this, we test for equality between the two elements of each MLRow, and count the number of inequalities (mispredictions).

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val trainError = trainVsPred.filter(r => r(0) != r(1)).numRows.toDouble/trainData.numRows
~~~
</div>
</div>

What does this mean? It means that if we give the model the same points that it was trained with, it will classify `trainError` percent of them incorrectly.

Define the following function at your scala prompt --- it will be used to evaluate models later:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
def evalModel(model: SVMModel, testData: MLTable) = {
    val trainData = model.trainingData
    val trainVsPred = trainData.map(r => MLRow(r(0), model.predict(r.tail)))
    val trainErr = trainVsPred.filter(r => r(0).toNumber != r(1).toNumber).numRows.toDouble / trainData.numRows
    val testVsPred = testData.map(r => MLRow(r(0), model.predict(r.tail)))
    val testErr = testVsPred.filter(r => r(0).toNumber != r(1).toNumber).numRows.toDouble / testData.numRows
    (trainErr, testErr)
}
~~~
</div>
</div>

## Feature importance
Next, let's drill into our model to figure out which features are important.
Let's sort the features by their weight and look at the most and least important features:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
// This will match the features to our N-Grams and tell us which ones are most interesting.
val topFeatures = model.features.sortWith(_._2 < _._2).take(10)
val bottomFeatures = model.features.sortWith(_._2 > _._2).take(10)
~~~
</div>
</div>


## Parameter Tuning
We've seen what happens when we run with the magic parameter
`learningRate=10.0`.  What happens when we vary this parameter? It turns out
that we may get better results depending on how we set this parameter. If it's
too high, we may never find an optimal model; if it's too low, we may not build
a good model in the required number of iterations.

**Exercise:** Train a model for several different values of `learningRate`.  Calculate the training error for each model and report the `learningRate` that minimizes the error. (Hint: Try moving in steps of 10x = e.g. `learningRate` = 0.0001, 0.001, 0.01, ...)

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
val learningRates = List(0.00001, 0.0001, 0.001, 0.01, 0.1, 1.0, 10.0, 100.0, 1000.0)

val models = learningRates.map({lr =>
    SVMAlgorithm.train(trainData, SVMParameters(learningRate=lr, regParam=1.0, maxIterations=50))
    })

val modelErrors = models.map(model => evalModel(model, testData))

val sortedParams = learningRates.zip(modelErrors)

//Best model is the one with lowest test error.
val bestModel = models(modelErrors.map(_._2).zipWithIndex.min._2)
~~~
</div>
</div>
</div>

If we look at `sortedParams`, we can see that the models are very sensitive to learning rate and indeed need a learning rate of "1.0" for this number of iterations. To evaluate the performance of `bestModel` we can simply run the following:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
evalModel(bestModel, testData)
~~~
</div>
</div>

This indicates that this model has a 25.5% training error, and a 24.7% test error --- usually test error is worse than training error, but in this case the test error is a little bit better.


## Test the model on new data
Now that we're pretty sure we've built a good model, let's see how well it classifies new text that it wasn't trained on.

Let's create a new `TextModel`, which expects a model and a featurizer and uses them to classify arbitrary text.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
// You should still have your featurizer from the initial featurization process!
import mli.ml._
val textModel = new TextModel(bestModel, (s: MLString) => featurizer(MLRow(1.0, s)).tail)

// TextModels have a `predict` method which takes a string as input. Try it out on this article from Wikipedia:
textModel.predict(MLString(Some(scala.io.Source.fromURL("http://en.wikipedia.org/wiki/Got_Live_If_You_Want_It!_(album)").mkString)))

textModel.predict(MLString(Some(scala.io.Source.fromURL("http://en.wikipedia.org/wiki/Death").mkString)))
~~~
</div>
</div>

What classes did the model predict? Was it right?

**Bonus Exercise**: Try this on other articles --- are the predictions
reasonable?

## TF-IDF Features

In this section, we'll step back and revisit the task of featurization
in order to illustrate how the MLI provides an easy-to-use platform for
developing new featurization algorithms.
So far, we've focused on simple counting-based features, but feature vector of
1's and 0's is kind of boring.
We haven't done anything to add more weight to terms that are less frequently
used but may be more important when they are used. For example, the N-Gram
"lebron_james" might be highly indicative of a sports article, but probably
only shows up in a small fraction of the dataset. When we do see it in a
document, we want to classify the document as "Sports" with higher probability.

Let's do some feature engineering, and compute the <a
href="http://en.wikipedia.org/wiki/Tf%E2%80%93idf">term frequency-inverse
document frequency</a> statistic of our features. That is, we weight terms by the
*inverse* of the frequency with which they appear in the entire corpus,
thus downweighting commonly-appearing terms and promoting infrequent ones.

**Exercise:** Given a collection of identically-shaped bigram feature
vectors, compute their total document frequency. (Hint: `MLVector` supports the
"plus" operation for elementwise addition. Once you have this, compute the
TF-IDF statistic on the original vector.)

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


**Bonus Exercise:** Using the TF-IDF features, repeat the previous steps (model training,
training data evaluation, feature importance, parameter tuning, and evaluating on test data).
Do you see any improvement using these new features?

