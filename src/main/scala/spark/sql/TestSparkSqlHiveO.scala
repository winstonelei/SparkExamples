package main.scala.spark.sql

import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by hadoop on 5/9/15.
 */
class TestSparkSqlHiveO {

}

case class Pinfo(name:String,addr:String,phone:String)

object TestSparkSqlHiveO{

  def main(args: Array[String]) {

    val conf = new SparkConf()
    conf.setAppName("guiyi").setMaster("spark://weekend01:7077").setJars(Array("/home/hadoop/dev/sparktest/target/sparkdemo-1.0-SNAPSHOT.jar"))

    val sc = new SparkContext(conf)
    val rdd = sc.textFile("hdfs://weekend01:9000/sparksql/data/peopleinfo.data").map(_.split(","))
    rdd.collect().foreach(x=>println(x.mkString))

    sc.stop()
    println("lsakjflsakjdflksajdflaskdjflaskgflsakdjf")


  }

}
