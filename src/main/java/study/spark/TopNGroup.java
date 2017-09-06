package study.spark;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

/**
 * 使用Java的方式开发开发分组Top N程序
 * 实现根据key分组后，取出topN的代码
 */
public class TopNGroup {

	public static void main(String[] args) {
		/**
	       * 第1步：创建Spark的配置对象SparkConf，设置Spark程序的运行时的配置信息，
	       * 例如说通过setMaster来设置程序要链接的Spark集群的Master的URL,如果设置
	       * 为local，则代表Spark程序在本地运行，特别适合于机器配置条件非常差（例如
	       * 只有1G的内存）的初学者       * 
	       */
		SparkConf conf = new SparkConf().setAppName("TopNGroup");//.setMaster("local")
		
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

	//	JavaRDD<String> lines = sc.textFile("topn.txt");

		JavaRDD<String> lines = sc.textFile("hdfs://hadoop16:9000/topn.txt");

		//把每行数据变成符合要求的Key-Value的方式
		JavaPairRDD<String, Integer>  pairs =lines.mapToPair(new PairFunction<String, String, Integer>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Tuple2<String, Integer> call(String line) throws Exception {
				String[] splitedLine = line.split(" ");
				return new Tuple2<String, Integer>(splitedLine[0], Integer.valueOf(splitedLine[1]));
			}
		});
		
		JavaPairRDD<String, Iterable<Integer>> groupedPairs  = pairs.groupByKey(); //对数据进行分组

		JavaPairRDD<String, Iterable<Integer>> top5 = groupedPairs.mapToPair(new PairFunction<Tuple2<String,Iterable<Integer>>, String, Iterable<Integer>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Tuple2<String, Iterable<Integer>> call(Tuple2<String, Iterable<Integer>> groupedData) throws Exception {
				Integer[] top5 = new Integer[5];//保存Top5的数据本身
				String groupedKey = groupedData._1;	//获得分组的组名
				Iterator<Integer> groupedValue = groupedData._2.iterator(); //获取每组的内容集合
				while(groupedValue.hasNext()){	//查看是否有下一个元素，如果有则继续进行循环
					Integer value = groupedValue.next();	//获取当前循环的元素本身的内容
					for(int i = 0; i < 5; i++){	//具体实现分组内部的Top N
						if(top5[i] == null){
							top5[i] = value;
							break;
						} else if (value > top5[i]) {
							for(int j = 4; j > i; j--){
								top5[j] = top5[j-1];
							}
							top5[i] = value;
							break;
						}
					}
				}
				return new Tuple2<String, Iterable<Integer>> (groupedKey, Arrays.asList(top5));
			}
		});
		
		//打印分组后的Top N
		top5.foreach(new VoidFunction<Tuple2<String,Iterable<Integer>>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void call(Tuple2<String, Iterable<Integer>> topped) throws Exception {
				System.out.println("Group Key : " + topped._1);	//获取Group Key
				Iterator<Integer> toppedValue = topped._2.iterator();	//获取Group Value
				while(toppedValue.hasNext()){	//具体打印出每组的Top N
					Integer value = toppedValue.next();
					System.out.println(value);
				}
				System.out.println("*****************************************************");
			}
		});
		sc.close();
		
	}

}
