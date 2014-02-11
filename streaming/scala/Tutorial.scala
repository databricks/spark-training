import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
import TutorialHelper._

object Tutorial {
  def main(args: Array[String]) {
    
    // Location of the Spark directory 
    val sparkHome = "/root/spark"
    
    // URL of the Spark cluster
    val sparkUrl = TutorialHelper.getSparkUrl()

    // Location of the required JAR files 
    val jarFile = "target/scala-2.10/tutorial_2.10-0.1-SNAPSHOT.jar"

    // HDFS directory for checkpointing
    val checkpointDir = TutorialHelper.getHdfsUrl() + "/checkpoint/" 

    // Configure Twitter credentials using twitter.txt
    TutorialHelper.configureTwitterCredentials()
    
    // Your code goes here
  }
}

