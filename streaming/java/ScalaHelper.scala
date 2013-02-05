import spark.streaming.api.java._;
import twitter4j._
import spark.storage.StorageLevel
import sys.process.stringSeqToProcess

object ScalaHelper {
  def twitterStream(username: String, password: String, jsc: JavaStreamingContext): JavaDStream[Status] = {
    val stream = new TwitterInputDStream(jsc.ssc, username, password, Nil, StorageLevel.MEMORY_ONLY_SER_2)
    jsc.ssc.registerInputStream(stream)
    new JavaDStream(stream)
  }
  
  /** Returns the HDFS URL */
  def getHdfsUrl(): String = {
    val name : String = Seq("bash", "-c", "curl -s http://169.254.169.254/latest/meta-data/hostname") !! ;
    println("Hostname = " + name)
    "hdfs://" + name.trim + ":9000"
  }
}
