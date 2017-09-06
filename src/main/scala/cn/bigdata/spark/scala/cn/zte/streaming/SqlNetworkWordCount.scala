package cn.bigdata.spark.scala.cn.zte.streaming

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import org.apache.spark.sql.SQLContext
import org.apache.spark.storage.StorageLevel


/**
  * Created by winstone on 2017/8/30.
  */
object SqlNetworkWordCount {
  def main(args: Array[String]) {
      if(args.length<2){
          System.exit(1)
      }

    val sparkConf = new SparkConf().setAppName("sqlNetWordCount")
    val ssc = new StreamingContext(sparkConf,Seconds(2))
    val lines = ssc.socketTextStream(args(0),args(1).toInt,StorageLevel.MEMORY_AND_DISK)
    val words = lines.flatMap(_.split(" "))

    words.foreachRDD((rdd: RDD[String], time: Time) => {
      val sqlContext = SQLContextSingleton.getInstance(rdd.sparkContext)
      import sqlContext.implicits._
      val wordsDataFrame = rdd.map(w => Record(w)).toDF()
      wordsDataFrame.registerTempTable("words")
      val wordCountsDataFrame =
        sqlContext.sql("select word, count(*) as total from words group by word")
      wordCountsDataFrame.show()
    })
    ssc.start()
    ssc.awaitTermination()
  }

}

case class Record(word: String)


/** Lazily instantiated singleton instance of SQLContext */
object SQLContextSingleton {

  @transient  private var instance: SQLContext = _

  def getInstance(sparkContext: SparkContext): SQLContext = {
    if (instance == null) {
      instance = new SQLContext(sparkContext)
    }
    instance
  }
}
