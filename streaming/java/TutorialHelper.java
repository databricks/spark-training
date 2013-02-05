import spark.streaming.api.java.*;
import spark.storage.StorageLevel;
import scala.io.Source;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.lang.*;

class TutorialHelper {
  static {
    Logger.getLogger("spark").setLevel(Level.WARN);
    Logger.getLogger("spark.streaming.NetworkInputTracker").setLevel(Level.INFO);
  }

  static String getTwitterUsername() throws Exception {
    File file = new File("../login.txt");
    if (file.exists()) {
      List<String> lines = readLines(file);
      if (lines.size() < 2)
        throw new Exception("Error parsing " + file + " - it does not have two lines");
      return lines.get(0).trim();
    } else {
      throw new Exception("Could not find " + file);
    }
  }

  static String getTwitterPassword() throws Exception {
    File file = new File("../login.txt");
    if (file.exists()) {
      List<String> lines = readLines(file);
      if (lines.size() < 2)
        throw new Exception("Error parsing " + file + " - it does not have two lines");
      return lines.get(1).trim();
    } else {
      throw new Exception("Could not find " + file);
    }
  }

  /** Returns the HDFS URL */
  static String getHdfsUrl() throws Exception {
    Runtime run = Runtime.getRuntime() ;
    String cmd = "bash -c 'curl -s  http://169.254.169.254/latest/meta-data/hostname'";
    Process pr = run.exec(cmd) ;
    pr.waitFor();
    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream())) ;
    String name = buf.readLine();
    System.out.println("Hostname = " + name);
    return "hdfs://" + name.trim() + ":9000";
  }

  /** Returns the Spark URL */
  static String getSparkUrl() throws Exception {
    File file = new File("/root/spark-ec2/cluster-url");
    if (file.exists()) {
      List<String> lines = readLines(file);
      return lines.get(0);
    } else {
      throw new Exception("Could not find " + file);
    }
  }

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
}

