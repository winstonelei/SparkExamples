package main.scala.cn.bigdata.spark.scala.cn.zte.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import scala.collection.Seq
import org.apache.spark.HashPartitioner


object Mystateful{
  
    
  def main(args: Array[String]): Unit = {
    val accumulate = (values:Seq[Int],pre:Option[Int]) => {
                val accusum = values.sum
                val presum = pre.getOrElse(0)
                Some(accusum + presum)
    }


    val updateFunc = (iterator:Iterator[(String,Seq[Int],Option[Int])]) => {
  //通过两个函数来实现
//			  iterator.flatMap(t => accumulate(t._2,t._3).map { x => (t._1,x) })
  //通过一个函数，两行来实现
//			  val it2 = iterator.map(t => (t._1,t._2.sum,t._3))
//        it2.flatMap(t => Some[Int](t._2 + t._3.getOrElse(0)).map { x => (t._1,x) })
  //通过一行来实现
      iterator.flatMap(t => Some[Int](t._2.sum + t._3.getOrElse(0)).map { x => (t._1,x) })
    }
  val conf = new SparkConf
  conf.setMaster("local[2]").setAppName("mystateful")

  val sc = new StreamingContext(conf,Seconds(2))
  sc.checkpoint(".")
  val line = sc.socketTextStream("weekend01", 9999, StorageLevel.MEMORY_AND_DISK_SER);
  val words = line.flatMap { x => x.split(" ")}.map { x => (x,1) }
  val initialRDD = sc.sparkContext.parallelize(List(("hello",1),("tom",1)))

  val stateDstream = words.updateStateByKey(updateFunc, new HashPartitioner(sc.sparkContext.defaultParallelism), true, initialRDD)
  stateDstream.print()
  sc.start()
  sc.awaitTermination()

    
  }
  
}