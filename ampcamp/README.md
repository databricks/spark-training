Like the Spark docs, this is compiled using Jekyll.

To use the `jekyll` command, you will need to have Jekyll installed, the
easiest way to do this is via a Ruby Gem, see the [jekyll installation
instructions](https://github.com/mojombo/jekyll/wiki/install). This will create
a directory called _site containing index.html as well as the rest of the
compiled files. Read more about Jekyll at
https://github.com/mojombo/jekyll/wiki.

The docs use:
* [Kramdown](http://kramdown.rubyforge.org/) markdown extensions for
  marking code in code tags.

* [Prettify](https://code.google.com/p/google-code-prettify/wiki/GettingStarted)
  javascript library for syntax highlighting code.

* [TOCPlugin](https://code.google.com/p/samaxesjs/wiki/TOCPlugin) javascript 
  library to generate the table of contents

* [LESS](http://lesscss.org/) for style sheets, which compiles down to straight
  css but requires the less compiler (lessc) to be installed on your computer.
  If it is installed then the .css files will be generated from the .less files
  automatically by means of a jekyll "plugin", which is a ruby script in the
  _plugins directory. NOTE: if you use jekyll --auto the plugin that compiles
  the less files into css will not run automatically when you save a .less file,
  you have to manually run `jekyll` or `jekyll --auto` again.

Some of this code was adapted from the offical Scala documentation site.
