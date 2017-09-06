package main.scala.cn.bigdata.spark.scala.cn.zte.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 使用Scala开发集群运行的Spark来实现在线热搜索词
  *
  * 背景描述：在社交网络（例如微博）、电子商务（例如京东）、搜索引擎（例如百度）等人们核心关注的内容之一就是我所关注的内容中
  * 大家正在最关注什么或者说当前的热点是什么，这在实际企业级应用中是非常有价值的。例如我们关心过去30分钟大家正在热搜索什么，并且
  * 每5分钟更新一次，这就使得热点内容是动态更新，当然也是更有价值。strom也可以实现，但是它是一条一条做数据处理的，代码非常麻烦，而sparkstreaming就是
  * 在线批处理的。
  * 实现技术：Spark Streaming提供了滑动窗口的技术来支撑实现上述业务背景，窗口是batch  interval的倍数，这里就是30分钟为一个窗口，每个5分钟中是有很多interval的，
  * 随着时间的推移，这是一个滑动的过程。明显中间重复了25分钟，一般不是每次都算下，它不需要checkpoint。而是从性能角度考虑，算最新的，加新减旧，需要checkpoint。
  * 我们可以使用reduceByKeyAndWindow操作来做具体实现，对每个window的函数执行reducebykey操作，重载的俩方法分别是每次都重新算30分钟的内容和算新减旧
  *
  *
  */
object OnlineHottestItems {
  def main(args: Array[String]){
    /**
      * 第1步：创建Spark的配置对象SparkConf，设置Spark程序的运行时的配置信息，
      * 例如说通过setMaster来设置程序要链接的Spark集群的Master的URL,如果设置
      * 为local，则代表Spark程序在本地运行，特别适合于机器配置条件非常差（例如
      * 只有1G的内存）的初学者       *
      */
    val conf = new SparkConf() //创建SparkConf对象
    conf.setAppName("OnlineHottestItems") //设置应用程序的名称，在程序运行的监控界面可以看到名称
    conf.setMaster("spark://master1:7077") //此时，程序在Spark集群

    /**
      * 此处设置Batch Interval是在Spark Streaming中生成基本Job的时间单位，窗口和滑动时间间隔
      * 一定是该Batch Interval的整数倍
      */
    val ssc = new StreamingContext(conf, Seconds(5))

    // 这是加新减旧，要checkpoint，因为要复用40秒的RDD。这里的每个RDD是没任何关系的，要服用，必须checkpoint
    //     ssc.checkpoint("/library/onlinehot/")
    val hottestStream = ssc.socketTextStream("master1", 9999)
    /**
      * 用户搜索的格式简化为name item，在这里我们由于要计算出热点内容，所以只需要提取出item即可
      * 提取出的item然后通过map转换为（item，1）格式
      */
    val searchPair = hottestStream.map(_.split(" ")(1)).map(item => (item, 1))
    //一分钟里面每隔20秒更新一次，一个批次为5秒，这是每次都重新算。这里就会得到过去一分钟大家热搜的词或者热点，每隔20秒更新
    val hottestDStream = searchPair.reduceByKeyAndWindow((v1:Int, v2:Int) => v1 + v2, Seconds(60), Seconds(20))

    //这是加新减旧，第一个参数加上新的，第2个参数，减去过去20秒的
    // val hottestDStream = searchPair.reduceByKeyAndWindow((v1: Int, v2: Int) => v1 + v2, (v1: Int, v2: Int) => v1 - v2, Seconds(60), Seconds(20))
    //Dstream没有sortByKey的操作，所以排序用transform实现，false为降序，take(3)拿排名前3的东西

   var sortmap = hottestDStream.map(pair => (pair._2, pair._1))

   var sortRdd = sortmap.transform(rdd=>{
      val takeRdd =  rdd.map(pair =>{
         (pair._2,pair._1)}).sortByKey(false).map(newPair => (newPair._2,newPair._1)).take(3)
      ssc.sparkContext.makeRDD(takeRdd)
   })

    /*val top3 = hottestDStream.map(pair => (pair._2, pair._1)).sortByKey(false).
      map(pair => (pair._2, pair._1)).take(3)
    for(item <- top3){
      println(item)
    }
    hottestItemRDD
  }).print()*/

  ssc.start()
  ssc.awaitTermination()

}
}