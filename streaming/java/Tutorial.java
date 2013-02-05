import spark.api.java.*;
import spark.api.java.function.*;
import spark.streaming.*;
import spark.streaming.api.java.*;
import twitter4j.*;
import java.util.Arrays;
import scala.Tuple2;

public class Tutorial {
  public static void main(String[] args) throws Exception {
    // Location of the Spark directory 
    String sparkHome = "/root/spark";
    
    // URL of the Spark cluster
    String sparkUrl = TutorialHelper.getSparkUrl();

    // Location of the required JAR files 
    String jarFile = "target/scala-2.9.2/tutorial_2.9.2-0.1-SNAPSHOT.jar";

    // HDFS directory for checkpointing
    // TODO put back    String checkpointDir = TutorialHelper.getHdfsUrl() + "/checkpoint/";
    String checkpointDir = "/tmp";

    // Twitter credentials from login.txt
    String twitterUsername = TutorialHelper.getTwitterUsername();
    String twitterPassword = TutorialHelper.getTwitterPassword();
   
    // Your code goes here
  }
}

