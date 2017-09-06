package main.scala.spark.sql

import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.hive.HiveContext

/**
 * Created by hadoop on 5/9/15.
 */
class TestSparkSqlHive {

}

/**
 *
 * 1/首先要将hive-site.xml拷贝到spark到conf目录下
 * 2/在spark到lib目录中放一份mysql到驱动jar包
 * 3/在sparkconf中设置一下spakrhome（环境变量中有也可以）
 * 4/在代码中创建hivecontext作为访问hive数据到入口
 * 5/配置文件可以统一放到工程到claspath下，src/main/resources/{hdfs-site.xml,core-site.xml,hive-site.xml}
 *
 */

case class Peopleinfo(name: String, addr: String, phone: String)

object TestSparkSqlHive {

  def main(args: Array[String]) {

    val conf = new SparkConf()
    conf.setMaster("spark://spark135:7077").setAppName("sqlhive1")
      //.setSparkHome("/home/hadoop/app/spark-1.3.0")


    val sc = new SparkContext(conf)

    //    val infordd = sc.textFile("hdfs://weekend01:9000/sparksql/data/peopleinfo.data").map(_.split(","))
    //    infordd.collect.foreach(x=>println(x.mkString))


    val sqlContext = new SQLContext(sc)

    val hiveContext = new HiveContext(sc)
    import sqlContext.implicits._
//    import hiveContext.implicits._
    //可以直接查询hive到表数据
    //    hiveContext.sql("select * from people_hive_tbl").collect().foreach(println)

    //也可以将hive到表加载为sparksql中到基础数据类型 datafram
        val hTblDF = hiveContext.table("frontend")
        hTblDF.printSchema()
        val count = hTblDF.count()
        println(count + "-----------------")


    //加载一个hive以外到文件为一个sparksql到表
      //  val infoDF = sc.textFile("hdfs://weekend01:9000/sparksql/data/peopleinfo.data").map(_.split(",")).map(x=>Peopleinfo(x(0),x(1),x(2))).toDF()
    //    infordd.show()

//        val infoDF = infordd.toDF
       // infoDF.registerTempTable("infotbl")
//        infoDF.printSchema()
//        hiveContext.sql("select * from infotbl").show()


     /*   val resDF = hiveContext.sql("select a.name,a.age,b.addr,b.phone from people_hive_tbl a join infotbl b on a.name=b.name")
        resDF.save("hdfs://weekend01:9000/sparksql/joinjason/","json")*/
    //    resDF.collect.foreach(println)

    //    hTblDF.save("hdfs://weekend01:9000/ideahive/outjson/","json",SaveMode.Overwrite);
    //    hTblDF.save("hdfs://weekend01:9000/ideahive/outparquet/","parquet",SaveMode.Overwrite);

    //sparksql内建到数据源类型不包括testfile
    //    hTblDF.save("hdfs://weekend01:9000/ideahive/outtext/","text",SaveMode.Overwrite);

    sc.stop()


  }


}
