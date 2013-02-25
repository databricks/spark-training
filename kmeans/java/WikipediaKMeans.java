import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import scala.Tuple2;
import spark.api.java.*;
import spark.api.java.function.*;
import spark.util.Vector;

import java.io.*;
import java.util.*;
import com.google.common.collect.Lists;

public class WikipediaKMeans {
  /** Creates a vector object from a string */
  static Vector parseVector(String line) {
    String[] parts = line.split(",");
    double[] dParts = new double[parts.length];
    for (int i = 0; i < parts.length; i++) {
      dParts[i] = Double.parseDouble(parts[i]);
    }
    return new Vector(dParts);
  }

  /** Gets the URL of this spark cluster */
  static String getSparkUrl() throws Exception {
    File file = new File("/root/spark-ec2/cluster-url");
    if (file.exists()) {
      List<String> lines = readLines(file);
      return lines.get(0);
    } else {
      throw new Exception("Could not find " + file);
    }
  }

  /** Gets the hostname of the Spark master */
  static String getMasterHostname() throws Exception {
    File file = new File("/root/spark-ec2/masters");
    if (file.exists()) {
      List<String> lines = readLines(file);
      return lines.get(0);
    } else {
      throw new Exception("Could not find " + file);
    }
  }

  /** Reads the lines of a file into a List */
  private static List<String> readLines(File file) throws IOException {
    FileReader fileReader = new FileReader(file);
    BufferedReader bufferedReader = new BufferedReader(fileReader);
    List<String> lines = new ArrayList<String>();
    String line = null;
    while ((line = bufferedReader.readLine()) != null) {
      lines.add(line);
    }
    bufferedReader.close();
    return lines;
  }

  // Implement your own functions here

  public static void main(String[] args) throws Exception {
    Logger.getLogger("spark").setLevel(Level.WARN);
    String sparkHome = "/root/spark";
    String jarFile = "target/scala-2.9.2/wikipedia-kmeans_2.9.2-0.0.jar";
    String master = getSparkUrl();
    String masterHostname = getMasterHostname();
    JavaSparkContext sc = new JavaSparkContext(master, "WikipediaKMeans", 
      sparkHome, jarFile);

    int K = 4;
    double convergeDist = .000001;

    JavaPairRDD<String, Vector> data = sc.textFile(
        "hdfs://" + masterHostname + ":9000/wikistats_featurized").map(
      new PairFunction<String, String, Vector>() {
        public Tuple2<String, Vector> call(String in)
        throws Exception {
          String[] parts = in.split("#");
          return new Tuple2<String, Vector>(parts[0], parseVector(parts[1]));
        }
      }
     ).cache();

    // Your code goes here

    System.exit(0);
  }
}
