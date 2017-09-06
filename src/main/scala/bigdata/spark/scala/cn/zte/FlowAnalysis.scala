package main.scala.bigdata.spark.scala.cn.zte

import org.apache.spark.{SparkConf, SparkContext}
/**
 *
 * 对日志中的手机用户流量数据汇总，排序，输出结果文件
 *
 *
 */
class FlowAnalysis {

}

object FlowAnalysis {

  def main(args: Array[String]): Unit = {

    val masterurl = "local[1]"
    val input = "c:/flow/srcdata/"
    val output = "c:/flow/output/"

    val conf = new SparkConf
    conf.setMaster(masterurl).setAppName("flow")
    val sc = new SparkContext(conf)

    //加载文件创建rdd
    val linerdd = sc.textFile(input).map { x => x.split("\t") }
    linerdd.cache()

    //提取流量数据,rdd[(phone:string,(upFlow:long,dFlow:long))]
    val sumdata = linerdd.map { x => (x(1), (x(x.length - 3).toLong, x(x.length - 2).toLong)) }
      .reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2))

    val sumdata2 = sumdata.map(x=>(x._1,x._2._1,x._2._2,x._2._1+x._2._2))

    val sumdata3 = sumdata2.map(x=>(x._4,x)).sortByKey(false).map(x=>x._2)

    sumdata3.saveAsTextFile(output)

    sc.stop()

  }

}