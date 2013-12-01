---
layout: global
title: Introduction to the Scala Shell
categories: [module]
navigation:
  weight: 40
  show: true
skip-chapter-toc: true
---

This chapter will teach you the basics of using the Scala shell and introduce you to functional programming with collections.

If you're already comfortable with Scala or plan on using the Python shell for the interactive Spark sections of this mini course, skip ahead to the next section.

This exercise is based on a great tutorial, <a href="http://www.artima.com/scalazine/articles/steps.html" target="_blank">First Steps to Scala</a>.
However, reading through that whole tutorial and trying the examples at the console may take considerable time, so we will provide a basic introduction to the Scala shell here. Do as much as you feel you need (in particular you might want to skip the final "bonus" question).

1. Launch the Scala console by typing:

   ~~~
   scala
   ~~~

1. Declare a list of integers as a variable called "myNumbers".

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> val myNumbers = List(1, 2, 5, 4, 7, 3)
   myNumbers: List[Int] = List(1, 2, 5, 4, 7, 3)
   </pre>
   </div>

1. Declare a function, `cube`, that computes the cube (third power) of an Int.
   See steps 2-4 of First Steps to Scala.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> def cube(a: Int): Int = a * a * a
   cube: (a: Int)Int
   </pre>
   </div>

1. Apply the function to `myNumbers` using the `map` function. Hint: read about the `map` function in <a href="http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List" target="_blank">the Scala List API</a> and also in Table 1 about halfway through the <a href="http://www.artima.com/scalazine/articles/steps.html" target="_blank">First Steps to Scala</a> tutorial.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> myNumbers.map(x => cube(x))
          res: List[Int] = List(1, 8, 125, 64, 343, 27)
          // Scala also provides some shorthand ways of writing this:
          // myNumbers.map(cube(_))
          // myNumbers.map(cube)
   </pre>
   </div>

1. Then also try writing the function inline in a `map` call, using closure notation.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> myNumbers.map{x => x * x * x}
   res: List[Int] = List(1, 8, 125, 64, 343, 27)
   </pre>
   </div>

1. Define a `factorial` function that computes n! = 1 * 2 * ... * n given input n.
   You can use either a loop or recursion, in our solution we use recursion (see steps 5-7 of <a href="http://www.artima.com/scalazine/articles/steps.html" target="_blank">First Steps to Scala</a>).
   Then compute the sum of factorials in `myNumbers`. Hint: check out the `sum` function in <a href="http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List" target="_blank">the Scala List API</a>.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> def factorial(n:Int):Int = if (n==0) 1 else n * factorial(n-1) // From http://bit.ly/b2sVKI
   factorial: (Int)Int
   scala&gt; myNumbers.map(factorial).sum
   res: Int = 5193
   </pre>
   </div>

1. <i>**BONUS QUESTION.** This is a more challenging task and may require 10 minutes or more to complete, so you should consider skipping it depending on your timing so far.</i> Do a wordcount of a textfile. More specifically, create and populate a Map with words as keys and counts of the number of occurrences of the word as values.

   You can load a text file as an array of lines as shown below:

   <pre class="prettyprint lang-scala linenums">
   import scala.io.Source
   val lines = Source.fromFile("/root/mesos/README").getLines.toArray
   </pre>

   Then, instantiate a `collection.mutable.HashMap[String,Int]` and use functional methods to populate it with wordcounts. Hint, in our solution, which is inspired by <a href="http://bit.ly/6mhGvo" target="_blank">this solution online</a>, we use <a href="http://richard.dallaway.com/in-praise-of-flatmap" target="_blank">`flatMap`</a> and then `map`.

   <div class="solution" markdown="1">
   <pre class="prettyprint lang-scala">
   scala> import scala.io.Source
   import scala.io.Source

   scala> val lines = Source.fromFile("/root/mesos/README").getLines.toArray
   lines: Array[String] = Array(hlep, help, plot, set terminal pdf, pdf, set terminal pdf color, set output "monolithic-utilization-thru-day.pdf", set terminal pdf enhanced color, set terminal pdf monochrome, set terminal pdf mono, set output "graph.pdf", set terminal x11 enhanced color, help terminal, x11, quit, plot "utilization" using 1:2, plot "utilization" using 1/2:2, #plot "utilization" using 1:($2/), RadDeg, x!, Inv, sin, ln, π, cos, log, e, tan, √, Ans, EXP, xy, (, ), %, AC, 7, 8, 9, ÷, 4, 5, 6, ×, 1, 2, 3, -, 0, ., =, +, plot "utilization" using 1:($2/278400), plot "utilization.cpu.and.mem" using 1:($3/556800), plot "7-day-mono-ya-c_0.01-stdout-UTILIZATION" using 1:2, plot "7-day-mono-ya-c_0.01-stdout-UTILIZATION" using 1:($2/12500*8), plot [0:1] "7-day-mono-ya-c_0.01-stdout...

   scala> val counts = new collection.mutable.HashMap[String, Int].withDefaultValue(0)
   counts: scala.collection.mutable.Map[String,Int] = Map()

   scala> lines.flatMap(line => line.split(" ")).foreach(word => counts(word) += 1)

   scala> counts
   res: scala.collection.mutable.Map[String,Int] = Map(2.6) -> 1, will -> 4, contain -> 1, Once -> 1, might -> 3, (requires -> 1, cluster. -> 3, up -> 2, There -> 1, addition, -> 1, host -> 3, distribution -> 2, [build] -> 2, Then -> 1, tests -> 3, ========================== -> 1, ZooKeeper -> 1, JAVA_HOME, -> 1, deploy -> 1, is -> 3, Cluster -> 1, [build]/bin/mesos-build-env.sh.in). -> 1, require -> 1, see -> 1, specific -> 3, necessary -> 2, JAVA_CPPFLAGS, -> 1, via: -> 1, collection -> 1, always, -> 1, configure -> 3, CPPFLAGS -> 1, email: -> 1, a -> 18, for -> 8, --localstatedir -> 1, building -> 1, slave(s) -> 1, After -> 2, populated -> 1, located -> 1, necessary) -> 1, we -> 3, doing: -> 2, with -> 4, Building -> 1, are: -> 1, JAR -> 1, [build]/src -> 1, --help -> 1, JAVA_CPPFLAGS...
   </pre>
   </div>

   <div class="solution" markdown="1">
   Or, a purely functional solution:

   <pre class="prettyprint lang-scala">
   scala> import scala.io.Source
   import scala.io.Source

   scala> val lines = Source.fromFile("/root/mesos/README").getLines.toArray
   lines: Array[String] = Array(hlep, help, plot, set terminal pdf, pdf, set terminal pdf color, set output "monolithic-utilization-thru-day.pdf", set terminal pdf enhanced color, set terminal pdf monochrome, set terminal pdf mono, set output "graph.pdf", set terminal x11 enhanced color, help terminal, x11, quit, plot "utilization" using 1:2, plot "utilization" using 1/2:2, #plot "utilization" using 1:($2/), RadDeg, x!, Inv, sin, ln, π, cos, log, e, tan, √, Ans, EXP, xy, (, ), %, AC, 7, 8, 9, ÷, 4, 5, 6, ×, 1, 2, 3, -, 0, ., =, +, plot "utilization" using 1:($2/278400), plot "utilization.cpu.and.mem" using 1:($3/556800), plot "7-day-mono-ya-c_0.01-stdout-UTILIZATION" using 1:2, plot "7-day-mono-ya-c_0.01-stdout-UTILIZATION" using 1:($2/12500*8), plot [0:1] "7-day-mono-ya-c_0.01-stdout...

   scala> val emptyCounts = Map[String,Int]().withDefaultValue(0)
   emptyCounts: scala.collection.immutable.Map[String,Int] = Map()

   scala> val words = lines.flatMap(line => line.split(" "))
   words: Array[java.lang.String] = Array(Building, and, Installing, =======================, "", For, the, impatient:,...

   scala> val counts = words.foldLeft(emptyCounts)({(currentCounts: Map[String,Int], word: String) => currentCounts.updated(word, currentCounts(word) + 1)})
   counts: scala.collection.immutable.Map[String,Int] = Map(used -> 3, "deploy" -> 2, launch -> 1,...

   scala> counts
   </pre>
   </div>
