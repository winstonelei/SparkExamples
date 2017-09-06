package main.scala.spark.clustering
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}
/**
  * Created by stone on 2016/8/25.
  *
  *
  */
object RFDTree {

  def main(args: Array[String]) {
    val conf = new SparkConf()                                     //创建环境变量
      .setMaster("local")                                             //设置本地化处理
      .setAppName("ZombieBayes")                              //设定名称
    val sc = new SparkContext(conf)
    val data = MLUtils.loadLibSVMFile(sc, "./src/main/scala/spark/clustering/DTree.txt")

    val numClasses = 2//分类数量
    val categoricalFeaturesInfo = Map[Int, Int]()//设定输入格式
    val numTrees = 3// 随机雨林中决策树的数目
    val featureSubSetStrategy = "auto" //设置属性在节点计算数,自动决定每个节点的属性数
    val impurity = "entropy" //设定信息增益计算方式
    val maxDepth = 5 //最大深度
    val maxBins = 3 // 设定分割数据集

    val model = RandomForest.trainClassifier(
      data,
      numClasses,
      categoricalFeaturesInfo,
      numTrees,
      featureSubSetStrategy,
      impurity,
      maxDepth,
      maxBins
    )// 建立模型

    model.trees.foreach(println)//打印每棵树信息
    println(model.numTrees)
    println(model.totalNumNodes)
  }
}
