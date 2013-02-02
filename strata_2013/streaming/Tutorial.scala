import spark.streaming._
import StreamingContext._
import TutorialHelper._

object Tutorial {
  def main(args: Array[String]) {
    
    // Location of the Spark directory 
    val sparkHome = "/root/spark"
    
    // URL of the Spark cluster
    val sparkUrl = getSparkUrl()

    // Location of the required JAR files 
    val jarFile = "target/scala-2.9.2/Tutorial_2.9.2-0.1-SNAPSHOT.jar"

    // Twitter credentials from login.txt
    val (twitterUsername, twitterPassword) = getTwitterCredentials()
   
    // Your code goes here

  }
}

