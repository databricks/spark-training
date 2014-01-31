---
layout: global
title: Graph Analytics With GraphX
categories: [module]
navigation:
  weight: 75
  show: true
---

{:toc}

<p style="text-align: center;">
  <img src="img/graphx_logo.png"
       title="GraphX Logo"
       alt="GraphX"
       width="65%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

<!-- In this chapter we use GraphX to analyze Wikipedia data and implement graph algorithms in Spark. As with other exercises we will work with a subset of the Wikipedia traffic statistics data from May 5-7, 2009. In particular, this dataset only includes a subset of all Wikipedia articles. -->

GraphX is the new (alpha) Spark API for graphs (e.g., Web-Graphs and Social Networks) and graph-parallel computation (e.g., PageRank and Collaborative Filtering).
At a high-level, GraphX extends the Spark RDD abstraction by introducing the [Resilient Distributed Property Graph](#property_graph): a directed multigraph with properties attached to each vertex and edge.
To support graph computation, GraphX exposes a set of fundamental operators (e.g., [subgraph](#structural_operators), [joinVertices](#join_operators), and [mapReduceTriplets](#mrTriplets)) as well as an optimized variant of the [Pregel](#pregel) API.
In addition, GraphX includes a growing collection of graph [algorithms](#graph_algorithms) and
[builders](#graph_builders) to simplify graph analytics tasks.

In this chapter we use GraphX to analyze Wikipedia data and implement graph algorithms in Spark.
The GraphX API is currently only available in Scala but we plan to provide Java and Python bindings in the future.

## Background on Graph-Parallel Computation (Optional)

If you want to get started coding right away, you can skip this part or come back later.

From social networks to language modeling, the growing scale and importance of graph data has driven the development of numerous new *graph-parallel* systems (e.g., [Giraph](http://giraph.apache.org) and [GraphLab](http://graphlab.org)).
By restricting the types of computation that can be expressed and introducing new techniques to partition and distribute graphs, these systems can efficiently execute sophisticated graph algorithms orders of magnitude faster than more general *data-parallel* systems.

<p style="text-align: center;">
  <img src="img/data_parallel_vs_graph_parallel.png"
       title="Data-Parallel vs. Graph-Parallel"
       alt="Data-Parallel vs. Graph-Parallel"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

The same restrictions that enable graph-parallel systems to achieve substantial performance gains also limit their ability to express many of the important stages in a typical graph-analytics pipeline.
Moreover while graph-parallel systems are optimized for iterative diffusion algorithms like PageRank they are not well suited to more basic tasks like constructing the graph, modifying its structure, or expressing computation that spans multiple graphs.

These tasks typically require data-movement outside of the graph topology and are often more naturally expressed as operations on tables in more traditional data-parallel systems like Map-Reduce.
Furthermore, how we look at data depends on our objectives and the same raw data may require many different table and graph views throughout the analysis process:

<p style="text-align: center;">
  <img src="img/tables_and_graphs.png"
       title="Tables and Graphs"
       alt="Tables and Graphs"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

Moreover, it is often desirable to be able to move between table and graph views of the same physical data and to leverage the properties of each view to easily and efficiently express
computation.
However, existing graph analytics pipelines compose graph-parallel and data-parallel systems, leading to extensive data movement and duplication and a complicated programming
model.

<p style="text-align: center;">
  <img src="img/graph_analytics_pipeline.png"
       title="Graph Analytics Pipeline"
       alt="Graph Analytics Pipeline"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

The goal of the GraphX project is to unify graph-parallel and data-parallel computation in one system with a single composable API.
The GraphX API enables users to view data both as graphs and as collections (i.e., RDDs) without data movement or duplication. By incorporating recent advances in graph-parallel systems, GraphX is able to optimize the execution of graph operations.

Prior to the release of GraphX, graph computation in Spark was expressed using Bagel, an implementation of Pregel.
GraphX improves upon Bagel by exposing a richer property graph API, a more streamlined version of the Pregel abstraction, and system optimizations to improve performance and reduce memory overhead.
While we plan to eventually deprecate Bagel, we will continue to support the [Bagel API](api/bagel/index.html#org.apache.spark.bagel.package) and [Bagel programming guide](bagel-programming-guide.html).
However, we encourage Bagel users to explore the new GraphX API and comment on issues that may complicate the transition from Bagel.


## Introduction to the GraphX API

To get started you first need to import GraphX.  Run the following in your Spark shell:

<div class="codetabs">
<div data-lang="scala">
{% highlight scala %}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
{% endhighlight %}
</div>
</div>

Great! You have now "installed" GraphX.

### The Property Graph
<a name="property_graph"></a>

[PropertyGraph]: api/graphx/index.html#org.apache.spark.graphx.Graph

The [property graph](PropertyGraph) is a directed multigraph with properties attached to each vertex and edge.
A directed multigraph is a directed graph with potentially multiple parallel edges sharing the same source and destination vertex.
The ability to support parallel edges simplifies modeling scenarios where multiple relationships (e.g., co-worker and friend) can appear between the same vertices.
Each vertex is keyed by a *unique* 64-bit long identifier (`VertexID`).
Similarly, edges have corresponding source and destination vertex identifiers.
The properties are stored as Scala/Java objects with each edge and vertex in the graph.

Throughout the first half of this tutorial we will use the following toy property graph.
While this is hardly <i>big data</i>, it provides an opportunity to learn about the graph data model and the GraphX API.  In this example we have a small social network with users and their ages modeled as vertices and likes modeled as directed edges.  In this fictional scenario users can like other users multiple times.


<p style="text-align: center;">
  <img src="img/social_graph.png"
       title="Toy Social Network"
       alt="Toy Social Network"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

We begin by creating the property graph from arrays of vertices and edges.
Later we will demonstrate how to load real data.
Paste the following code into the spark shell.

<div class="codetabs">
<div data-lang="scala">
{% highlight scala %}
val vertexArray = Array(
  (1L, ("Alice", 28)),
  (2L, ("Bob", 27)),
  (3L, ("Charlie", 65)),
  (4L, ("David", 42)),
  (5L, ("Ed", 55)),
  (6L, ("Fran", 50))
  )
val edgeArray = Array(
  Edge(2L, 1L, 7),
  Edge(2L, 4L, 2),
  Edge(3L, 2L, 4),
  Edge(3L, 6L, 3),
  Edge(4L, 1L, 1),
  Edge(5L, 2L, 2),
  Edge(5L, 3L, 8),
  Edge(5L, 6L, 3)
  )
{% endhighlight %}
</div>
</div>

In the above example we make use of the [`Edge`][Edge] class. Edges have a `srcId` and a
`dstId` corresponding to the source and destination vertex identifiers. In addition, the `Edge`
class has an `attr` member which stores the edge property (in this case the number of likes).

[Edge]: api/graphx/index.html#org.apache.spark.graphx.Edge

Using `sc.parallelize` (introduced in the Spark tutorial) construct the following RDDs from `vertexArray` and `edgeArray`

<div class="codetabs">
<div data-lang="scala" markdown="1">
{% highlight scala %}
val vertexRDD: RDD[(Long, (String, Int))] = // Implement
val edgeRDD: RDD[Edge[Int]] = // Implement
{% endhighlight %}
</div>
</div>

In case you get stuck (or skipped the Spark tutorial) here is the solution.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
{% highlight scala %}
val vertexRDD: RDD[(Long, (String, Int))] = sc.parallelize(vertexArray)
val edgeRDD: RDD[Edge[Int]] = sc.parallelize(edgeArray)
{% endhighlight %}
</div>
</div>
</div>

Now we are ready to build a property graph.  The basic property graph constructor takes an RDD of vertices (with type `RDD[(VertexId, V)]`) and an RDD of edges (with type `RDD[Edge[E]]`) and builds a graph (with type `Graph[V, E]`).  Try the following:

<div class="codetabs">
<div data-lang="scala" markdown="1">
{% highlight scala %}
val graph: Graph[(String, Int), Int] = Graph(vertexRDD, edgeRDD)
{% endhighlight %}
</div>
</div>

The vertex property for this graph is a tuple `(String, Int)` corresponding to the *User Name* and *Age* and the edge property is just an `Int` corresponding to the number of *Likes* in our hypothetical social network.

There are numerous ways to construct a property graph from raw files, RDDs, and even synthetic
generators.
Like RDDs, property graphs are immutable, distributed, and fault-tolerant.
Changes to the values or structure of the graph are accomplished by producing a new graph with the desired changes.
Note that substantial parts of the original graph (i.e. unaffected structure, attributes, and indices) are reused in the new graph.
The graph is partitioned across the workers using vertex-partitioning heuristics.
As with RDDs, each partition of the graph can be recreated on a different machine in the event of a failure.

### Graph Views

In many cases we will want to extract the vertex and edge RDD views of a graph (e.g., when aggregating or saving the result of calculation).
As a consequence, the graph class contains members (`graph.vertices` and `graph.edges`) to access the vertices and edges of the graph.
While these members extend `RDD[(VertexId, V)]` and `RDD[Edge[E]]` they are actually backed by optimized representations that leverage the internal GraphX representation of graph data.

Use `graph.vertices` to display the names of the users that are at least `30` years old.  The output should contain (in addition to lots of log messages):

<pre class="prettyprint lang-bsh">
David is 42
Fran is 50
Ed is 55
Charlie is 65
</pre>

Here is a hint:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
{% highlight scala %}
graph.vertices.filter { /** Implement */ }.collect.foreach { /** implement */ }
{% endhighlight %}
</div>
</div>
</div>


Here are a few solutions:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
{% highlight scala %}
// Solution 1
graph.vertices.filter { case (id, (name, age)) => age > 30 }.collect.foreach {
  case (id, (name, age)) => println(s"$name is $age")
}

// Solution 2
graph.vertices.filter(v => v._2._2 > 30).collect.foreach(v => println(s"${v._2._1} is ${v._2._2}"))

// Solution 3
for ((id,(name,age)) <- graph.vertices.filter { case (id,(name,age)) => age > 30 }.collect) {
  println(s"$name is $age")
}
{% endhighlight %}
</div>
</div>
</div>

In addition to the vertex and edge views of the property graph, GraphX also exposes a triplet view.
The triplet view logically joins the vertex and edge properties yielding an `RDD[EdgeTriplet[VD, ED]]` containing instances of the [`EdgeTriplet`][EdgeTriplet] class. This *join* can be expressed in the following SQL expression:

[EdgeTriplet]: api/graphx/index.html#org.apache.spark.graphx.EdgeTriplet

{% highlight sql %}
SELECT src.id, dst.id, src.attr, e.attr, dst.attr
FROM edges AS e LEFT JOIN vertices AS src JOIN vertices AS dst
ON e.srcId = src.Id AND e.dstId = dst.Id
{% endhighlight %}

or graphically as:

<p style="text-align: center;">
  <img src="img/triplet.png"
       title="Edge Triplet"
       alt="Edge Triplet"
       width="65%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

The [`EdgeTriplet`][EdgeTriplet] class extends the [`Edge`][Edge] class by adding the `srcAttr` and `dstAttr` members which contain the source and destination properties respectively.


Use the `graph.triplets` view to display who likes who.  The output should look like:

<pre class="prettyprint lang-bsh">
Bob likes Alice
Bob likes David
Charlie likes Bob
Charlie likes Fran
David likes Alice
Ed likes Bob
Ed likes Charlie
Ed likes Fran
</pre>

Here is a partial solution:

<div class="codetabs">
<div data-lang="scala" markdown="1">
{% highlight scala %}
for (triplet <- graph.triplets) {
 /**
   * Triplet has the following Fields:
   *   triplet.srcAttr: (String, Int) // triplet.srcAttr._1 is the name
   *   triplet.dstAttr: (String, Int)
   *   triplet.attr: Int
   *   triplet.srcId: VertexId
   *   triplet.dstId: VertexId
   */
}
{% endhighlight %}
</div>
</div>

Here is the solution:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
{% highlight scala %}
for (triplet <- graph.triplets) {
  println( s"${triplet.srcAttr._1} likes ${triplet.dstAttr._1}")
}
{% endhighlight %}
</div>
</div>
</div>

If someone likes someone else more than 5 times than that relationship is getting pretty serious.
For extra credit, find the lovers.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
{% highlight scala %}
for (triplet <- graph.triplets.filter(t => t.attr > 5)) {
  println( s"${triplet.srcAttr._1} loves ${triplet.dstAttr._1}")
}
{% endhighlight %}
</div>
</div>
</div>

## Graph Operators

Just as RDDs have basic operations like `count`, `map`, `filter`, and `reduceByKey`, property graphs also have a collection of basic operations.
The following is a list of some of the many functions exposed by the Graph API.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
/** Summary of the functionality in the property graph */
class Graph[VD, ED] {
  // Information about the Graph
  val numEdges: Long
  val numVertices: Long
  val inDegrees: VertexRDD[Int]
  val outDegrees: VertexRDD[Int]
  val degrees: VertexRDD[Int]

  // Views of the graph as collections
  val vertices: VertexRDD[VD]
  val edges: EdgeRDD[ED]
  val triplets: RDD[EdgeTriplet[VD, ED]]

  // Change the partitioning heuristic
  def partitionBy(partitionStrategy: PartitionStrategy): Graph[VD, ED]

  // Transform vertex and edge attributes
  def mapVertices[VD2](map: (VertexID, VD) => VD2): Graph[VD2, ED]
  def mapEdges[ED2](map: Edge[ED] => ED2): Graph[VD, ED2]
  def mapEdges[ED2](map: (PartitionID, Iterator[Edge[ED]]) => Iterator[ED2]): Graph[VD, ED2]
  def mapTriplets[ED2](map: EdgeTriplet[VD, ED] => ED2): Graph[VD, ED2]

  // Modify the graph structure
  def reverse: Graph[VD, ED]
  def subgraph(
      epred: EdgeTriplet[VD,ED] => Boolean = (x => true),
      vpred: (VertexID, VD) => Boolean = ((v, d) => true))
    : Graph[VD, ED]
  def groupEdges(merge: (ED, ED) => ED): Graph[VD, ED]

  // Join RDDs with the graph
  def joinVertices[U](table: RDD[(VertexID, U)])(mapFunc: (VertexID, VD, U) => VD): Graph[VD, ED]
  def outerJoinVertices[U, VD2](other: RDD[(VertexID, U)])
      (mapFunc: (VertexID, VD, Option[U]) => VD2)
    : Graph[VD2, ED]

  // Aggregate information about adjacent triplets
  def collectNeighbors(edgeDirection: EdgeDirection): VertexRDD[Array[(VertexID, VD)]]
  def mapReduceTriplets[A: ClassTag](
      mapFunc: EdgeTriplet[VD, ED] => Iterator[(VertexID, A)],
      reduceFunc: (A, A) => A,
      activeSetOpt: Option[(VertexRDD[_], EdgeDirection)] = None)
    : VertexRDD[A]

  // Iterative graph-parallel computation
  def pregel[A](initialMsg: A, maxIterations: Int, activeDirection: EdgeDirection)(
      vprog: (VertexID, VD, A) => VD,
      sendMsg: EdgeTriplet[VD, ED] => Iterator[(VertexID,A)],
      mergeMsg: (A, A) => A)
    : Graph[VD, ED]

  // Basic graph algorithms
  def pageRank(tol: Double, resetProb: Double = 0.15): Graph[Double, Double]
  def connectedComponents(): Graph[VertexID, ED]
  def triangleCount(): Graph[Int, ED]
  def stronglyConnectedComponents(numIter: Int): Graph[VertexID, ED]
}
~~~
</div>
</div>


These functions are split between [`Graph`][Graph] and [`GraphOps`][GraphOps].
However, thanks to the "magic" of Scala implicits the operators in `GraphOps` are automatically available as members of `Graph`.

For example, we can compute the in-degree of each vertex (defined in `GraphOps`) by the following:

[Graph]: api/graphx/index.html#org.apache.spark.graphx.Graph
[GraphOps]: api/graphx/index.html#org.apache.spark.graphx.GraphOps

<div class="codetabs">
<div data-lang="scala" markdown="1">
{% highlight scala %}
val inDegrees: VertexRDD[Int] = graph.inDegrees
{% endhighlight %}
</div>
</div>

In the above example the `graph.inDegrees` operators returned a `VertexRDD[Int]` (recall that this behaves like `RDD[(VertexId, Int)]`).  What if we wanted to incorporate the in and out degree of each vertex into the vertex property?  To do this we will use a set of common graph operators.

Paste the following code into the spark shell:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
// Define a class to more clearly model the user property
case class User(name: String, age: Int, inDeg: Int, outDeg: Int)

// Transform the
val userGraph = graph.mapVertices{ case (id, (name, age)) => User(name, age, 0, 0) }

// Fill in the degree information
val degreeGraph = userGraph.outerJoinVertices(userGraph.inDegrees) {
  case (id, u, inDegOpt) => User(u.name, u.age, inDegOpt.getOrElse(0), u.outDeg)
}.outerJoinVertices(graph.outDegrees) {
  case (id, u, outDegOpt) => User(u.name, u.age, u.inDeg, outDegOpt.getOrElse(0))
}
~~~
</div>
</div>

Here we use the `outerJoinVertices` method of `Graph` which has the following (confusing) type signature:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
 def outerJoinVertices[U, VD2](other: RDD[(VertexID, U)])
      (mapFunc: (VertexID, VD, Option[U]) => VD2)
    : Graph[VD2, ED]
~~~
</div>
</div>

It takes *two* argument lists.
The first contains an `RDD` of vertex values and the second argument list takes a function from the id, attribute, and Optional matching value in the `RDD` to a new vertex value.
Note that it is possible that the input `RDD` may not contain values for some of the vertices in the graph.
In these cases the `Option` argument is empty and `optOutDeg.getOrElse(0)` returns 0.

Print the names of the users who liked by the same number of people they like.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
degreeGraph.vertices.filter {
  case (id, u) => u.inDeg == u.outDeg
}.collect.foreach(println(_))
~~~
</div>
</div>
</div>

### The Map Reduce Triplets Operator

Using the property graph from Section 2.1, suppose we want to find the oldest follower of each user. The [`mapReduceTriplets`][Graph.mapReduceTriplets] operator allows us to do this. It enables neighborhood aggregation, and its simplified signature is as follows:

[Graph.mapReduceTriplets]: api/graphx/index.html#org.apache.spark.graphx.Graph@mapReduceTriplets[A](mapFunc:org.apache.spark.graphx.EdgeTriplet[VD,ED]=&gt;Iterator[(org.apache.spark.graphx.VertexId,A)],reduceFunc:(A,A)=&gt;A,activeSetOpt:Option[(org.apache.spark.graphx.VertexRDD[_],org.apache.spark.graphx.EdgeDirection)])(implicitevidence$10:scala.reflect.ClassTag[A]):org.apache.spark.graphx.VertexRDD[A]

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
class Graph[VD, ED] {
  def mapReduceTriplets[A](
      map: EdgeTriplet[VD, ED] => Iterator[(VertexId, A)],
      reduce: (A, A) => A): VertexRDD[A]
}
~~~
</div>
</div>

The map function is applied to each edge triplet in the graph, yielding messages destined to the adjacent vertices. The reduce function combines messages destined to the same vertex. The operation results in a `VertexRDD` containing an aggregated message for each vertex.

We can find the oldest follower for each user by sending age messages along each edge and aggregating them with the `max` function:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val graph: Graph[(String, Int), Int] // Constructed from above
val oldestFollowerAge: VertexRDD[Int] = graph.mapReduceTriplets[Int](
  edge => Iterator((edge.dstId, edge.srcAttr._2)),
  (a, b) => max(a, b))

val withNames = graph.vertices.innerJoin(oldestFollowerAge) {
  (id, pair, oldestAge) => (pair._1, oldestAge)
}

withNames.collect.foreach(println(_))
~~~
</div>
</div>

As an exercise, try finding the average follower age for each user instead of the max.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
val graph: Graph[(String, Int), Int] // Constructed from above
val oldestFollowerAge: VertexRDD[Int] = graph.mapReduceTriplets[Int](
  // map function
  edge => Iterator((edge.dstId, (1.0, edge.srcAttr._2))),
  // reduce function
  (a, b) => ((a._1 + b._1), (a._1*a._2 + b._1*b._2)/(a._1+b._1)))

val withNames = graph.vertices.innerJoin(oldestFollowerAge) {
  (id, pair, oldestAge) => (pair._1, oldestAge)
}

withNames.collect.foreach(println(_))
~~~
</div>
</div>
</div>

### Subgraph

Suppose we want to find users in the above graph who are lonely so we can suggest new friends for them. The [subgraph][Graph.subgraph] operator takes vertex and edge predicates and returns the graph containing only the vertices that satisfy the vertex predicate (evaluate to true) and edges that satisfy the edge predicate *and connect vertices that satisfy the vertex predicate*.

We can use the subgraph operator to consider only strong relationships with more than 2 likes. We do this by supplying an edge predicate only:

[Graph.subgraph]: api/graphx/index.html#org.apache.spark.graphx.Graph@subgraph((EdgeTriplet[VD,ED])⇒Boolean,(VertexId,VD)⇒Boolean):Graph[VD,ED]

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val graph: Graph[(String, Int), Int] // Constructed from above
val strongRelationships: Graph[(String, Int), Int] =
  graph.subgraph(epred = (edge => edge.attr > 2))
~~~
</div>
</div>

As an exercise, use this subgraph to find lonely users who have no strong relationships (i.e., have degree 0 in the subgraph).

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
val strongRelationships: Graph[(String, Int), Int] = // from above

val lonely = strongRelationships.degrees.filter {
  case (id, degree) => degree == 0
}

lonely.collect.foreach(println(_))
~~~
</div>
</div>
</div>


<!-- ### TODO: Reverse?
### TODO: MapEdges or MapVertices
 -->

## Constructing an End-to-End Graph Analytics Pipeline on Real Data

Now that we have learned about the individual components of the GraphX API, we are ready to put them together to build a real analytics pipeline.
In this section, we will start with raw Wikipedia text data, use Spark operators to clean the data and extract structure, use GraphX operators to analyze the structure, and then use Spark operators
to examine the output of the graph analysis, all from the Spark shell.

If you don't already have the Spark shell open, start it now and import the `org.apache.spark.graphx` package.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import org.apache.spark.graphx._
import org.apache.spark.graphx.RDD
~~~
</div>
</div>

If you are using a cluster provided by the AMPLab for this tutorial, you already have a dataset that contains
all of the English Wikipedia articles in HDFS on your cluster. If you are following along at
home: TODO (what should they do???).

### Load the Wikipedia Articles

The first step in our analytics pipeline is to ingest our raw data into Spark. Load the data (located at
`"/wiki_dump/part*"` in HDFS) into an RDD:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val wiki: RDD[String] = // implement
~~~
<div class="solution" markdown="1">
~~~
// We tell Spark to cache the result in memory so we won't have to
// repeat the expensive disk IO
val wiki: RDD[String] = sc.textFile("/wiki_dump/part*").cache
~~~
</div>
</div>
</div>

### Count the articles

Use the `RDD` count method:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
// Notice that count is not just doing the count but also triggers the wiki RDD's lazy evaluation.
// This means that the read from HDFS is being performed here as well.
wiki.count
// res0: Long = 13449972
~~~
</div>
</div>
</div>

### Look at top article:

Display the contents of the first article:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
wiki.take(1)
// res1: Array[String] = Array(AccessibleComputing #REDIRECT [[Computer accessibility]] {{R from CamelCase}})
~~~
</div>
</div>
</div>

### Clean the Data

The next steps in the pipeline are to clean the data and extract the graph structure.
In this example, we will be extracting the link graph but one could imagine other graphs (e.g., the keyword by document graph and the contributor graph).
From the sample article we printed out, we can already observe some structure to the data.
The first word in the line is the name of the article, and the rest of string is the article contents.
We also can see that this article is a redirect to the "Computer Accessibility" article, and not a full independent article.

Now we are going to use the structure we've already observed to do the first round of data-cleaning.
We define the `Article` class to hold the different parts of the article which we
parse from the raw string, and filter out articles that are malformed or redirects.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
// Define the article class
class Article(val title: String, val body: String)

// Parse the articles
val articles = wiki.map(_.split('\t')).
  // two filters on article format
  filter(line => (line.length > 1 && !(line(1) contains "REDIRECT")).
  // store the results in an object for easier access
  map(line => new Article(line(0).trim, line(1).trim))
~~~
</div>
</div>

### Making a Vertex RDD

At this point, our data is in a clean enough format that we can create our vertex RDD.
Remember we are going to extract the link graph from this dataset, so a natural vertex attribute is the title of the article.
We are also going to define a mapping from article title to vertex ID by hashing the article title.
Finish implementing the following:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
// Hash function to assign an Id to each article
def pageHash(title: String) = title.toLowerCase.replace(" ", "").hashCode

// The vertices with id and article title:
val vertices: RDD[(VertexId, String)] = /** Implement */
~~~
<div class="solution" markdown="1">
~~~
// Hash function to assign an Id to each article
def pageHash(title: String) = title.toLowerCase.replace(" ", "").hashCode
// The vertices with id and article title:
val vertices = articles.map(a => (pageHash(a.title), a.title))
~~~
</div>
</div>
</div>

### Making the Edge RDD

The next step in data-cleaning is to extract our edges to find the structure of the link graph.
We know that the MediaWiki syntax (the markup syntax Wikipedia uses) indicates a link by enclosing the link destination in double square brackets on either side.
So a link looks like "[[Article We Are Linking To]]."
Based on this knowledge, we can write a regular expression to extract all strings that are enclosed on both sides by "[[" and "]]" respectively, and then apply that regular expression to each article's contents, yielding the destination of all links contained in the article.

Copy and paste the following into your Spark shell:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val pattern = "\\[\\[.+?\\]\\]".r
val edges: RDD[Edge[Double]] = articles.flatMap { a =>
  val srcVid = pageHash(a.title)
  pattern.findAllIn(a.body).map { link =>
    val dstVid = pageHash(link.replace("[[", "").replace("]]", ""))
    Edge(srcVid, dstVid, 1.0)
  }
}
~~~
</div>
</div>

This code extracts all the outbound links on each page and produces an RDD of edges with unit weight.

### Making the Graph

We are finally ready to create our graph.
Note that at this point, we have been using core Spark dataflow operators, working with our data in a table view.
Switching to a graph view of our data is now as simple as calling the Graph constructor with our vertex RDD, our edge RDD, and a default vertex attribute.
The default vertex attribute is used to initialize vertices that are not present in the vertex RDD, but are found link edges (links).
In the Wikipedia data there are often links to nonexistent pages.
<!-- GraphX takes the safe approach by creating new vertices in this situation, rather than let the graph have "dangling edges" or assuming the user's data is perfect.
 -->

In our case, we are going to use a dummy value of "xxxxx" for our default
vertex attribute.
We pick "xxxxx" as there are no Wikipedia articles with that as the title, and so we will be able to use the dummy value as a flag to filter out any artificially created vertices and all edges that point to them at the same time.
These edges and dummy vertices are an artifact of our imperfect, dirty dataset, something that inevitably occurs in real world analytics pipelines.
Note that this is another round of data-cleaning, but one that is done based on properties of the graph, making it much simpler to do with a graph-view of our data.

Complete the following:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val graph = Graph(vertices, edges, "xxxxx").cache
val cleanGraph = graph.subgraph(vpred = /* Implement */)
~~~
<div class="solution" markdown="1">
~~~
val graph = Graph(vertices, edges, "xxxxx").cache
val cleanGraph = graph.subgraph(vpred = {(v, d) => !(d contains "xxxxx")}).cache
~~~
</div>
</div>
</div>

### Running PageRank on Wikipedia

We can now do some actual graph analytics.
For this example, we are going to run [PageRank](http://en.wikipedia.org/wiki/PageRank) to evaluate what the most important pages in the Wikipedia graph are.
[`PageRank`](PageRank) is part of a small but growing library of common graph algorithms already implemented in GraphX.
However, the implementation is simple and straightforward, and just consists of some initialization code, a vertex program and message combiner to pass to Pregel.

[PageRank]: api/graphx/index.html#org.apache.spark.graphx.lib.PageRank

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val prGraph = cleanGraph.staticPageRank(5).cache
~~~
</div>
</div>

`Graph.staticPageRank` returns a graph whose vertex attributes are the PageRank values of each page.
However, this means that while the resulting graph `prGraph` only contains the PageRank of the vertices and no longer contains the original vertex properties including the title.
Luckily, we still have our `cleanGraph` that contains that information.
Here, we can perform a join of the vertices in the `prGraph` that have the information about relative ranks of the vertices with the vertices in the `cleanGraph` that have the information about the mapping from vertex to article title.
This yields a new graph that has combined both pieces of information, storing them both in a tuple
as the new vertex attribute. We can then perform further table-based operators on this new list of vertices,
such as finding the ten most important vertices (those with the highest pageranks) and printing out
their corresponding article titles. Putting this all together, and we get the following set of operations
to find the titles of the ten most important articles.

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
val titleAndPRGraph = cleanGraph.outerJoinVertices(ranksG.vertices)({(v, title, r) => (r.getOrElse(0.0), title)})
titleAndPrGraph.vertices.top(10)(Ordering.by((entry: (VertexId, (Double, String))) => entry._2._1)).foreach(t => println(t._2._2 + ": " + t._2._1))
~~~
</div>
</div>

This brings us to the end of the GraphX chapter of the tutorial. We encourage you to continue playing
with the code and to check out the [Programming Guide](TODO: Link) for further documentation about the system.


Bug reports and feature requests are welcomed.
