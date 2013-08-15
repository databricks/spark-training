import spark.streaming.api.java._;
import spark.storage.StorageLevel
import sys.process.stringSeqToProcess
import java.io.File

object ScalaHelper {
  /** Returns the HDFS URL */
  def getHdfsUrl(): String = {
    try {
      val name : String = Seq("bash", "-c", "curl -s http://169.254.169.254/latest/meta-data/hostname") !! ;
      println("Hostname = " + name)
      "hdfs://" + name.trim + ":9000"
    } catch {
      case e: Exception => {
        if (new File("../local").exists) {
          "."
        } else {
          throw e
        }
      }
    }
  }
}
