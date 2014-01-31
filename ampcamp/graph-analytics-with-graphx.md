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
At a high-level, GraphX extends the Spark RDD by introducing the [Resilient Distributed Property Graph](#property_graph): a directed multigraph with properties attached to each vertex and edge.
To support graph computation, GraphX exposes a set of fundamental operators (e.g., [subgraph](#structural_operators), [joinVertices](#join_operators), and [mapReduceTriplets](#mrTriplets)) as well as an optimized variant of the [Pregel](#pregel) API.
In addition, GraphX includes a growing collection of graph [algorithms](#graph_algorithms) and
[builders](#graph_builders) to simplify graph analytics tasks.

In this chapter we use GraphX to analyze Wikipedia data and implement graph algorithms in Spark.
The GraphX API is currently only available in Scala but we plan to provide Java and Python bindings in the future.

## Background on Graph-Parallel Computation

From social networks to language modeling, the growing scale and importance of graph data has driven the development of numerous new *graph-parallel* systems (e.g., [Giraph](http://giraph.apache.org) and [GraphLab](http://graphlab.org)).
By restricting the types of computation that can be expressed and introducing new techniques to partition and distribute graphs, these systems can efficiently execute sophisticated graph algorithms orders of magnitude faster than more general *data-parallel* systems.

<p style="text-align: center;">
  <img src="img/data_parallel_vs_graph_parallel.png"
       title="Data-Parallel vs. Graph-Parallel"
       alt="Data-Parallel vs. Graph-Parallel"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

However, the same restrictions that enable these substantial performance gains also make it
difficult to express many of the important stages in a typical graph-analytics pipeline:
constructing the graph, modifying its structure, or expressing computation that spans multiple
graphs.
Furthermore, how we look at data depends on our objectives and the same raw data may have
many different table and graph views.

<p style="text-align: center;">
  <img src="img/tables_and_graphs.png"
       title="Tables and Graphs"
       alt="Tables and Graphs"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

As a consequence, it is often necessary to be able to move between table and graph views of the same physical data and to leverage the properties of each view to easily and efficiently express
computation.
However, existing graph analytics pipelines must compose graph-parallel and data- parallel systems, leading to extensive data movement and duplication and a complicated programming
model.

<p style="text-align: center;">
  <img src="img/graph_analytics_pipeline.png"
       title="Graph Analytics Pipeline"
       alt="Graph Analytics Pipeline"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

The goal of the GraphX project is to unify graph-parallel and data-parallel computation in one system with a single composable API.
The GraphX API enables users to view data both as a graph and as collections (i.e., RDDs) without data movement or duplication. By incorporating recent advances in graph-parallel systems, GraphX is able to optimize the execution of graph operations.

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
// To make some of the examples work we will also need RDD
import org.apache.spark.rdd.RDD
{% endhighlight %}
</div>
</div>


### The Property Graph
<a name="property_graph"></a>

[PropertyGraph]: api/graphx/index.html#org.apache.spark.graphx.Graph

The [property graph](PropertyGraph) is a directed multigraph with properties attached to each vertex and edge.
A directed multigraph is a directed graph with potentially multiple parallel edges sharing the same source and destination vertex.
The ability to support parallel edges simplifies modeling scenarios where multiple relationships (e.g., co-worker and friend) can appear between the same vertices.
Each vertex is keyed by a *unique* 64-bit long identifier (`VertexID`).
Similarly, edges have corresponding source and destination vertex identifiers.
The properties are stored as Scala objects with each edge and vertex in the graph.

In the following example we create the following toy property graph:

<p style="text-align: center;">
  <img src="img/social_graph.png"
       title="Toy Social Network"
       alt="Toy Social Network"
       width="50%" />
  <!-- Images are downsized intentionally to improve quality on retina displays -->
</p>

Lets begin by creating the vertices and edges.  In this toy example we will create the graph from arrays of vertices and edges but later we will demonstrate how to load real data.  Paste the following code into your shell.

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

In the above example we make use of the [`Edge`][Edge] case class. Edges have a `srcId` and a
`dstId` corresponding to the source and destination vertex identifiers. In addition, the `Edge`
class has an `attr` member which stores the edge property (in this case the number of likes).
[Edge]: api/graphx/index.html#org.apache.spark.graphx.Edge

[Edge]: api/graphx/index.html#org.apache.spark.graphx.Edge

Using `sc.parallelize` construct the following RDDs from `vertexArray` and `edgeArray`

<div class="codetabs">
<div data-lang="scala" markdown="1">
{% highlight scala %}
val vertexRDD: RDD[(Long, (String, Int))] = // Implement
val edgeRDD: RDD[Edge[Int]] = // Implement
{% endhighlight %}
</div>
</div>

In case you get stuck here is the solution.

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

The vertex property for this graph is tuple `(String, Int)` corresponding to the `User Name` and `Age` and the edge property is just an `Int` corresponding to the number of likes.

There are numerous ways to construct a property graph from raw files, RDDs, and even synthetic
generators.
Like RDDs, property graphs are immutable, distributed, and fault-tolerant.
Changes to the values or structure of the graph are accomplished by producing a new graph with the desired changes.
Note that substantial parts of the original graph (i.e., unaffected structure, attributes, and indicies) are reused in the new graph.
The graph is partitioned across the workers using vertex-partitioning heuristics.
As with RDDs, each partition of the graph can be recreated on a different machine in the event of a failure.

### Graph Views

In many cases we will to extract the vertex and edge RDD views of a graph (e.g., when aggregating or saving the result of calculation).
As a consequence, the graph class contains members (i.e., `graph.vertices` and `graph.edges`) to access the vertices and edges of the graph.
While these members extend `RDD[(VertexId, V)]` and `RDD[Edge[E]]` they are actually backed by optimized representations that leverage the internal GraphX representation of graph data.

Use `graph.vertices` to display the names of the users that are at least `30` years old.  The output should contain (in addition to lots of log messages):

<pre class="prettyprint lang-bsh">
David is 42
Fran is 50
Ed is 55
Charlie is 65
</pre>

Here are a few solutions:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
{% highlight scala %}
// Solution 1
graph.vertices filter { case (id, (name, age)) => age > 30 } foreach {
  case (id, (name, age)) => println(s"$name is $age")
}

// Solution 2
graph.vertices.filter(v => v._2._2 > 30).foreach(v => println(s"${v._2._1} is ${v._2._2}"))

// Solution 3
for ((id,(name,age)) <- graph.vertices filter { case (id,(name,age)) => age > 30 }) {
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
FROM edges AS e LEFT JOIN vertices AS src, vertices AS dst
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

Need a hint?  Here is a partial solution:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
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
</div>

Here is the full solution:

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

Just as RDDs have basic operations like `map`, `filter`, and `reduceByKey`, property graphs also have a collection of basic operators that take user defined functions and produce new graphs with transformed properties and structure.
The core operators that have optimized implementations are defined in [`Graph`][Graph] and convenient operators that are expressed as a compositions of the core operators are defined in [`GraphOps`][GraphOps].
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

In the above example the `graph.inDegrees` operators returned a `VertexRDD[Int]` (recall that this behaves like `RDD[(VertexId, Int)]`).  What if we wanted to incorporate the in and out degree of each vertex as a vertex property?  To do this we will use a set of common graph operators.  Paste the following code into the spark shell:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
// Fill in the degree information
val degreeGraph = graph.outerJoinVertices(graph.inDegrees) {
  (id, u, inDegOpt) => (u._1, u._2, inDegOpt.getOrElse(0))
}.outerJoinVertices(graph.outDegrees) {
  (id, u, outDegOpt) => (u._1, u._2, u._3, outDegOpt.getOrElse(0))
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

Print the names of the users who like the same number of people who like them.

<div class="codetabs">
<div data-lang="scala" markdown="1">
<div class="solution" markdown="1">
~~~
degreeGraph.vertices.filter {
  case (id, (name, age, inDeg, outDeg)) => inDeg == outDeg
}.collect.foreach(println(_))
~~~
</div>
</div>
</div>

### The Map Reduce Triplets Operator

Using the property graph from Section 2.1, suppose we want to find the oldest follower of each user. The [`mapReduceTriplets`][Graph.mapReduceTriplets] operator allows us to do this. It enables neighborhood aggregation, and its simplified signature is as follows:

[Graph.mapReduceTriplets]: api/graphx/index.html#org.apache.spark.graphx.Graph@mapReduceTriplets[A](mapFunc:org.apache.spark.graphx.EdgeTriplet[VD,ED]=&gt;Iterator[(org.apache.spark.graphx.VertexId,A)],reduceFunc:(A,A)=&gt;A,activeSetOpt:Option[(org.apache.spark.graphx.VertexRDD[_],org.apache.spark.graphx.EdgeDirection)])(implicitevidence$10:scala.reflect.ClassTag[A]):org.apache.spark.graphx.VertexRDD[A]

{% highlight scala %}
class Graph[VD, ED] {
  def mapReduceTriplets[A](
      map: EdgeTriplet[VD, ED] => Iterator[(VertexId, A)],
      reduce: (A, A) => A): VertexRDD[A]
}
{% endhighlight %}

The map function is applied to each edge triplet in the graph, yielding messages destined to the adjacent vertices. The reduce function combines messages destined to the same vertex. The operation results in a `VertexRDD` containing an aggregated message for each vertex.

We can find the oldest follower for each user by sending age messages along each edge and aggregating them with the `max` function:

{% highlight scala %}
val graph: Graph[(String, Int), Int] // Constructed from above
val oldestFollowerAge: VertexRDD[Int] = graph.mapReduceTriplets[Int](
  edge => Iterator((edge.dstId, edge.srcAttr._2)),
  (a, b) => max(a, b))

val withNames = graph.vertices.innerJoin(oldestFollowerAge) {
  (id, pair, oldestAge) => (pair._1, oldestAge)
}

withNames.collect.foreach(println(_))
{% endhighlight %}

As an exercise, try finding the average follower age for each user instead of the max.

