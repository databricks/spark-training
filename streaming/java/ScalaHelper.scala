import spark.streaming.api.java._;
import twitter4j._
import spark.storage.StorageLevel

object ScalaHelper {
  def twitterStream(username: String, password: String, jsc: JavaStreamingContext): JavaDStream[Status] = {
    val stream = new TwitterInputDStream(jsc.ssc, username, password, Nil, StorageLevel.MEMORY_ONLY_SER_2)
    jsc.ssc.registerInputStream(stream)
    new JavaDStream(stream)
  }
}
