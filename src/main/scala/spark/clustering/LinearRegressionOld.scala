package main.scala.spark.clustering
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * spark mllib LinearRegression(线性回归), 商品价格与消费者输入之间的关系
  * 商品需求(y, 吨),价格(x1, 元),消费者收入(x2, 元)
    y	x1	x2
    5	1	1
    8	1	2
    7	2	1
    13	2	3
    18	3	4
  建立需求函数: y = ax1+bx2
  * Created by stone on 2016/8/25.
  *  *　线性回归, 建立商品价格与消费者输入之间的关系,
  *  *  预测价格
  */
object LinearRegressionOld {
  def main(args : Array[String]){
    val conf = new SparkConf()     //创建环境变量
      .setMaster("local")        //设置本地化处理
      .setAppName("LinearRegression12")//设定名称
       val sc = new SparkContext(conf)  //创建环境变量实例
      val data = sc.textFile("./src/main/scala/spark/clustering/lr.txt")//获取数据集路径
      val parsedData = data.map { line =>	 //开始对数据集处理
          val parts = line.split('|') //根据逗号进行分区
          LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(',').map(_.toDouble)))
        }.cache() //转化数据格式



      //LabeledPoint,　numIterations, stepSize 线性回归根据随机梯度下降法实现
      val model = LinearRegressionWithSGD.train(parsedData, 4, 0.1) //建立模型

      val result = model.predict(Vectors.dense(1, 3))//通过模型预测模型
      println(model.weights)
      println(model.weights.size)
      println(result)	//打印预测结果

      /*val result1 = model.predict(Vectors.dense(2,2))
      println(model.weights)
      println(model.weights.size)
      println(result1)*/
  }
}
