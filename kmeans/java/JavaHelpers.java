import java.io.*;
import java.util.*;
import com.google.common.collect.Lists;

import spark.util.Vector;

public class JavaHelpers {
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
}
