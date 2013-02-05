import spark.streaming.api.java.*;

public class Tutorial {
  public static void main(String[] args) throws Exception {
    // Location of the Spark directory 
    String sparkHome = "/root/spark";
    
    // URL of the Spark cluster
    String sparkUrl = TutorialHelper.getSparkUrl();

    // Location of the required JAR files 
    String jarFile = "target/scala-2.9.2/tutorial_2.9.2-0.1-SNAPSHOT.jar";

    // HDFS directory for checkpointing
    String checkpointDir = TutorialHelper.getHdfsUrl() + "/checkpoint/";

    // Twitter credentials from login.txt
    String twitterUsername = TutorialHelper.getTwitterPassword();
    String twitterPassword = TutorialHelper.getTwitterUsername();
   
    // Your code goes here

  }
}

