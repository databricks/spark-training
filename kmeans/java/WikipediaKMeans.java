import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.io.*;
import java.util.*;
import com.google.common.collect.Lists;

import scala.Tuple2;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.util.Vector;


public class WikipediaKMeans {
  // Add any new functions you need here

  public static void main(String[] args) throws Exception {
    Logger.getLogger("spark").setLevel(Level.WARN);
    String sparkHome = "/root/spark";
    String jarFile = "target/scala-2.9.3/wikipedia-kmeans_2.9.3-0.0.jar";
    String master = JavaHelpers.getSparkUrl();
    String masterHostname = JavaHelpers.getMasterHostname();
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans", 
      sparkHome, jarFile);

    int K = 10;
    double convergeDist = .000001;

    JavaPairRDD<String, Vector> data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
      new PairFunction<String, String, Vector>() {
        public Tuple2<String, Vector> call(String in)
        throws Exception {
          String[] parts = in.split("#");
          return new Tuple2<String, Vector>(
            parts[0], JavaHelpers.parseVector(parts[1]));
        }
      }
     ).cache();

    // Your code goes here
    sc.stop();
    System.exit(0);
  }
}
