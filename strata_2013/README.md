Like the Spark docs, this is compiled using Jekyll.

To use the `jekyll` command, you will need to have Jekyll installed, the
easiest way to do this is via a Ruby Gem, see the [jekyll installation
instructions](https://github.com/mojombo/jekyll/wiki/install). This will create
a directory called _site containing index.html as well as the rest of the
compiled files. Read more about Jekyll at
https://github.com/mojombo/jekyll/wiki.

The docs use [Kramdown](http://kramdown.rubyforge.org/) markdown extensions for
marking code in code tags, and we Javascript to highlight the code and generate
the table of contents (this code was adapted from the offical Scala
documentation site).
