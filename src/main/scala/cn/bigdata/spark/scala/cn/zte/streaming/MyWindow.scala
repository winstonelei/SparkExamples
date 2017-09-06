package main.scala.cn.bigdata.spark.scala.cn.zte.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import scala.collection.Seq
import org.apache.spark.HashPartitioner


object MyWindow{
  
    
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf
    conf.setMaster("local[2]").setAppName("mywindow")
    val sc = new StreamingContext(conf,Seconds(2))
    sc.checkpoint(".")
    val line = sc.socketTextStream("weekend01", 9999, StorageLevel.MEMORY_AND_DISK_SER);

    val words = line.flatMap { x => x.split(" ")}.map { x => (x,1) }

    val wordsCount = words.reduceByKeyAndWindow(_+_, _-_, Seconds(10), Seconds(6))
    
    val abc = wordsCount.map{case (word,count) => (count,word)}

    val sortabc = abc.transform(_.sortByKey(false)).map{case (count,word) => (word,count)}
    
    sortabc.print()

    sc.start()
    sc.awaitTermination()

  }
  
}