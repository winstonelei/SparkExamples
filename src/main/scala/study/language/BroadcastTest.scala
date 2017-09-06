package study.language

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by winstone on 2017/7/25.
  */
object BroadcastTest {

  def main(args: Array[String]) {

     val bcName = if(args.length >2) args(2) else "Http"

     val blockSize = if(args.length > 3) args(3) else "4096"

     val sparkConf = new SparkConf().setAppName("BroadCast Test")
                                   .set("spark.broadcast.factory", s"org.apache.spark.broadcast.${bcName}BroadcastFactory")
                                   .set("spark.broadcast.blockSize", blockSize)
                                   .setMaster("local")

     val sc = new SparkContext(sparkConf)

     val slices = if(args.length>0)args(0).toInt else 2;

     val num = if(args.length >1) args(1).toInt else 10000

     val arr1 = (0 until num).toArray

     for(i <-0 to arr1.length){

       println("Iteration" + i)

       println("===========")
       val startTime = System.nanoTime
       val barr1 = sc.broadcast(arr1)

       val observedSizes =  sc.parallelize(1 to 10,slices).map(_ => barr1.value.size)

       println("开始输出结果")
       observedSizes.collect().foreach(i => println(i))
       println("结束输出")

       println("Iteration %d took %.0f milliseconds".format(i, (System.nanoTime - startTime) / 1E6))

     }

     sc.stop()


  }

}
