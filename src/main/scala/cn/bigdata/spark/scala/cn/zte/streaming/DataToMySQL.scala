package cn.bigdata.spark.scala.cn.zte.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by winstone on 2017/9/6 0006.
  * 实现了最新的单词存入数据库中
  */
object DataToMySQL {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("use the foreachRDD write data to mysql").setMaster("local[2]")
    val ssc = new StreamingContext(conf,Seconds(10))
    val streamData = ssc.socketTextStream("master",9999)
    val wordCount = streamData.map(line =>(line.split(",")(0),1)).reduceByKeyAndWindow(_+_,Seconds(60))
    val hottestWord = wordCount.transform(itemRDD => {
      val top3 = itemRDD.map(pair => (pair._2, pair._1))
        .sortByKey(false).map(pair => (pair._2, pair._1)).take(3)
       ssc.sparkContext.makeRDD(top3)
    })

    hottestWord.foreachRDD( rdd =>{
      rdd.foreachPartition(partitionOfRecords =>{
        val connect = scalaConnectPool.getConnection
        connect.setAutoCommit(false)
        val stmt = connect.createStatement()
        partitionOfRecords.foreach(record =>{
          stmt.addBatch("insert into searchKeyWord (insert_time,keyword,search_count) values (now(),'"+record._1+"','"+record._2+"')")
        })
        stmt.executeBatch()
        connect.commit()
       }
      )
     }
    )
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }
}
