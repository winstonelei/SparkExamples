package main.scala.spark.sql

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row


import scala.collection.mutable.ArrayBuffer

/**
 * Created by hadoop on 5/9/15.
 */
class TestSparkSql {

}
case class Person(name:String,age:Int)

object TestSparkSql {


  def main(args: Array[String]): Unit = {

    //首先创建sparkContext
    val conf = new SparkConf()
    conf.setMaster("local[1]").setAppName("testsql")
    val sc = new SparkContext(conf)

    //再创建sqlContext，它说整个sparksql的入口
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    //接着加载表数据（创建DataFram）
    //创建datafram之前，需要先定义schema,有两种方式
    //第一种：通过casecalss的反射机制来定义

    val personrdd = sc.textFile("file:///home/hadoop/sourcedatas/data/people.txt").map(_.split(",")).map(x=>Person(x(0),x(1).trim.toInt))
    val personDF = personrdd.toDF

    personDF.printSchema()
    personDF.show()


    println("-------------------")

    //第二种方式:通过StructType来定义schema

    val fields = new ArrayBuffer[StructField]()
    fields += StructField("name",StringType,true)
    fields += StructField("age",IntegerType,true)
    val schema = new StructType(fields.toArray)

    val rowRdd = sc.parallelize(List("zhangsan,18","lisi,28","wangwu,38","angelababy,18")).map(_.split(","))
    .map(x=>Row(x(0),x(1)))

    //sqlcontext可以针对RowRDD和指定到schema来创建DataFram
    val personDF2 = sqlContext.createDataFrame(rowRdd,schema)
    personDF2.printSchema()
    personDF2.show()

    sc.stop()

  }



}


