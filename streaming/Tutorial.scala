import spark._
import spark.streaming._
import SparkContext._
import StreamingContext._
import TutorialHelper._

object Tutorial {
  def main(args: Array[String]) {
    
    // Location of the Spark directory 
    val sparkHome = "/root/spark"
    
    // URL of the Spark cluster
    val sparkUrl = TutorialHelper.getSparkUrl()

    // Location of the required JAR files 
    val jarFile = "target/scala-2.9.2/tutorial_2.9.2-0.1-SNAPSHOT.jar"

    // HDFS directory for checkpointing
    val checkpointDir = TutorialHelper.getHdfsUrl() + "/checkpoint/" 

    // Twitter credentials from login.txt
    val (twitterUsername, twitterPassword) = TutorialHelper.getTwitterCredentials()
   
    // Your code goes here
  }
}

