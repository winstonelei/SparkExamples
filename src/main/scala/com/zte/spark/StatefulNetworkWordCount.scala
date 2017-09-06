package main.scala.com.zte.spark

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StatefulNetworkWordCount {
  def main(args: Array[String]) {
    /*if (args.length < 2) {
      System.err.println("Usage: StatefulNetworkWordCount <master> <hostname> <port> <seconds>\n" +
        "In local mode, <master> should be 'local[n]' with n > 1")
      System.exit(1)
    }*/
   // StreamingExamples.setStreamingLogLevels()
    val updateFunc = (values: Seq[Int], state: Option[Int]) => {
      val currentCount = values.foldLeft(0)(_ + _)
      val previousCount = state.getOrElse(0)
      Some(currentCount + previousCount)
    }
    //创建StreamingContext
    /*val ssc = new StreamingContext(args(0), "StatefulNetworkWordCount",
      Seconds(args(3).toInt), System.getenv("SPARK_HOME"), StreamingContext.jarOfClass(this.getClass))*/

    val sparkConf = new SparkConf().setAppName("StatefulNetworkWordCount")
    // Create the context with a 1 second batch size
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    ssc.checkpoint(".")
    //创建NetworkInputDStream，需要指定ip和端口
    val lines = ssc.socketTextStream("192.168.84.132", 9999)
    val words = lines.flatMap(_.split(" "))
    val wordDstream = words.map(x => (x, 1))
    //使用updateStateByKey来更新状态
    val stateDstream = wordDstream.updateStateByKey[Int](updateFunc)
    stateDstream.print()
    ssc.start()
    ssc.awaitTermination()
}
}
