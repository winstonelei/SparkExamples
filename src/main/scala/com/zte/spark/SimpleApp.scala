package main.scala.com.zte.spark

/**
  * Created by stone on 2016/5/27.
  */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {
    //val logFile = "YOUR_SPARK_HOME/README.md" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple").setMaster("local")
    val sc = new SparkContext(conf)
    val logFile=sc.textFile("qingshu.txt")
    val numAs = logFile.filter(line => line.contains("a")).count()
    println(numAs)

    //logFile.flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_).saveAsTextFile("result.txt")

  /*  val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))*/
  }
}
