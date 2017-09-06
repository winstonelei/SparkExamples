package bigdata.spark.scala.cn.zte

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

 /**
  * Created by winstone on 2017/8/29.
  */
object ReduceTest {

  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("reduce app").setMaster("local[4]")

    var sc = new SparkContext(conf)

    var rdd = sc.parallelize(Array(("xiaoming",1),("zhangsan",2),("xiaoming",3)))

    val result = reduceByTest(sc,rdd)

    println(result)

    sc.stop()

  }

  def reduceByTest(sc:SparkContext,rdd:RDD[(String,Int)]): (String, Int) ={

    val rdd2= rdd.reduce{
         (a,b)=>
         if(a._1 == b._1){
           (a._1,a._2+b._2)
         }else{
           a
         }
    }
     rdd2
  }



}
