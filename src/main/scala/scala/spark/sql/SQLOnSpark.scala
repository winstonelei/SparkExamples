package scala.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by winstone on 2017/8/30.
  */


case class  Person(name:String,age:Int)
object SQLOnSpark {

  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("SQLONSpark").setMaster("local[4]")

    val sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)

    import sqlContext._
    import sqlContext.implicits._

    val people:RDD[Person] = sc.textFile("F:\\test.text").map(_.split(",")).map(p => Person(p(0),p(1).trim().toInt))

    for(i <- people){
      println(i.name +" "+i.age)
    }

    val parquestFile = sqlContext.read.parquet("F:\\test.parquest")

    for(i <- parquestFile.take(10)){
        println(i)
    }

    parquestFile.show()

    parquestFile.registerTempTable("parquestWiki")

    val parquestQuery = sqlContext.sql("select * from parquestWiki")

    val takeArray = parquestQuery.take(10)

    val resultRdd = sc.makeRDD(takeArray)

    resultRdd.foreach(p=>{
       println(p)
    })

    parquestQuery.take(10).foreach(p=>
         println(p)
    )

    sc.stop()

  }

}
