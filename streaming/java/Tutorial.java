import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.twitter.*;
import org.apache.spark.streaming.api.java.*;
import twitter4j.*;
import java.util.Arrays;
import scala.Tuple2;

public class Tutorial {
  public static void main(String[] args) throws Exception {

    // Checkpoint directory 
    String checkpointDir = TutorialHelper.getCheckpointDirectory();

    // Configuring Twitter credentials 
    String apiKey = "";
    String apiSecret = "";
    String accessToken = "";
    String accessTokenSecret = "";
    TutorialHelper.configureTwitterCredentials(apiKey, apiSecret, accessToken, accessTokenSecret);

    // Your code goes here

  }
}

