package main.scala.bigdata.spark.scala.cn.zte

import java.io.File
import java.util

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer
import scala.io.Source


/**
 * 要提交到集群运行，需要进行一下步骤操作：
 * 1、代码中应该在sparkconf中指定masterurl
 * 2、再将程序打成jar包
 * 3、将jar包上传到服务器
 * 4、用spark/bin下的spark-submit脚本进行提交
 * bin/spark-submit  --class cn.bigdata.spark.WordcountDemo \
 * --master spark://weekend01:7077  \
 * --deploy-mode cluster
 * <jar-path>
 * <main-method-params>
 * 
 */
object WordcountDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf
    conf.setMaster("local[2]").setAppName("wordcount")
    val sc = new SparkContext(conf)
    val data = "F:\\tmp\\a.txt"
    //val result = "f://b.txt"

    var res=sc.textFile(data).flatMap { x => x.split(" ") }.map{ x => (x,1)}.reduceByKey(_+_)
    .map(x=>(x._2,x._1))
      .sortByKey().map(x=>(x._2,x._1)).saveAsTextFile("F:\\tmp\\result.txt")


/*    val raw = List(("a",1),("b",2),("c",3))
    val res1 = raw.map{case (key,value) => value}.reduce(_ + _)
    println(res1)*/


    val a = sc.parallelize(1 to 9, 3)
    def doubleFunc(iter: Iterator[Int]) : Iterator[(Int,Int)] = {
      var res = List[(Int,Int)]()
      while (iter.hasNext)
      {
        val cur = iter.next;
        res .::= (cur,cur*2)
      }
      res.iterator
    }


    def doubleFunc1(iter:Iterator[Int]) : Iterator[(Int,Int)] ={
      var resultList = new ListBuffer[(Int,Int)]
      while(iter.hasNext){
         val cur = iter.next()
         resultList.append((cur,cur*2))
      }
      resultList.iterator
    }

    val result = a.mapPartitions(doubleFunc)
    println(result.collect().mkString)


    def tokenOut(fileName:String):util.Map[String,Int]={
      val tokens = Source.fromFile(fileName).mkString.split(",")
      val map =new util.HashMap[String,Int]()
      for(token <- tokens){map.get(token,0)+1}
      map
    }

/*    val conf = new SparkConf().setAppName("SimpleApp").setMaster("local")
    val sc = new SparkContext(conf)*/

    /*
      val logFile=sc.textFile("qingshu.txt")
      val numAs = logFile.filter(line => line.contains("a")).count()
      println(numAs)*/


    /*var res=logFile.flatMap { x => x.split(" ") }.map { x => (x,1) }.reduceByKey(_+_)
      .map(x=>(x._2,x._1)).sortByKey().map(x=>(x._2,x._1))//.saveAsTextFile("result.txt")

    //res.foreach(println)
    res.collect().foreach(println)
*/

/*    val rdd1 = sc.parallelize(Array("a b c", "d e f", "h i j"))
    //将rdd1里面的每一个元素先切分在压平
    val rdd2 = rdd1.flatMap(_.split(' '))
    rdd2.collect().foreach(x=>println(x))*/


/*
    val rdd1 = sc.parallelize(List(5, 6, 4, 3))
    val rdd2 = sc.parallelize(List(1, 2, 3, 4))
    //求并集
    val rdd3 = rdd1.union(rdd2)
    //求交集
    val rdd4 = rdd1.intersection(rdd2)
    //去重
    rdd3.distinct.collect().foreach(x=>println(x))

    println("===================>>>开始")

    rdd4.collect().foreach(x=>println(x))*/


/*    val rdd1 = sc.parallelize(List(("tom", 1), ("jerry", 3), ("kitty", 2)))
    val rdd2 = sc.parallelize(List(("jerry", 2), ("tom", 2), ("shuke", 2)))
    //求jion
    val rdd3 = rdd1.join(rdd2)
    println(rdd3.collect().foreach(tuple=>print(tuple._1+"=="+tuple._2)))
    //求并集
    val rdd4 = rdd1 union rdd2
    //按key进行分组
    rdd4.groupByKey
    println(rdd4.collect().foreach(tuple=>println(tuple._1 +",,,"+ tuple._2)))*/



/*    val rdd1 = sc.parallelize(List(("tom", 1), ("jerry", 3), ("kitty", 2),  ("shuke", 1)))
    val rdd2 = sc.parallelize(List(("jerry", 2), ("tom", 3), ("shuke", 2), ("kitty", 5)))
    val rdd3 = rdd1.union(rdd2)
    rdd3.foreach(x=>println(x._1+".."+x._2))
    println("rdd3...")
    //按key进行聚合
    val rdd4 = rdd3.reduceByKey(_ + _)
    rdd4.foreach(x=>println(x._1+".."+x._2))
    println("rdd4...")
    rdd4.collect
    //按value的降序排序
    val rdd5 = rdd4.map(t => (t._2, t._1)).sortByKey(false).map(t => (t._2, t._1))
    rdd5.collect().foreach(tuple=>println(tuple._1+".."+tuple._2))*/

/*
    val rdd1 = sc.parallelize(List(1, 2, 3, 4, 5))
    //reduce聚合
    val rdd2 = rdd1.reduce(_ + _)
    println(rdd2)
*/

/*
    val rdd1 = sc.parallelize(List(("tom", 1), ("tom", 2), ("jerry", 3), ("kitty", 2)))
    val rdd2 = sc.parallelize(List(("jerry", 2), ("tom", 1), ("shuke", 2)))
    //cogroup
    val rdd3 = rdd1.cogroup(rdd2)
    //注意cogroup与groupByKey的区别
    rdd3.collect().foreach(tuple=>println(tuple._1+"----"+tuple._2))
*/


    sc.stop()
    
  }

}