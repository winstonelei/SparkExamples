package main.scala.bigdata.spark.scala.cn.zte

import org.apache.spark.{SparkConf, SparkContext}

class FlowAnalysis2 {
}

case class FlowBean(val phone:String, val upFlow:Long, val dFlow:Long){
  val sumFlow = upFlow + dFlow
}


object FlowAnalysis2 {

  def main(args: Array[String]): Unit = {
    val masterurl = "spark://weekend01:7077"
    val input = "hdfs://weekend01:9000/flow/srcdata/"
    val output = "hdfs://weekend01:9000/flow/output/"

    val conf = new SparkConf
    conf.setMaster(masterurl).setAppName("flow")
    val sc = new SparkContext(conf)

    //加载文件创建rdd
    val linerdd = sc.textFile(input).map { x => x.split("\t") }
    linerdd.cache()
    
    val beanrdd = linerdd.map { x => (x(1),FlowBean(x(1),x(x.length-3).toLong,x(x.length-2).toLong)) }
    val sumrdd = beanrdd.reduceByKey((bean1,bean2)=>FlowBean(bean1.phone,bean1.upFlow + bean2.upFlow,bean1.dFlow+bean2.dFlow))

    val sortrdd = sumrdd.map(x=>(x._2.sumFlow,x)).sortByKey(false)

    sortrdd.map(x=>x._2._2).saveAsTextFile(output);
    
    sc.stop
  }

}