package main.scala.spark.sql

/*import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors*/
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by hadoop on 5/10/15.
 */
class OrderKmeans {

}
//spark.sql.OrderKmeans
object OrderKmeans {

  def main(args: Array[String]) {
   /* val conf = new SparkConf()
    conf.setMaster("spark://weekend01:7077").setAppName("kmeans")

    val sc = new SparkContext(conf)

    val hiveContext = new HiveContext(sc)
    import hiveContext.implicits._

    //先从hive中加载到日志数据
    hiveContext.sql("use saledata")

    hiveContext.sql("set spark.sql.shuffle.partitions=20")

    val data = hiveContext.sql("select a.orderlocation, sum(b.itemqty) totalqty,sum(b.itemamout) totalamount from tbl_stock a join tbl_stockdetail b on a.orderid=b.orderid group by a.orderlocation")

    //将日志数据转变成Vectors
    val parsedata = data.map{
      case Row(_,totalqty,totalamount) =>
        val features = Array[Double](totalqty.toString.toDouble,totalamount.toString.toDouble)
        Vectors.dense(features)
    }


    //用kmeans对样本向量进行训练得到模型
    val numcluster = 3
    val maxIterations = 20
    val model = KMeans.train(parsedata,numcluster,maxIterations)

    //用模型对我们到数据进行预测

    val resrdd = data.map{

      case Row(orderlocation,totalqty,totalamount) =>
        //提取到每一行到特征值
        val features = Array[Double](totalqty.toString.toDouble,totalamount.toString.toDouble)
        //将特征值转换成特征向量
        val linevector = Vectors.dense(features)
        //将向量输入model中进行预测，得到预测值
        val prediction = model.predict(linevector)

        //返回每一行结果String
        orderlocation + " " + totalqty + " " + totalamount + " " + prediction

    }

    resrdd.saveAsTextFile("hdfs://weekend01:9000/sparksql/kmeansout/")
*/
   // sc.stop()
  }


}
