package main.scala.cn.bigdata.spark.scala.cn.zte.streaming

/**
  * Created by stone on 2016/8/17.
  */

import cn.bigdata.spark.scala.cn.zte.streaming.scalaConnectPool
import org.apache.spark.SparkConf
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 使用Spark Streaming+Spark SQL来在线动态计算电商中不同类别中最热门的商品排名，例如手机这个类别下面最热门的三种手机、电视这个类别
  * 下最热门的三种电视，该实例在实际生产环境下具有非常重大的意义；
  *   实现技术：Spark Streaming+Spark SQL，之所以Spark Streaming能够使用ML、sql、graphx等功能是因为有foreachRDD和Transform
  * 等接口，这些接口中其实是基于RDD进行操作，所以以RDD为基石，就可以直接使用Spark其它所有的功能，就像直接调用API一样简单。
  *  假设说这里的数据的格式：user item category，例如Rocky Samsung Android
  */
object OnlineTheTop3ItemForEachCategory2DB {
  def main(args: Array[String]){
    /**
      * 第1步：创建Spark的配置对象SparkConf，设置Spark程序的运行时的配置信息，
      * 例如说通过setMaster来设置程序要链接的Spark集群的Master的URL,如果设置
      * 为local，则代表Spark程序在本地运行，特别适合于机器配置条件非常差（例如
      * 只有1G的内存）的初学者       *
      */
    val conf = new SparkConf() //创建SparkConf对象
    conf.setAppName("OnlineTheTop3ItemForEachCategory2DB") //设置应用程序的名称，在程序运行的监控界面可以看到名称
    //    conf.setMaster("spark://Master:7077") //此时，程序在Spark集群
    conf.setMaster("local[2]")
    //设置batchDuration时间间隔来控制Job生成的频率并且创建Spark Streaming执行的入口
    val ssc = new StreamingContext(conf, Seconds(5))
    ssc.checkpoint(".")
    val userClickLogsDStream = ssc.socketTextStream("localhost", 9999)
    val formattedUserClickLogsDStream = userClickLogsDStream.map(clickLog =>
      (clickLog.split(" ")(2) + "_" + clickLog.split(" ")(1), 1))
    // 任意一种商品在过去60秒钟被点击了多少次，由于key是类型category和item的组合体，所以接下来可把商品分成不同的类别，然后计算出每种商品和最热门的商品
    //    val categoryUserClickLogsDStream = formattedUserClickLogsDStream.reduceByKeyAndWindow((v1:Int, v2: Int) => v1 + v2,
    //      (v1:Int, v2: Int) => v1 - v2, Seconds(60), Seconds(20))
    //这里用_+_ , _-_,这种占位符的方式，简化上面的写法。这里得到的是tuple，过去60秒内每种商品被点击的次数
    val categoryUserClickLogsDStream = formattedUserClickLogsDStream.reduceByKeyAndWindow(_+_,
      _-_, Seconds(60), Seconds(20))
    // 在上面的基础之上用foreachRDD结合spark sql进行处理
    categoryUserClickLogsDStream.foreachRDD { rdd => {
      if (rdd.isEmpty()) {
          println("No data inputted!!!")//rdd为空直
        // 接不要干后面的所有事情
      } else {
        val categoryItemRow = rdd.map(reducedItem => {
          val category = reducedItem._1.split("_")(0)
          val item = reducedItem._1.split("_")(1)
          val click_count = reducedItem._2
          //返回Row
          Row(category, item, click_count)
        })
        //构建DataFrame用下面代码
        val structType = StructType(Array(
          StructField("category", StringType, true),
          StructField("item", StringType, true),
          StructField("click_count", IntegerType, true)
        ))

        val hiveContext = new HiveContext(rdd.context)
        //真正创建DataFrame
        val categoryItemDF = hiveContext.createDataFrame(categoryItemRow, structType)
        //注册一个表，用sql去操作
        categoryItemDF.registerTempTable("categoryItemTable")
        //拿到的结果是DataFrame
        val reseltDataFram = hiveContext.sql("SELECT category,item,click_count FROM (SELECT category,item,click_count,row_number()" +
          " OVER (PARTITION BY category ORDER BY click_count DESC) rank FROM categoryItemTable) subquery " +
          " WHERE rank <= 3")
        reseltDataFram.show()
        //把DF变成RDD
        val resultRowRDD = reseltDataFram.rdd

        resultRowRDD.foreachPartition { partitionOfRecords => {
          //这里必须做非空判断，否则为null的时候，会报错，而且rdd不为null，partition可能为null，因为不能确保60秒中的每5秒都有数据
            if (partitionOfRecords.isEmpty){
              println("This RDD is not null but partition is null")
            } else {
              partitionOfRecords.foreach(record =>{
                println(record.getAs("category"))
                println(record.getAs("item"))
                println(record.getAs("click_count"))
              }
            )
          // ConnectionPool is a static, lazily initialized pool of connections
            val connection = scalaConnectPool.getConnection
            partitionOfRecords.foreach(record => {
              //把结果插入数据库中
              val sql = "insert into categorytop3(category,item,client_count) values('" + record.getAs("category") + "','" +
                record.getAs("item") + "'," + record.getAs("click_count") + ")"
              val stmt = connection.createStatement();
              stmt.executeUpdate(sql);
            })
          }
         }
        }
      }
    }
    }
    /**
      * 在StreamingContext调用start方法的内部其实是会启动JobScheduler的Start方法，进行消息循环，在JobScheduler
      * 的start内部会构造JobGenerator和ReceiverTacker，并且调用JobGenerator和ReceiverTacker的start方法：
      *   1，JobGenerator启动后会不断的根据batchDuration生成一个个的Job
      *   2，ReceiverTracker启动后首先在Spark Cluster中启动Receiver（其实是在Executor中先启动ReceiverSupervisor），在Receiver收到
      *   数据后会通过ReceiverSupervisor存储到Executor并且把数据的Metadata信息发送给Driver中的ReceiverTracker，在ReceiverTracker
      *   内部会通过ReceivedBlockTracker来管理接受到的元数据信息
      * 每个BatchInterval会产生一个具体的Job，其实这里的Job不是Spark Core中所指的Job，它只是基于DStreamGraph而生成的RDD
      * 的DAG而已，从Java角度讲，相当于Runnable接口实例，此时要想运行Job需要提交给JobScheduler，在JobScheduler中通过线程池的方式找到一个
      * 单独的线程来提交Job到集群运行（其实是在线程中基于RDD的Action触发真正的作业的运行），为什么使用线程池呢？
      *   1，作业不断生成，所以为了提升效率，我们需要线程池；这和在Executor中通过线程池执行Task有异曲同工之妙；
      *   2，有可能设置了Job的FAIR公平调度的方式，这个时候也需要多线程的支持；
      *
      */
    ssc.start()
    ssc.awaitTermination()

  }
}