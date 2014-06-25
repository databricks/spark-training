import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.File
import org.apache.log4j.Logger
import org.apache.log4j.Level
import sys.process.stringSeqToProcess

object TutorialHelper {
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  Logger.getLogger("org.apache.spark.storage.BlockManager").setLevel(Level.ERROR)
    
  /** Configures the Oauth Credentials for accessing Twitter */
  def configureTwitterCredentials(apiKey: String, apiSecret: String, accessToken: String, accessTokenSecret: String) {
    val configs = new HashMap[String, String] ++= Seq(
      "apiKey" -> apiKey, "apiSecret" -> apiSecret, "accessToken" -> accessToken, "accessTokenSecret" -> accessTokenSecret)
    println("Configuring Twitter OAuth")
    configs.foreach{ case(key, value) => 
        if (value.trim.isEmpty) {
          throw new Exception("Error setting authentication - value for " + key + " not set")
        }
        val fullKey = "twitter4j.oauth." + key.replace("api", "consumer")
        System.setProperty(fullKey, value.trim)
        println("\tProperty " + fullKey + " set as [" + value.trim + "]") 
    }
    println()
  }

  /** Returns the Spark URL */
  def getSparkUrl(): String = {
    val file = new File("/root/spark-ec2/cluster-url")
    if (file.exists) {
      val url = Source.fromFile(file.toString).getLines.toSeq.head
      url
    } else if (new File("../local").exists) {
      "local[4]"
    } else {
      throw new Exception("Could not find " + file)
    }
  }

  /** Returns the HDFS URL */
  def getCheckpointDirectory(): String = {
    try {
      val name : String = Seq("bash", "-c", "curl -s http://169.254.169.254/latest/meta-data/hostname") !! ;
      println("Hostname = " + name)
      "hdfs://" + name.trim + ":9000/checkpoint/"
    } catch {
      case e: Exception => {
        "./checkpoint/"
      }
    }
  }
}

