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

# Graph Operators

Just as RDDs have basic operations like `map`, `filter`, and `reduceByKey`, property graphs also
have a collection of basic operators that take user defined functions and produce new graphs with
transformed properties and structure.  The core operators that have optimized implementations are
defined in [`Graph`][Graph] and convenient operators that are expressed as a compositions of the
core operators are defined in [`GraphOps`][GraphOps].  However, thanks to Scala implicits the
operators in `GraphOps` are automatically available as members of `Graph`.  For example, we can
compute the in-degree of each vertex (defined in `GraphOps`) by the following:

[Graph]: api/graphx/index.html#org.apache.spark.graphx.Graph
[GraphOps]: api/graphx/index.html#org.apache.spark.graphx.GraphOps

{% highlight scala %}
val graph: Graph[(String, String), String]
// Use the implicit GraphOps.inDegrees operator
val inDegrees: VertexRDD[Int] = graph.inDegrees
{% endhighlight %}

The reason for differentiating between core graph operations and [`GraphOps`][GraphOps] is to be
able to support different graph representations in the future.  Each graph representation must
provide implementations of the core operations and reuse many of the useful operations defined in
[`GraphOps`][GraphOps].

### Summary List of Operators
The following is a quick summary of the functionality defined in both [`Graph`][Graph] and
[`GraphOps`][GraphOps] but presented as members of Graph for simplicity. Note that some function
signatures have been simplified (e.g., default arguments and type constraints removed) and some more
advanced functionality has been removed so please consult the API docs for the official list of
operations.

{% highlight scala %}
/** Summary of the functionality in the property graph */
class Graph[VD, ED] {
  // Information about the Graph ===================================================================
  val numEdges: Long
  val numVertices: Long
  val inDegrees: VertexRDD[Int]
  val outDegrees: VertexRDD[Int]
  val degrees: VertexRDD[Int]
  // Views of the graph as collections =============================================================
  val vertices: VertexRDD[VD]
  val edges: EdgeRDD[ED]
  val triplets: RDD[EdgeTriplet[VD, ED]]
  // Functions for caching graphs ==================================================================
  def persist(newLevel: StorageLevel = StorageLevel.MEMORY_ONLY): Graph[VD, ED]
  def cache(): Graph[VD, ED]
  def unpersistVertices(blocking: Boolean = true): Graph[VD, ED]
  // Change the partitioning heuristic  ============================================================
  def partitionBy(partitionStrategy: PartitionStrategy): Graph[VD, ED]
  // Transform vertex and edge attributes ==========================================================
  def mapVertices[VD2](map: (VertexID, VD) => VD2): Graph[VD2, ED]
  def mapEdges[ED2](map: Edge[ED] => ED2): Graph[VD, ED2]
  def mapEdges[ED2](map: (PartitionID, Iterator[Edge[ED]]) => Iterator[ED2]): Graph[VD, ED2]
  def mapTriplets[ED2](map: EdgeTriplet[VD, ED] => ED2): Graph[VD, ED2]
  def mapTriplets[ED2](map: (PartitionID, Iterator[EdgeTriplet[VD, ED]]) => Iterator[ED2])
    : Graph[VD, ED2]
  // Modify the graph structure ====================================================================
  def reverse: Graph[VD, ED]
  def subgraph(
      epred: EdgeTriplet[VD,ED] => Boolean = (x => true),
      vpred: (VertexID, VD) => Boolean = ((v, d) => true))
    : Graph[VD, ED]
  def mask[VD2, ED2](other: Graph[VD2, ED2]): Graph[VD, ED]
  def groupEdges(merge: (ED, ED) => ED): Graph[VD, ED]
  // Join RDDs with the graph ======================================================================
  def joinVertices[U](table: RDD[(VertexID, U)])(mapFunc: (VertexID, VD, U) => VD): Graph[VD, ED]
  def outerJoinVertices[U, VD2](other: RDD[(VertexID, U)])
      (mapFunc: (VertexID, VD, Option[U]) => VD2)
    : Graph[VD2, ED]
  // Aggregate information about adjacent triplets =================================================
  def collectNeighborIds(edgeDirection: EdgeDirection): VertexRDD[Array[VertexID]]
  def collectNeighbors(edgeDirection: EdgeDirection): VertexRDD[Array[(VertexID, VD)]]
  def mapReduceTriplets[A: ClassTag](
      mapFunc: EdgeTriplet[VD, ED] => Iterator[(VertexID, A)],
      reduceFunc: (A, A) => A,
      activeSetOpt: Option[(VertexRDD[_], EdgeDirection)] = None)
    : VertexRDD[A]
  // Iterative graph-parallel computation ==========================================================
  def pregel[A](initialMsg: A, maxIterations: Int, activeDirection: EdgeDirection)(
      vprog: (VertexID, VD, A) => VD,
      sendMsg: EdgeTriplet[VD, ED] => Iterator[(VertexID,A)],
      mergeMsg: (A, A) => A)
    : Graph[VD, ED]
  // Basic graph algorithms ========================================================================
  def pageRank(tol: Double, resetProb: Double = 0.15): Graph[Double, Double]
  def connectedComponents(): Graph[VertexID, ED]
  def triangleCount(): Graph[Int, ED]
  def stronglyConnectedComponents(numIter: Int): Graph[VertexID, ED]
}
{% endhighlight %}


## Property Operators

In direct analogy to the RDD `map` operator, the property
graph contains the following:

{% highlight scala %}
class Graph[VD, ED] {
  def mapVertices[VD2](map: (VertexId, VD) => VD2): Graph[VD2, ED]
  def mapEdges[ED2](map: Edge[ED] => ED2): Graph[VD, ED2]
  def mapTriplets[ED2](map: EdgeTriplet[VD, ED] => ED2): Graph[VD, ED2]
}
{% endhighlight %}

Each of these operators yields a new graph with the vertex or edge properties modified by the user
defined `map` function.

> Note that in all cases the graph structure is unaffected. This is a key feature of these operators
> which allows the resulting graph to reuse the structural indices of the original graph. The
> following snippets are logically equivalent, but the first one does not preserve the structural
> indices and would not benefit from the GraphX system optimizations:
> {% highlight scala %}
val newVertices = graph.vertices.map { case (id, attr) => (id, mapUdf(id, attr)) }
val newGraph = Graph(newVertices, graph.edges)
{% endhighlight %}
> Instead, use [`mapVertices`][Graph.mapVertices] to preserve the indices:
> {% highlight scala %}
val newGraph = graph.mapVertices((id, attr) => mapUdf(id, attr))
{% endhighlight %}

[Graph.mapVertices]: api/graphx/index.html#org.apache.spark.graphx.Graph@mapVertices[VD2]((VertexId,VD)⇒VD2)(ClassTag[VD2]):Graph[VD2,ED]

These operators are often used to initialize the graph for a particular computation or project away
unnecessary properties.  For example, given a graph with the out-degrees as the vertex properties
(we describe how to construct such a graph later), we initialize it for PageRank:

{% highlight scala %}
// Given a graph where the vertex property is the out-degree
val inputGraph: Graph[Int, String] =
  graph.outerJoinVertices(graph.outDegrees)((vid, _, degOpt) => degOpt.getOrElse(0))
// Construct a graph where each edge contains the weight
// and each vertex is the initial PageRank
val outputGraph: Graph[Double, Double] =
  inputGraph.mapTriplets(triplet => 1.0 / triplet.srcAttr).mapVertices((id, _) => 1.0)
{% endhighlight %}

## Structural Operators
<a name="structural_operators"></a>

Currently GraphX supports only a simple set of commonly used structural operators and we expect to
add more in the future.  The following is a list of the basic structural operators.

{% highlight scala %}
class Graph[VD, ED] {
  def reverse: Graph[VD, ED]
  def subgraph(epred: EdgeTriplet[VD,ED] => Boolean,
               vpred: (VertexId, VD) => Boolean): Graph[VD, ED]
  def mask[VD2, ED2](other: Graph[VD2, ED2]): Graph[VD, ED]
  def groupEdges(merge: (ED, ED) => ED): Graph[VD,ED]
}
{% endhighlight %}

The [`reverse`][Graph.reverse] operator returns a new graph with all the edge directions reversed.
This can be useful when, for example, trying to compute the inverse PageRank.  Because the reverse
operation does not modify vertex or edge properties or change the number of edges, it can be
implemented efficiently without data-movement or duplication.

[Graph.reverse]: api/graphx/index.html#org.apache.spark.graphx.Graph@reverse:Graph[VD,ED]

The [`subgraph`][Graph.subgraph] operator takes vertex and edge predicates and returns the graph
containing only the vertices that satisfy the vertex predicate (evaluate to true) and edges that
satisfy the edge predicate *and connect vertices that satisfy the vertex predicate*.  The `subgraph`
operator can be used in number of situations to restrict the graph to the vertices and edges of
interest or eliminate broken links. For example in the following code we remove broken links:

[Graph.subgraph]: api/graphx/index.html#org.apache.spark.graphx.Graph@subgraph((EdgeTriplet[VD,ED])⇒Boolean,(VertexId,VD)⇒Boolean):Graph[VD,ED]

{% highlight scala %}
// Create an RDD for the vertices
val users: RDD[(VertexId, (String, String))] =
  sc.parallelize(Array((3L, ("rxin", "student")), (7L, ("jgonzal", "postdoc")),
                       (5L, ("franklin", "prof")), (2L, ("istoica", "prof")),
                       (4L, ("peter", "student"))))
// Create an RDD for edges
val relationships: RDD[Edge[String]] =
  sc.parallelize(Array(Edge(3L, 7L, "collab"),    Edge(5L, 3L, "advisor"),
                       Edge(2L, 5L, "colleague"), Edge(5L, 7L, "pi"),
                       Edge(4L, 0L, "student"),   Edge(5L, 0L, "colleague")))
// Define a default user in case there are relationship with missing user
val defaultUser = ("John Doe", "Missing")
// Build the initial Graph
val graph = Graph(users, relationships, defaultUser)
// Notice that there is a user 0 (for which we have no information) connected to users
// 4 (peter) and 5 (franklin).
graph.triplets.map(
    triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._1
  ).collect.foreach(println(_))
// Remove missing vertices as well as the edges to connected to them
val validGraph = graph.subgraph(vpred = (id, attr) => attr._2 != "Missing")
// The valid subgraph will disconnect users 4 and 5 by removing user 0
validGraph.vertices.collect.foreach(println(_))
validGraph.triplets.map(
    triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._1
  ).collect.foreach(println(_))
{% endhighlight %}

> Note in the above example only the vertex predicate is provided.  The `subgraph` operator defaults
> to `true` if the vertex or edge predicates are not provided.

The [`mask`][Graph.mask] operator also constructs a subgraph by returning a graph that contains the
vertices and edges that are also found in the input graph.  This can be used in conjunction with the
`subgraph` operator to restrict a graph based on the properties in another related graph.  For
example, we might run connected components using the graph with missing vertices and then restrict
the answer to the valid subgraph.

[Graph.mask]: api/graphx/index.html#org.apache.spark.graphx.Graph@mask[VD2,ED2](Graph[VD2,ED2])(ClassTag[VD2],ClassTag[ED2]):Graph[VD,ED]

{% highlight scala %}
// Run Connected Components
val ccGraph = graph.connectedComponents() // No longer contains missing field
// Remove missing vertices as well as the edges to connected to them
val validGraph = graph.subgraph(vpred = (id, attr) => attr._2 != "Missing")
// Restrict the answer to the valid subgraph
val validCCGraph = ccGraph.mask(validGraph)
{% endhighlight %}

The [`groupEdges`][Graph.groupEdges] operator merges parallel edges (i.e., duplicate edges between
pairs of vertices) in the multigraph.  In many numerical applications, parallel edges can be *added*
(their weights combined) into a single edge thereby reducing the size of the graph.

[Graph.groupEdges]: api/graphx/index.html#org.apache.spark.graphx.Graph@groupEdges((ED,ED)⇒ED):Graph[VD,ED]

## Join Operators
<a name="join_operators"></a>

In many cases it is necessary to join data from external collections (RDDs) with graphs.  For
example, we might have extra user properties that we want to merge with an existing graph or we
might want to pull vertex properties from one graph into another.  These tasks can be accomplished
using the *join* operators. Below we list the key join operators:

{% highlight scala %}
class Graph[VD, ED] {
  def joinVertices[U](table: RDD[(VertexId, U)])(map: (VertexId, VD, U) => VD)
    : Graph[VD, ED]
  def outerJoinVertices[U, VD2](table: RDD[(VertexId, U)])(map: (VertexId, VD, Option[U]) => VD2)
    : Graph[VD2, ED]
}
{% endhighlight %}

The [`joinVertices`][GraphOps.joinVertices] operator joins the vertices with the input RDD and
returns a new graph with the vertex properties obtained by applying the user defined `map` function
to the result of the joined vertices.  Vertices without a matching value in the RDD retain their
original value.

[GraphOps.joinVertices]: api/graphx/index.html#org.apache.spark.graphx.GraphOps@joinVertices[U](RDD[(VertexId,U)])((VertexId,VD,U)⇒VD)(ClassTag[U]):Graph[VD,ED]

> Note that if the RDD contains more than one value for a given vertex only one will be used.   It
> is therefore recommended that the input RDD be first made unique using the following which will
> also *pre-index* the resulting values to substantially accelerate the subsequent join.
> {% highlight scala %}
val nonUniqueCosts: RDD[(VertexID, Double)]
val uniqueCosts: VertexRDD[Double] =
  graph.vertices.aggregateUsingIndex(nonUnique, (a,b) => a + b)
val joinedGraph = graph.joinVertices(uniqueCosts)(
  (id, oldCost, extraCost) => oldCost + extraCost)
{% endhighlight %}

The more general [`outerJoinVertices`][Graph.outerJoinVertices] behaves similarly to `joinVertices`
except that the user defined `map` function is applied to all vertices and can change the vertex
property type.  Because not all vertices may have a matching value in the input RDD the `map`
function takes an `Option` type.  For example, we can setup a graph for PageRank by initializing
vertex properties with their `outDegree`.

[Graph.outerJoinVertices]: api/graphx/index.html#org.apache.spark.graphx.Graph@outerJoinVertices[U,VD2](RDD[(VertexId,U)])((VertexId,VD,Option[U])⇒VD2)(ClassTag[U],ClassTag[VD2]):Graph[VD2,ED]


{% highlight scala %}
val outDegrees: VertexRDD[Int] = graph.outDegrees
val degreeGraph = graph.outerJoinVertices(outDegrees) { (id, oldAttr, outDegOpt) =>
  outDegOpt match {
    case Some(outDeg) => outDeg
    case None => 0 // No outDegree means zero outDegree
  }
}
{% endhighlight %}

> You may have noticed the multiple parameter lists (e.g., `f(a)(b)`) curried function pattern used
> in the above examples.  While we could have equally written `f(a)(b)` as `f(a,b)` this would mean
> that type inference on `b` would not depend on `a`.  As a consequence, the user would need to
> provide type annotation for the user defined function:
> {% highlight scala %}
val joinedGraph = graph.joinVertices(uniqueCosts,
  (id: VertexID, oldCost: Double, extraCost: Double) => oldCost + extraCost)
{% endhighlight %}


## Neighborhood Aggregation

A key part of graph computation is aggregating information about the neighborhood of each vertex.
For example we might want to know the number of followers each user has or the average age of the
the followers of each user.  Many iterative graph algorithms (e.g., PageRank, Shortest Path, and
connected components) repeatedly aggregate properties of neighboring vertices (e.g., current
PageRank Value, shortest path to the source, and smallest reachable vertex id).

### Map Reduce Triplets (mapReduceTriplets)
<a name="mrTriplets"></a>

[Graph.mapReduceTriplets]: api/graphx/index.html#org.apache.spark.graphx.Graph@mapReduceTriplets[A](mapFunc:org.apache.spark.graphx.EdgeTriplet[VD,ED]=&gt;Iterator[(org.apache.spark.graphx.VertexId,A)],reduceFunc:(A,A)=&gt;A,activeSetOpt:Option[(org.apache.spark.graphx.VertexRDD[_],org.apache.spark.graphx.EdgeDirection)])(implicitevidence$10:scala.reflect.ClassTag[A]):org.apache.spark.graphx.VertexRDD[A]

The core (heavily optimized) aggregation primitive in GraphX is the
[`mapReduceTriplets`][Graph.mapReduceTriplets] operator:

{% highlight scala %}
class Graph[VD, ED] {
  def mapReduceTriplets[A](
      map: EdgeTriplet[VD, ED] => Iterator[(VertexId, A)],
      reduce: (A, A) => A)
    : VertexRDD[A]
}
{% endhighlight %}

The [`mapReduceTriplets`][Graph.mapReduceTriplets] operator takes a user defined map function which
is applied to each triplet and can yield *messages* destined to either (none or both) vertices in
the triplet.  To facilitate optimized pre-aggregation, we currently only support messages destined
to the source or destination vertex of the triplet.  The user defined `reduce` function combines the
messages destined to each vertex.  The `mapReduceTriplets` operator returns a `VertexRDD[A]`
containing the aggregate message (of type `A`) destined to each vertex.  Vertices that do not
receive a message are not included in the returned `VertexRDD`.

<blockquote>

<p>Note that <code>mapReduceTriplets</code> takes an additional optional <code>activeSet</code>
(not shown above see API docs for details) which restricts the map phase to edges adjacent to the
vertices in the provided <code>VertexRDD</code>: </p>

{% highlight scala %}
  activeSetOpt: Option[(VertexRDD[_], EdgeDirection)] = None
{% endhighlight %}

<p>The EdgeDirection specifies which edges adjacent to the vertex set are included in the map
phase. If the direction is <code>In</code>, then the user defined <code>map</code> function will
only be run only on edges with the destination vertex in the active set. If the direction is
<code>Out</code>, then the <code>map</code> function will only be run only on edges originating from
vertices in the active set.  If the direction is <code>Either</code>, then the <code>map</code>
function will be run only on edges with <i>either</i> vertex in the active set.  If the direction is
<code>Both</code>, then the <code>map</code> function will be run only on edges with both vertices
in the active set.  The active set must be derived from the set of vertices in the graph.
Restricting computation to triplets adjacent to a subset of the vertices is often necessary in
incremental iterative computation and is a key part of the GraphX implementation of Pregel. </p>

</blockquote>

In the following example we use the `mapReduceTriplets` operator to compute the average age of the
more senior followers of each user.

{% highlight scala %}
// Import random graph generation library
import org.apache.spark.graphx.util.GraphGenerators
// Create a graph with "age" as the vertex property.  Here we use a random graph for simplicity.
val graph: Graph[Double, Int] =
  GraphGenerators.logNormalGraph(sc, numVertices = 100).mapVertices( (id, _) => id.toDouble )
// Compute the number of older followers and their total age
val olderFollowers: VertexRDD[(Int, Double)] = graph.mapReduceTriplets[(Int, Double)](
  triplet => { // Map Function
    if (triplet.srcAttr > triplet.dstAttr) {
      // Send message to destination vertex containing counter and age
      Iterator((triplet.dstId, (1, triplet.srcAttr)))
    } else {
      // Don't send a message for this triplet
      Iterator.empty
    }
  },
  // Add counter and age
  (a, b) => (a._1 + b._1, a._2 + b._2) // Reduce Function
)
// Divide total age by number of older followers to get average age of older followers
val avgAgeOfOlderFollowers: VertexRDD[Double] =
  olderFollowers.mapValues( (id, value) => value match { case (count, totalAge) => totalAge / count } )
// Display the results
avgAgeOfOlderFollowers.collect.foreach(println(_))
{% endhighlight %}

> Note that the `mapReduceTriplets` operation performs optimally when the messages (and the sums of
> messages) are constant sized (e.g., floats and addition instead of lists and concatenation).  More
> precisely, the result of `mapReduceTriplets` should ideally be sub-linear in the degree of each
> vertex.

### Computing Degree Information

A common aggregation task is computing the degree of each vertex: the number of edges adjacent to
each vertex.  In the context of directed graphs it often necessary to know the in-degree, out-
degree, and the total degree of each vertex.  The  [`GraphOps`][GraphOps] class contains a
collection of operators to compute the degrees of each vertex.  For example in the following we
compute the max in, out, and total degrees:

{% highlight scala %}
// Define a reduce operation to compute the highest degree vertex
def max(a: (VertexId, Int), b: (VertexId, Int)): (VertexId, Int) = {
  if (a._2 > b._2) a else b
}
// Compute the max degrees
val maxInDegree: (VertexId, Int)  = graph.inDegrees.reduce(max)
val maxOutDegree: (VertexId, Int) = graph.outDegrees.reduce(max)
val maxDegrees: (VertexId, Int)   = graph.degrees.reduce(max)
{% endhighlight %}

### Collecting Neighbors

In some cases it may be easier to express computation by collecting neighboring vertices and their
attributes at each vertex. This can be easily accomplished using the
[`collectNeighborIds`][GraphOps.collectNeighborIds] and the
[`collectNeighbors`][GraphOps.collectNeighbors] operators.

[GraphOps.collectNeighborIds]: api/graphx/index.html#org.apache.spark.graphx.GraphOps@collectNeighborIds(EdgeDirection):VertexRDD[Array[VertexId]]
[GraphOps.collectNeighbors]: api/graphx/index.html#org.apache.spark.graphx.GraphOps@collectNeighbors(EdgeDirection):VertexRDD[Array[(VertexId,VD)]]


{% highlight scala %}
class GraphOps[VD, ED] {
  def collectNeighborIds(edgeDirection: EdgeDirection): VertexRDD[Array[VertexId]]
  def collectNeighbors(edgeDirection: EdgeDirection): VertexRDD[ Array[(VertexId, VD)] ]
}
{% endhighlight %}

> Note that these operators can be quite costly as they duplicate information and require
> substantial communication.  If possible try expressing the same computation using the
> `mapReduceTriplets` operator directly.


















## Background: Featurization

To apply most machine learning algorithms, we must first preprocess and featurize the data.  That is, for each data point, we must generate a vector of numbers describing the salient properties of that data point.  In our case, each data point will consist of a unique Wikipedia article identifier (i.e., a unique combination of Wikipedia project code and page title) and associated traffic statistics.  We will generate 24-dimensional feature vectors, with each feature vector entry summarizing the page view counts for the corresponding hour of the day.

Recall that each record in our dataset consists of a string with the format "`<date_time> <project_code> <page_title> <num_hits> <page_size>`".  The format of the date-time field is YYYYMMDD-HHmmSS (where 'M' denotes month, and 'm' denotes minute).

Given our time constraints, in order to focus on the machine learning algorithms themselves, we have pre-processed the data to create the featurized dataset that we will use to implement K-means clustering.
{% comment %}
If you are interested in doing featurization on your own, you can follow [these instructions](featurization.html).
{% endcomment %}

## Clustering

[K-Means clustering](http://en.wikipedia.org/wiki/K-means_clustering) is a popular clustering algorithm that can be used to partition your dataset into K clusters. We now look at how we can implement K-Means clustering using Spark to cluster the featurized Wikipedia dataset.

## Setup
Similar to the Spark streaming exercises above, we will be using a standalone project template for this exercise. In your AMI, this has been setup in `/root/kmeans/[scala|java|python]/`. You should find the following items in the directory.

<div class="codetabs">
<div data-lang="scala">

<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>sbt</code>: Directory containing the SBT tool</li>
<li><code>build.sbt</code>: SBT project file</li>
<li><code>WikipediaKMeans.scala</code>: Main Scala program that you are going to edit, compile and run</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.scala</code>. It should look as follows:
</div>
<div data-lang="java">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>sbt</code>: Directory containing the SBT tool</li>
<li><code>build.sbt</code>: SBT project file</li>
<li><code>WikipediaKMeans.java</code>: Main Java program that you are going to edit, compile and run</li>
<li><code>JavaHelpers.java</code>: Some helper functions used by the template.</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.java</code>. It should look as follows:
</div>
<div data-lang="python">
<div class="prettyprint" style="margin-bottom:10px">
<ul style="margin-bottom:0px">
<li><code>WikipediaKMeans.py</code>: Main Python program that you are going to edit and run</li>
</ul>
</div>

The main file you are going to edit, compile and run for the exercises is <code>WikipediaKMeans.py</code>. It should look as follows:
</div>
</div>


<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.util.Vector

import org.apache.log4j.Logger
import org.apache.log4j.Level

import scala.util.Random
import scala.io.Source

object WikipediaKMeans {
  def parseVector(line: String): Vector = {
      return new Vector(line.split(',').map(_.toDouble))
  }

  // Add any new functions you need here

  def main(args: Array[String]) {
    Logger.getLogger("spark").setLevel(Level.WARN)
    val sparkHome = "/root/spark"
    val jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar"
    val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
    val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim

    val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))

    val K = 10
    val convergeDist = 1e-6

    val data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
            t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()

    // Your code goes here

    sc.stop();
    System.exit(0)
  }
}
~~~
</div>
<div data-lang="java" markdown="1">
~~~
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.io.*;
import java.util.*;
import com.google.common.collect.Lists;

import scala.Tuple2;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.util.Vector;


public class WikipediaKMeans {
  // Add any new functions you need here

  public static void main(String[] args) throws Exception {
    Logger.getLogger("spark").setLevel(Level.WARN);
    String sparkHome = "/root/spark";
    String jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar";
    String master = JavaHelpers.getSparkUrl();
    String masterHostname = JavaHelpers.getMasterHostname();
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans",
      sparkHome, jarFile);

    int K = 10;
    double convergeDist = .000001;

    JavaPairRDD<String, Vector> data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
      new PairFunction<String, String, Vector>() {
        public Tuple2<String, Vector> call(String in)
        throws Exception {
          String[] parts = in.split("#");
          return new Tuple2<String, Vector>(
            parts[0], JavaHelpers.parseVector(parts[1]));
        }
      }
     ).cache();

    // Your code goes here

    sc.stop();
    System.exit(0);
  }
}
~~~
</div>
<div data-lang="python" markdown="1">
~~~
import os
import sys
import numpy as np

from pyspark import SparkContext

def setClassPath():
    oldClassPath = os.environ.get('SPARK_CLASSPATH', '')
    cwd = os.path.dirname(os.path.realpath(__file__))
    os.environ['SPARK_CLASSPATH'] = cwd + ":" + oldClassPath


def parseVector(line):
    return np.array([float(x) for x in line.split(',')])

# Add any new functions you need here

if __name__ == "__main__":
    setClassPath()
    master = open("/root/spark-ec2/cluster-url").read().strip()
    masterHostname = open("/root/spark-ec2/masters").read().strip()
    sc = SparkContext(master, "PythonKMeans")
    K = 10
    convergeDist = 1e-5

    lines = sc.textFile(
	"hdfs://" + masterHostname + ":9000/wikistats_featurized")
    data = lines.map(
	lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()

    # Your code goes here
~~~
</div>
</div>

Let's first take a closer look at our template code in a text editor on the cluster itself, then we'll start adding code to the template. Locate the `WikipediaKMeans` class and open it with a text editor.

<div class="codetabs">
<div data-lang="scala">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/scala
vim WikipediaKMeans.scala  # If you don't know vim, you can use emacs or nano
</pre>
</div>
<div data-lang="java">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/java
vim WikipediaKMeans.java  # If you don't know vim, you can use emacs or nano
</pre>
</div>
<div data-lang="python">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/python
vim WikipediaKMeans.py  # If you don't know Vim, you can use emacs or nano
</pre>
</div>
</div>

The cluster machines have vim, emacs, and nano installed for editing. Alternatively, you can use your favorite text editor locally and then copy-paste content into vim, emacs, or nano before running it.

For any Spark computation, we first create a Spark context object. For Scala or Java programs, we do that by providing the Spark cluster URL, the Spark home directory, and the JAR file that will be generated when we compile our program. For Python programs, we only need to provide the Spark cluster URL. Finally, we also name our program "WikipediaKMeans" to identify it in Spark's web UI.

This is what it looks like in our template code:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans", sparkHome, jarFile);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    sc = SparkContext(master, "PythonKMeans")
~~~
</div>
</div>

Next, the code uses the SparkContext to read in our featurized dataset. The [featurization process](#command-line-preprocessing-and-featurization) created a 24-dimensional feature vector for each article in our Wikipedia dataset, with each vector entry summarizing the page view counts for the corresponding hour of the day. Each line in the file consists of the page identifier and the features separated by commas.

Next, the code reads the file in from HDFS and parses each line to create a RDD which contains pairs of `(String, Vector)`.

A quick note about the `Vector` class we will using in this exercise: For Scala and Java programs we will be using the [Vector](http://spark-project.org/docs/latest/api/core/index.html#spark.util.Vector) class provided by Spark. For Python, we will be using [NumPy arrays](http://docs.scipy.org/doc/numpy/reference/generated/numpy.array.html).

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
   val data = sc.textFile(
       "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
           t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()
   val count = data.count()
   println("Number of records " + count)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    JavaPairRDD<String, Vector> data = sc.textFile(
      "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
        new PairFunction<String, String, Vector>() {
          public Tuple2<String, Vector> call(String in)
          throws Exception {
            String[] parts = in.split("#");
            return new Tuple2<String, Vector>(parts[0], parseVector(parts[1]));
          }
        }
      ).cache();
    long count = data.count();
    System.out.println("Number of records " + count);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    lines = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized")
    data = lines.map(
        lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()
    count = data.count()
    print "Number of records " + str(count)
~~~
</div>
</div>

Now, let's make our first edit to the file and add code to count the number of records in our dataset by running `data.count()` and print the value. Find the comment that says "Your code goes here" and replace it with:

<div class="codetabs">
<div data-lang="scala" markdown="1">
~~~
    val count = data.count()
    println("Number of records " + count)
~~~
</div>
<div data-lang="java" markdown="1">
~~~
    long count = data.count();
    System.out.println("Number of records " + count);
~~~
</div>
<div data-lang="python" markdown="1">
~~~
    count = data.count()
    print "Number of records " + str(count)
~~~
</div>
</div>

## Running the program
Before we implement the K-Means algorithm, here is quick reminder on how you can run the program at any point during this exercise. Save the `WikipediaKMeans` file run the following commands:

<div class="codetabs">
<div data-lang="scala" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/scala
sbt/sbt package run
</pre>

This command will compile the `WikipediaKMeans` class and create a JAR file in `/root/kmeans/scala/target/scala-2.9.3/`. Finally, it will run the program. You should see output similar to the following on your screen:

</div>
<div data-lang="java" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/java
sbt/sbt package run
</pre>

This command will compile the `WikipediaKMeans` class and create a JAR file in `/root/kmeans/java/target/scala-2.9.3/`. Finally, it will run the program. You should see output similar to the following on your screen:

</div>
<div data-lang="python" markdown="1">
<pre class="prettyprint lang-bsh">
cd /root/kmeans/python
/root/spark/pyspark ./WikipediaKMeans.py
</pre>

This command will run `WikipediaKMeans` on your Spark cluster. You should see output similar to the following on your screen:

</div>
</div>

<pre class="prettyprint lang-bsh">
Number of records 802450
</pre>

## K-Means algorithm
We are now set to start implementing the K-means algorithm, so remove or comment out the lines we just added to print the record count and let's get started.

1. The first step in the K-Means algorithm is to initialize our centers by randomly picking `K` points from our dataset. We use the `takeSample` function in Spark to do this.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var centroids = data.takeSample(false, K, 42).map(x => x._2)
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      List<Tuple2<String, Vector>> centroidTuples = data.takeSample(false, K, 42);
      final List<Vector> centroids = Lists.newArrayList();
      for (Tuple2<String, Vector> t: centroidTuples) {
        centroids.add(t._2());
      }
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      # NOTE: PySpark does not support takeSample() yet. Use first K points instead.
      centroids = map(lambda (x, y): y, data.take(K))
~~~
    </div>
    </div>

1. Next, we need to compute the closest centroid for each point and we do this by using the `map` operation in Spark. For every point we will use a function `closestPoint` (which you have to write!) to compute the closest centroid.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var closest = data.map(p => (closestPoint(p._2, centroids), p._2)) // Won't work until you write closestPoint()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      JavaPairRDD<Integer, Vector> closest = data.map(
        new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
          public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
            return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
          }
        }
      );
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      closest = data.map(
          lambda (x, y) : (closestPoint(y, centroids), y))
~~~
    </div>
    </div>

    **Exercise:** Write the `closestPoint` function in `WikipediaKMeans` to return the index of the closest centroid given a point and the set of all centroids. To get you started, we provide the type signature of the function:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      static int closestPoint(Vector p, List<Vector> centers) {
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      def closestPoint(p, centers):
~~~
    </div>
    </div>

    In case you get stuck, you can use our solution given here:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
        var bestIndex = 0
        var closest = Double.PositiveInfinity
        for (i <- 0 until centers.length) {
          val tempDist = p.squaredDist(centers(i))
          if (tempDist < closest) {
            closest = tempDist
            bestIndex = i
          }
        }
        return bestIndex
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      static int closestPoint(Vector p, List<Vector> centers) {
        int bestIndex = 0;
        double closest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < centers.size(); i++) {
          double tempDist = p.squaredDist(centers.get(i));
          if (tempDist < closest) {
            closest = tempDist;
            bestIndex = i;
          }
        }
        return bestIndex;
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      def closestPoint(p, centers):
          bestIndex = 0
          closest = float("+inf")
          for i in range(len(centers)):
              dist = np.sum((p - centers[i]) ** 2)
              if dist < closest:
                  closest = dist
                  bestIndex = i
          return bestIndex
~~~
    </div>
    </div>
    </div>

    On the line defining the variable called `closest`, the `map` operation creates a new RDD which contains a tuple for every point. The first element in the tuple is the index of the closest centroid for the point and second element is the `Vector` representing the point.

1. Now that we have the closest centroid for each point, we can cluster our points by the centroid they belong to. To do this, we use a `groupByKey` operation as shown below. The `groupByKey` operator creates a new `RDD[(Int, Array[Vector])]` where the key is the index of the centroid and the values are all the points which belong to its cluster.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var pointsGroup = closest.groupByKey()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      pointsGroup = closest.groupByKey()
~~~
    </div>
    </div>

1. We can now calculate our new centroids by computing the mean of the points that belong to a cluster. We do this using `mapValues` which allows us to apply a function on all the values for a particular key. We also use a function `average` to compute the average of an array of vectors.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
        new Function<List<Vector>, Vector>() {
          public Vector call(List<Vector> ps) throws Exception {
            return average(ps);
          }
        }).collectAsMap();
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      newCentroids = pointsGroup.mapValues(
          lambda x : average(x)).collectAsMap()
~~~
    </div>
    </div>

    **Exercise:** Write the `average` function in `WikipediaKMeans` to sum all the vectors and divide it by the number of vectors present in the input array. Your function should return a new Vector which is the average of the input vectors. You can look at our solution in case you get stuck.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      def average(ps: Seq[Vector]) : Vector = {
        val numVectors = ps.size
        var out = new Vector(ps(0).elements)
        for (i <- 1 until numVectors) {
          out += ps(i)
        }
        out / numVectors
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      static Vector average(List<Vector> ps) {
        int numVectors = ps.size();
        Vector out = new Vector(ps.get(0).elements());
        for (int i = 0; i < numVectors; i++) {
          out.addInPlace(ps.get(i));
        }
        return out.divide(numVectors);
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      def average(points):
          numVectors = len(points)
          out = np.array(points[0])
          for i in range(2, numVectors):
              out += points[i]
          out = out / numVectors
          return out
~~~
    </div>
    </div>
    </div>

1. Finally, lets calculate how different our new centroids are compared to our initial centroids. This will be used to determine if we have converged to the right set of centroids. To do this we create a variable named `tempDist` that represents the distance between the old centroids and new centroids. In Scala and Java, we use the `squaredDist` function to compute the distance between two vectors. For Python we can use `np.sum`. We sum up the distance over `K` centroids and print this value.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      var tempDist = 0.0
      for (i <- 0 until K) {
        tempDist += centroids(i).squaredDist(newCentroids(i))
      }
      println("Finished iteration (delta = " + tempDist + ")")
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      double tempDist = 0.0;
      for (int i = 0; i < K; i++) {
        tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
      }
      for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
        centroids.set(t.getKey(), t.getValue());
      }
      System.out.println("Finished iteration (delta = " + tempDist + ")");
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
      tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
      print "Finished iteration (delta = " + str(tempDist) + ")"
~~~
    </div>
    </div>

    You can now save `WikipediaKMeans`, and [run it](#running-the-program) to make sure our program is working fine so far. If everything went right, you should see the output similar to the following. (NOTE: Your output may not exactly match this as we use a random set of initial centers).

    <pre class="prettyprint lang-bsh">Finished iteration (delta = 0.025900765093161377)</pre>

1. The above steps represent one iteration of the K-Means algorithm. We will need to repeat these steps until the distance between newly computed centers and the ones from the previous iteration become lesser than some small constant. (`convergeDist` in our program). To do this we put our steps inside a `do while` loop and check if `tempDist` is lower than the convergence constant. Putting this together with the previous steps, our code will look like:

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
~~~
      do {
        var closest = data.map(p => (closestPoint(p._2, centroids), p._2))
        var pointsGroup = closest.groupByKey()
        var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()

        tempDist = 0.0
        for (i <- 0 until K) {
          tempDist += centroids(i).squaredDist(newCentroids(i))
        }

        // Assign newCentroids to centroids
        for (newP <- newCentroids) {
          centroids(newP._1) = newP._2
        }
        iter += 1
        println("Finished iteration " + iter + " (delta = " + tempDist + ")")
      } while (tempDist > convergeDist)
~~~
    </div>
    <div data-lang="java" markdown="1">
~~~
      do {
          JavaPairRDD<Integer, Vector> closest = data.map(
            new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
              public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
                return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
              }
             }
            );
          JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
          Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
            new Function<List<Vector>, Vector>() {
              public Vector call(List<Vector> ps) throws Exception {
                return average(ps);
              }
            }).collectAsMap();
          double tempDist = 0.0;
          for (int i = 0; i < K; i++) {
            tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
          }
          for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
            centroids.set(t.getKey(), t.getValue());
          }
          System.out.println("Finished iteration (delta = " + tempDist + ")");

        } while (tempDist > convergeDist);
~~~
    </div>
    <div data-lang="python" markdown="1">
~~~
    while tempDist > convergeDist:
        closest = data.map(
            lambda (x, y) : (closestPoint(y, centroids), y))
        pointsGroup = closest.groupByKey()
        newCentroids = pointsGroup.mapValues(
            lambda x : average(x)).collectAsMap()

        tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
        for (x, y) in newCentroids.iteritems():
            centroids[x] = y
        print "Finished iteration (delta = " + str(tempDist) + ")"
~~~
    </div>
    </div>

1. **Exercise:** After the `do while` loop completes, write code to print the titles of 10 articles assigned to each cluster.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
      val numArticles = 10
      for((centroid, centroidI) <- centroids.zipWithIndex) {
        // print numArticles articles which are assigned to this centroid’s cluster
        data.filter(p => (closestPoint(p._2, centroids) == centroidI)).take(numArticles).foreach(
            x => println(x._1))
        println()
      }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
      System.out.println("Cluster with some articles:");
      int numArticles = 10;
      for (int i = 0; i < centroids.size(); i++) {
        final int index = i;
        List<Tuple2<String, Vector>> samples =
          data.filter(new Function<Tuple2<String, Vector>, Boolean>() {
            public Boolean call(Tuple2<String, Vector> in) throws Exception {
              return closestPoint(in._2(), centroids) == index;
            }
          }).take(numArticles);
        for(Tuple2<String, Vector> sample: samples) {
          System.out.println(sample._1());
        }
        System.out.println();
      }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
      print "Clusters with some articles"
      numArticles = 10
      for i in range(0, len(centroids)):
        samples = data.filter(lambda (x,y) : closestPoint(y, centroids) == i).take(numArticles)
        for (name, features) in samples:
          print name

        print " "
~~~
    </div>
    </div>
    </div>

1. You can save `WikipediaKMeans` and [run your program](#running-the-program) now. If everything goes well your algorithm will converge after some iterations and your final output should have clusters similar to the following output. Recall that our feature vector consisted of the number of times a page was visited in every hour of the day. We can see that pages are clustered together by _language_ indicating that they are accessed during the same hours of the day.

    <pre class="nocode">
    ja %E6%AD%8C%E8%97%A4%E9%81%94%E5%A4%AB
    ja %E7%8B%90%E3%81%AE%E5%AB%81%E5%85%A5%E3%82%8A
    ja %E3%83%91%E3%82%B0
    ja %E7%B4%AB%E5%BC%8F%E9%83%A8
    ja %E3%81%8A%E5%A7%89%E7%B3%BB
    ja Reina
    ja %E8%B8%8A%E3%82%8A%E5%AD%97
    ja %E3%83%90%E3%82%BB%E3%83%83%E3%83%88%E3%83%8F%E3%82%A6%E3%83%B3%E3%83%89
    ja %E3%81%BF%E3%81%9A%E3%81%BB%E3%83%95%E3%82%A3%E3%83%8A%E3%83%B3%E3%82%B7%E3%83%A3%E3%83%AB%E3%82%B0%E3%83%AB%E3%83%BC%E3%83%97
    ja %E6%96%B0%E6%BD%9F%E7%9C%8C%E7%AB%8B%E6%96%B0%E6%BD%9F%E5%8D%97%E9%AB%98%E7%AD%89%E5%AD%A6%E6%A0%A1

    ru %D0%A6%D0%B8%D1%80%D0%BA%D0%BE%D0%BD%D0%B8%D0%B9
    de Kirchenstaat
    ru %D0%90%D0%B2%D1%80%D0%B0%D0%B0%D0%BC
    de Migr%C3%A4ne
    de Portal:Freie_Software
    de Datenflusskontrolle
    de Dornier_Do_335
    de.b LaTeX-Schnellkurs:_Griechisches_Alphabet
    de Mach-Zahl
    ru Total_Commander

    en 761st_Tank_Battalion_(United_States)
    en File:N34---Douglas-DC3-Cockpit.jpg
    en Desmond_Child
    en Philadelphia_Freeway
    en Zygon
    en File:Ruth1918.jpg
    en Sally_Rand
    en File:HLHimmler.jpg
    en Waiting_to_Exhale
    en File:Sonic1991b.jpg
    </pre>

1. In case you want to look at the complete solution, here is how `WikipediaKMeans` will look after all the above steps have been completed.

    <div class="codetabs">
    <div data-lang="scala" markdown="1">
    <div class="solution" markdown="1">
~~~
    import spark.SparkContext
    import spark.SparkContext._
    import spark.util.Vector

    import org.apache.log4j.Logger
    import org.apache.log4j.Level

    import scala.util.Random
    import scala.io.Source

    object WikipediaKMeans {
      def parseVector(line: String): Vector = {
          return new Vector(line.split(',').map(_.toDouble))
      }

      def closestPoint(p: Vector, centers: Array[Vector]): Int = {
        var index = 0
        var bestIndex = 0
        var closest = Double.PositiveInfinity
        for (i <- 0 until centers.length) {
          val tempDist = p.squaredDist(centers(i))
          if (tempDist < closest) {
            closest = tempDist
            bestIndex = i
          }
        }
        return bestIndex
      }

      def average(ps: Seq[Vector]) : Vector = {
        val numVectors = ps.size
        var out = new Vector(ps(0).elements)
        for (i <- 1 until numVectors) {
          out += ps(i)
        }
        out / numVectors
      }

      // Add any new functions you need here

      def main(args: Array[String]) {
        Logger.getLogger("spark").setLevel(Level.WARN)
        val sparkHome = "/root/spark"
        val jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar"
        val master = Source.fromFile("/root/spark-ec2/cluster-url").mkString.trim
        val masterHostname = Source.fromFile("/root/spark-ec2/masters").mkString.trim

        val sc = new SparkContext(master, "WikipediaKMeans", sparkHome, Seq(jarFile))

        val K = 10
        val convergeDist = 1e-6

        val data = sc.textFile(
            "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
                t => (t.split("#")(0), parseVector(t.split("#")(1)))).cache()

        val count = data.count()
        println("Number of records " + count)

        // Your code goes here
        var centroids = data.takeSample(false, K, 42).map(x => x._2)
        var tempDist = 1.0
        do {
          var closest = data.map(p => (closestPoint(p._2, centroids), p._2))
          var pointsGroup = closest.groupByKey()
          var newCentroids = pointsGroup.mapValues(ps => average(ps)).collectAsMap()
          tempDist = 0.0
          for (i <- 0 until K) {
            tempDist += centroids(i).squaredDist(newCentroids(i))
          }
          for (newP <- newCentroids) {
            centroids(newP._1) = newP._2
          }
          println("Finished iteration (delta = " + tempDist + ")")
        } while (tempDist > convergeDist)

        println("Clusters with some articles:")
        val numArticles = 10
        for((centroid, centroidI) <- centroids.zipWithIndex) {
          // print numArticles articles which are assigned to this centroid’s cluster
          data.filter(p => (closestPoint(p._2, centroids) == centroidI)).take(numArticles).foreach(
              x => println(x._1))
          println()
        }

        sc.stop()
        System.exit(0)
      }
    }
~~~
    </div>
    </div>
    <div data-lang="java" markdown="1">
    <div class="solution" markdown="1">
~~~
    import org.apache.log4j.Logger;
    import org.apache.log4j.Level;
    import java.io.*;
    import java.util.*;
    import com.google.common.collect.Lists;

    import scala.Tuple2;
    import spark.api.java.*;
    import spark.api.java.function.*;
    import spark.util.Vector;


    public class WikipediaKMeans {
      static int closestPoint(Vector p, List<Vector> centers) {
        int bestIndex = 0;
        double closest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < centers.size(); i++) {
          double tempDist = p.squaredDist(centers.get(i));
          if (tempDist < closest) {
            closest = tempDist;
            bestIndex = i;
          }
        }
        return bestIndex;
      }

      static Vector average(List<Vector> ps) {
        int numVectors = ps.size();
        Vector out = new Vector(ps.get(0).elements());
        for (int i = 0; i < numVectors; i++) {
          out.addInPlace(ps.get(i));
        }
        return out.divide(numVectors);
      }

      public static void main(String[] args) throws Exception {
        Logger.getLogger("spark").setLevel(Level.WARN);
        String sparkHome = "/root/spark";
        String jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar";
        String master = JavaHelpers.getSparkUrl();
        String masterHostname = JavaHelpers.getMasterHostname();
        JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans",
          sparkHome, jarFile);

        int K = 10;
        double convergeDist = .000001;

        JavaPairRDD<String, Vector> data = sc.textFile(
          "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
          new PairFunction<String, String, Vector>() {
            public Tuple2<String, Vector> call(String in) throws Exception {
              String[] parts = in.split("#");
              return new Tuple2<String, Vector>(
               parts[0], JavaHelpers.parseVector(parts[1]));
            }
          }).cache();


        long count = data.count();
        System.out.println("Number of records " + count);

        List<Tuple2<String, Vector>> centroidTuples = data.takeSample(false, K, 42);
        final List<Vector> centroids = Lists.newArrayList();
        for (Tuple2<String, Vector> t: centroidTuples) {
          centroids.add(t._2());
        }

        System.out.println("Done selecting initial centroids");
        double tempDist;
        do {
          JavaPairRDD<Integer, Vector> closest = data.map(
            new PairFunction<Tuple2<String, Vector>, Integer, Vector>() {
              public Tuple2<Integer, Vector> call(Tuple2<String, Vector> in) throws Exception {
                return new Tuple2<Integer, Vector>(closestPoint(in._2(), centroids), in._2());
              }
            }
          );

          JavaPairRDD<Integer, List<Vector>> pointsGroup = closest.groupByKey();
          Map<Integer, Vector> newCentroids = pointsGroup.mapValues(
            new Function<List<Vector>, Vector>() {
             public Vector call(List<Vector> ps) throws Exception {
               return average(ps);
            }
          }).collectAsMap();
          tempDist = 0.0;
          for (int i = 0; i < K; i++) {
            tempDist += centroids.get(i).squaredDist(newCentroids.get(i));
          }
          for (Map.Entry<Integer, Vector> t: newCentroids.entrySet()) {
            centroids.set(t.getKey(), t.getValue());
          }
          System.out.println("Finished iteration (delta = " + tempDist + ")");

        } while (tempDist > convergeDist);

        System.out.println("Cluster with some articles:");
        int numArticles = 10;
        for (int i = 0; i < centroids.size(); i++) {
          final int index = i;
          List<Tuple2<String, Vector>> samples =
          data.filter(new Function<Tuple2<String, Vector>, Boolean>() {
            public Boolean call(Tuple2<String, Vector> in) throws Exception {
            return closestPoint(in._2(), centroids) == index;
          }}).take(numArticles);
          for(Tuple2<String, Vector> sample: samples) {
           System.out.println(sample._1());
          }
          System.out.println();
        }
        sc.stop();
        System.exit(0);
      }
    }
~~~
    </div>
    </div>
    <div data-lang="python" markdown="1">
    <div class="solution" markdown="1">
~~~
   import os
   import sys

   import numpy as np
   from pyspark import SparkContext

   def setClassPath():
       oldClassPath = os.environ.get('SPARK_CLASSPATH', '')
       cwd = os.path.dirname(os.path.realpath(__file__))
       os.environ['SPARK_CLASSPATH'] = cwd + ":" + oldClassPath

   def parseVector(line):
       return np.array([float(x) for x in line.split(',')])

   def closestPoint(p, centers):
       bestIndex = 0
       closest = float("+inf")
       for i in range(len(centers)):
           dist = np.sum((p - centers[i]) ** 2)
           if dist < closest:
               closest = dist
               bestIndex = i
       return bestIndex

   def average(points):
       numVectors = len(points)
       out = np.array(points[0])
       for i in range(2, numVectors):
           out += points[i]
       out = out / numVectors
       return out

   if __name__ == "__main__":
       setClassPath()
       master = open("/root/spark-ec2/cluster-url").read().strip()
       masterHostname = open("/root/spark-ec2/masters").read().strip()
       sc = SparkContext(master, "PythonKMeans")
       K = 10
       convergeDist = 1e-5

       lines = sc.textFile(
           "hdfs://" + masterHostname + ":9000/wikistats_featurized")

       data = lines.map(
           lambda x: (x.split("#")[0], parseVector(x.split("#")[1]))).cache()
       count = data.count()
       print "Number of records " + str(count)

       # TODO: PySpark does not support takeSample(). Use first K points instead.
       centroids = map(lambda (x, y): y, data.take(K))
       tempDist = 1.0

       while tempDist > convergeDist:
           closest = data.map(
               lambda (x, y) : (closestPoint(y, centroids), y))
           pointsGroup = closest.groupByKey()
           newCentroids = pointsGroup.mapValues(
               lambda x : average(x)).collectAsMap()

           tempDist = sum(np.sum((centroids[x] - y) ** 2) for (x, y) in newCentroids.iteritems())
           for (x, y) in newCentroids.iteritems():
               centroids[x] = y
           print "Finished iteration (delta = " + str(tempDist) + ")"
           sys.stdout.flush()


       print "Clusters with some articles"
       numArticles = 10
       for i in range(0, len(centroids)):
         samples = data.filter(lambda (x,y) : closestPoint(y, centroids) == i).take(numArticles)
         for (name, features) in samples:
           print name

         print " "
~~~
    </div>
    </div>
    </div>

1. **Challenge Exercise:** The K-Means implementation uses a `groupBy` and `mapValues` to compute the new centers. This can be optimized by using a running sum of the vectors that belong to a cluster and running counter of the number of vectors present in a cluster. How would you use the Spark API to implement this?
