package main.scala.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

class LogPreprocess {

}
object LogPreprocess {
  
  def main(args: Array[String]): Unit = {
    
    val conf = new SparkConf
    conf.setMaster("spark://weekend01:7077").setAppName("logpreprocess")
    val sc =  new SparkContext(conf)
    
    val file = sc.textFile("hdfs://weekend01:9000/excersize/orginlog/StockDetail.txt")
    val completion = file.filter { x => x.startsWith("D") }.map { x => x.slice(2, x.length()) }
    completion.saveAsTextFile("hdfs://weekend01:9000/excersize/processedlog/")
    
    sc.stop()

  }
  
  
  
}