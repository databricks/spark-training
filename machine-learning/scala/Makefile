rate:
	python ../bin/rateMovies
build: 
	../../sbt/sbt assembly  
run:
	${SPARK_HOME}/bin/spark-submit --class MovieLensALS target/scala-2.10/movielens-als-assembly-0.1.jar ../../data/movielens/medium/ ../personalRatings.txt
