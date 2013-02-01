import java.util.ArrayList;

import spark.api.java.*;
import spark.api.java.function.*;

public class Main {
  public static void main(String[] args) {
    String sparkHome = "/root/spark-dev";
    String jarFile = "target/scala-2.9.1/java-app-template_2.9.1-0.0.jar";
    JavaSparkContext sc = new JavaSparkContext(
      "local", "TestJob", sparkHome, jarFile);

    ArrayList<Integer> list = new ArrayList<Integer>();
    for (int i = 1; i <= 10; i++) {
      list.add(i);
    }

    System.out.println("1+2+...+10 = " + sc.parallelize(list).reduce(
      new Function2<Integer, Integer, Integer>() {
        public Integer call(Integer i1, Integer i2) {
          return i1 + i2;
        }
      }
    ));

    System.exit(0);
  }
}
