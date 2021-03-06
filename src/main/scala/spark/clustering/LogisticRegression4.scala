package main.scala.spark.clustering
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}
/**
  *
  * 转移数据说明
  * 肾细胞转移情况(有转移 y=1,无转移 y=2)
  x1:确诊时患者年龄(岁)
  x2:肾细胞血管内皮生长因子(VEGF),其阳性表述由低到高共３个等级
  x3:肾细胞组织内微血管数(MVC)
  x4:肾细胞核组织学分级，由低到高共４级
  x5:肾细胞分期，由低到高共４级
  y x1 x2 x3 x4 x5
  0 59 2 43.4 2 1
  * 分类预测之逻辑回归
  * Created by stone on 2016/8/24.
  */
object LogisticRegression4 {
  val conf = new SparkConf() //创建环境变量
    .setMaster("local")      //设置本地化处理
    .setAppName("LogisticRegression4")//设定名称
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {
    val data = MLUtils.loadLibSVMFile(sc, "./src/main/scala/spark/clustering/wa.txt")	//读取数据文件,一定注意文本格式
    val splits = data.randomSplit(Array(0.7, 0.3), seed = 11L)	//对数据集切分
    val parsedData = splits(0)		//分割训练数据
    val parseTtest = splits(1)		//分割测试数据
    val model = LogisticRegressionWithSGD.train(parsedData,50)	//训练模型

    val predictionAndLabels = parseTtest.map {//计算测试值
      case LabeledPoint(label, features) =>	//计算测试值
        val prediction = model.predict(features)//计算测试值
        (prediction, label)			//存储测试和预测值
    }

    val metrics = new MulticlassMetrics(predictionAndLabels)//创建验证类
    val precision = metrics.precision			//计算验证值
    println("Precision = " + precision)	//打印验证值

    val patient = Vectors.dense(Array(70,3,180.0,4,3))	//计算患者可能性
    if(patient == 1) println("患者的胃病有几率转移。")//做出判断
    else println("患者的胃病没有几率转移。")	//做出判断
    //Precision = 0.3333333333333333
    //患者的胃病没有几率转移。
  }
}
