package main.scala.cn.bigdata.spark.scala.cn.zte.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.storage.StorageLevel


object FlumeStreaming {
  
  
  def main(args: Array[String]): Unit = {
    
    StreamingExamples.setStreamingLogLevels()
    
    val conf = new SparkConf
    conf.setMaster("local[2]").setAppName("flumestreaming")

    val ssc = new StreamingContext(conf,Seconds(2))
    
    val flumeDs = FlumeUtils.createStream(ssc, "weekend01", 4545, StorageLevel.MEMORY_AND_DISK_2)
    
    val cnts = flumeDs.count().map { x => "received ===> " + x + "flume events"}
    
    cnts.print()

    val wordsFlumeDs = flumeDs.flatMap { x => new String( x.event.getBody().array()).split(" ") }.map { x => (x,1) }
    
    val wordcounts = wordsFlumeDs.reduceByKey(_+_)
    
//    wordcounts.saveAsTextFiles(prefix, suffix)
    
    wordcounts.print()
    ssc.start()
    ssc.awaitTermination()
    
    
  }
  
  
  
  
  
}