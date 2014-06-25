import org.apache.spark.streaming.api.java._
import org.apache.spark.storage.StorageLevel
import sys.process.stringSeqToProcess
import java.io.File

object ScalaHelper {
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
