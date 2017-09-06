package study.spark;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

public class CogroupOps {

	public static void main(String[] args) {
		 /**
	       * 第1步：创建Spark的配置对象SparkConf，设置Spark程序的运行时的配置信息，
	       * 例如说通过setMaster来设置程序要链接的Spark集群的Master的URL,如果设置
	       * 为local，则代表Spark程序在本地运行，特别适合于机器配置条件非常差（例如
	       * 只有1G的内存）的初学者       * 
	       */
		SparkConf conf = new SparkConf().setAppName("Cogroup Transformation").setMaster("local");
		/**
	       * 第2步：创建SparkContext对象
	       * SparkContext是Spark程序所有功能的唯一入口，无论是采用Scala、Java、Python、R等都必须有一个SparkContext(不同的语言具体的类名称不同，如果是Java的话则为JavaSparkContext)
	       * SparkContext核心作用：初始化Spark应用程序运行所需要的核心组件，包括DAGScheduler、TaskScheduler、SchedulerBackend
	       * 同时还会负责Spark程序往Master注册程序等
	       * SparkContext是整个Spark应用程序中最为至关重要的一个对象
	       */
		JavaSparkContext sc = new JavaSparkContext(conf); //其底层实际上就是Scala的SparkContext
		/**
	       * 第3步：根据具体的数据来源（HDFS、HBase、Local FS、DB、S3等）通过JavaSparkContext来创建JavaRDD
	       * JavaRDD的创建基本有三种方式：根据外部的数据来源（例如HDFS）、根据Scala集合、由其它的RDD操作
	       * 数据会被RDD划分成为一系列的Partitions，分配到每个Partition的数据属于一个Task的处理范畴
	       */
		List<Tuple2<Integer,String>> namesList = Arrays.asList(
				new Tuple2<Integer, String>(1, "Spark"),
				new Tuple2<Integer, String>(2, "Tachyon"),
				new Tuple2<Integer, String>(3, "Hadoop")
				);
		List<Tuple2<Integer,Integer>> scoresList = Arrays.asList(
				new Tuple2<Integer, Integer>(1, 100),
				new Tuple2<Integer, Integer>(2, 90),
				new Tuple2<Integer, Integer>(3, 70),
				new Tuple2<Integer, Integer>(1, 110),
				new Tuple2<Integer, Integer>(2, 95),
				new Tuple2<Integer, Integer>(2, 60)
				);
		
		JavaPairRDD<Integer, String>  names = sc.parallelizePairs(namesList);
		JavaPairRDD<Integer, Integer>  scores = sc.parallelizePairs(scoresList);

		/**
	        * 第4步：对初始的JavaRDD进行Transformation级别的处理，例如map、filter等高阶函数等的编程，来进行具体的数据计算
	       * 	第4.1步：讲每一行的字符串拆分成单个的单词
	       */
		 JavaPairRDD<Integer, Tuple2<Iterable<String>, Iterable<Integer>>> nameScores = names.cogroup(scores);

		 nameScores.foreach(new VoidFunction<Tuple2<Integer,Tuple2<Iterable<String>,Iterable<Integer>>>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void call(Tuple2<Integer, Tuple2<Iterable<String>, Iterable<Integer>>> t) throws Exception {
				System.out.println("ID: " + t._1);
				System.out.println("Name: " + t._2._1);
				System.out.println("Score: " + t._2._2);
				System.out.println("==============================================================================");
			}
		});

		sc.close();
	}

}
