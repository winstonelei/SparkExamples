package main.scala.spark.sql

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by hadoop on 5/10/15.
 */
class OrderStastic {

}
object OrderStastic {




  def main(args: Array[String]): Unit = {


    val conf = new SparkConf
    conf.setMaster("spark://spark135:7077").setAppName("orderstatistic2")

    val sc = new SparkContext(conf)

    val hiveContext = new HiveContext(sc)
    import hiveContext.implicits._

    //统计每年的订单总数及销售总额
    hiveContext.sql("use wisedu")

    val start1 = System.currentTimeMillis()
    val res2 = hiveContext.sql("select * from wisedu.frontend")
    res2.show()
    val end1 = System.currentTimeMillis()

/*    val res = hiveContext.sql("select c.theyear,count(distinct a.orderid),sum(b.itemamout) from tbl_stock a join tbl_stockdetail b on a.orderid = b.orderid join tbl_date c on a.dateid = c.dateid group by c.theyear order by c.theyear")
    res.show
    res.saveAsTable("order_amount_per_year");*/

    //cacheTable是一个lazy操作，需要在action运算之后才进行缓存
/*    hiveContext.cacheTable("tbl_date")
    hiveContext.cacheTable("tbl_stock")
    hiveContext.cacheTable("tbl_stockdetail")

    val start1 = System.currentTimeMillis()
    val res2 = hiveContext.sql("select c.theyear,max(d.sumofamount) from tbl_date c join (select a.dateid,a.orderid,sum(b.itemamout) as sumofamount from tbl_stock a join tbl_stockdetail b on a.orderid=b.orderid group by a.dateid,a.orderid ) d  on c.dateid=d.dateid group by c.theyear sort by c.theyear")
    res2.show()
    val end1 = System.currentTimeMillis()

    println( (end1-start1) +"-----------------------")


    val start2 = System.currentTimeMillis()
    val res3 = hiveContext.sql("select c.theyear,max(d.sumofamount) from tbl_date c join (select a.dateid,a.orderid,sum(b.itemamout) as sumofamount from tbl_stock a join tbl_stockdetail b on a.orderid=b.orderid group by a.dateid,a.orderid ) d  on c.dateid=d.dateid group by c.theyear sort by c.theyear")
    res3.show()
    val end2 = System.currentTimeMillis()

    println( (end2-start2) +"-----------------------")


    val start3 = System.currentTimeMillis()
    val res4 =  hiveContext.sql("select distinct  e.theyear,e.itemid,f.maxofamount from (select c.theyear,b.itemid,sum(b.amount) as sumofamount from tblStock a join tblStockDetail b on a.ordernumber=b.ordernumber join tbldate c on a.dateid=c.dateid group by c.theyear,b.itemid) e join (select d.theyear,max(d.sumofamount) as maxofamount from (select c.theyear,b.itemid,sum(b.amount) as sumofamount from tblStock a join tblStockDetail b on a.ordernumber=b.ordernumber join tbldate c on a.dateid=c.dateid group by c.theyear,b.itemid) d group by d.theyear) f on (e.theyear=f.theyear and e.sumofamount=f.maxofamount) order by e.theyear")
    res4.save("hdfs://weekend01:9000/sparksql/orderstastic/populateitem/")
    val end3 = System.currentTimeMillis()

    val times = sc.parallelize(Seq[Long](end1-start1,end2-start2,end3-start3))
    times.saveAsTextFile("hdfs://weekend01:9000/sparksql/orderstastic/times")*/

    sc.stop

  }

}
