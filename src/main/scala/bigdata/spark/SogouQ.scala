package main.scala.bigdata.spark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

class SogouQ {

}
object SogouQ {
  
  def main(args: Array[String]): Unit = {
    val sb = new StringBuilder
    val masterurl = "spark://weekend01:7077"
    val Array(input,wcoutput,ffoutput) = args
    val conf = new SparkConf
    conf.setMaster(masterurl).setAppName("sogouq")
    val sc = new SparkContext(conf)
    val file = sc.textFile(input)
    file.cache()
    val count = file.count()
    println("文件总共有" + count + "行")
    
    val wcrdd = file.map { x => x.split("\t") }.filter { x => x(2).contains("汶川地震") }.map { x => x.mkString("\t") }
    
    val count_wc = wcrdd.count
    println("搜索关键字中包含汶川地震的行数为： " + count_wc)
    wcrdd.saveAsTextFile(wcoutput)
    
    val ffrdd = file.map { x => x.split("\t") }.filter { x => x(3).equals("1 1") }.map { x => x.mkString("\t") }
    val count_11 = ffrdd.count
    println("结果排序和用户点击都为第一位的记录条数为： " + count_11)
    ffrdd.saveAsTextFile(ffoutput)

    sc.stop()

  }

}