package study.language

import org.apache.spark.{SparkConf, SparkContext}



/**
  * Created by winstone on 2017/7/25.
  */
object HdfsTest {


  def main(args: Array[String]) {

    val sparkConf = new SparkConf().setMaster("local").setAppName("hdfstest")

    val sc = new SparkContext(sparkConf)

    val file = sc.textFile("hdfs://hadoop16:9000/topn.txt")

    val mapped = file.map(s=>s.length).cache()

    for(i <- 1 to 10){

      val start = System.currentTimeMillis()

      for(x <- mapped){
         x+2
      }

      val end = System.currentTimeMillis();

      println("Iteration " + i + " took " + (end-start) + " ms");


    }

    sc.stop();

  }
}
