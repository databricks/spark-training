import spark.streaming._
import spark.storage.StorageLevel


class TutorialHelper(ssc: StreamingContext) {
  def twitterStream(username: String, password: String, filters: Seq[String] = Nil) = {
    val stream = new TwitterInputDStream(ssc, username, password, filters, StorageLevel.MEMORY_ONLY_SER_2)
    ssc.registerInputStream(stream)
    stream
    }
  }

object TutorialHelper {

  implicit def convert(ssc: StreamingContext) = new TutorialHelper(ssc)
  
  /** Returns the Twitter username and password from the file login.txt */
  def getTwitterCredentials (): (String, String) = {
    val lines = scala.io.Source.fromFile("./login.txt").getLines.toSeq
    if (lines.size < 2) 
      throw new Exception("Error parsing login.txt - it does not have two lines")
    (lines(0), lines(1)) 
  }

  /** Returns the Spark URL */
  def getSparkUrl(): String = {
    "local[2]"
  }
}

