import spark.streaming._
import spark.storage.StorageLevel
import scala.io.Source
import java.io.File
import org.apache.log4j.Logger
import org.apache.log4j.Level
import sys.process.stringSeqToProcess

class TutorialHelper(ssc: StreamingContext) {
  def twitterStream(username: String, password: String, filters: Seq[String] = Nil) = {
    val stream = new TwitterInputDStream(ssc, username, password, filters, StorageLevel.MEMORY_ONLY_SER_2)
    ssc.registerInputStream(stream)
    stream
    }
  }

object TutorialHelper {
  Logger.getLogger("spark").setLevel(Level.WARN)
  Logger.getLogger("spark.streaming.NetworkInputTracker").setLevel(Level.INFO)
    
  implicit def convert(ssc: StreamingContext) = new TutorialHelper(ssc)
  
  /** Returns the Twitter username and password from the file login.txt */
  def getTwitterCredentials (): (String, String) = {
    val file = new File("./login.txt")
    if (file.exists) {
      val lines = Source.fromFile(file.toString).getLines.toSeq
      if (lines.size < 2) 
        throw new Exception("Error parsing " + file + " - it does not have two lines")
      (lines(0), lines(1)) 
    } else {
      throw new Exception("Could not find " + file)
    }
  }

  /** Returns the Spark URL */
  def getSparkUrl(): String = {
    val file = new File("/root/spark-ec2/cluster-url")
    if (file.exists) {
      val url = Source.fromFile(file.toString).getLines.toSeq.head
      url
    } else {
      throw new Exception("Could not find " + file)
    }
    
  }

  /** Returns the HDFS URL */
  def getHdfsUrl(): String = {
    val name : String = Seq("bash", "-c", "curl -s http://169.254.169.254/latest/meta-data/hostname") !! ;
    println("Hostname = " + name)
    "hdfs://" + name.trim + ":9000"
  }
}

