package study.spark;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;


public class SparkJoinExample {

	public static void main(String[] args) {

		SparkConf conf = new SparkConf().setAppName("SpariJoinExample").setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);
		List<Tuple2<String,String>> blackListTuple = new ArrayList<Tuple2<String, String>>();

		blackListTuple.add(new Tuple2<String,String>("101","cc"));
		blackListTuple.add(new Tuple2<String,String>("100","cc"));
		
		JavaPairRDD<String,String> rdd1 =  sc.parallelizePairs(blackListTuple);
		
		// 首先将其中一个key分布相对较为均匀的RDD膨胀100倍。
		JavaPairRDD<String, String> expandedRDD = rdd1.flatMapToPair(new PairFlatMapFunction<Tuple2<String,String>, String, String>() {
			@Override
			public Iterable<Tuple2<String, String>> call(Tuple2<String, String> t) throws Exception {
				  List<Tuple2<String, String>> list = new ArrayList<Tuple2<String, String>>();
	                for(int i = 0; i < 10; i++) {
	                    list.add(new Tuple2<String, String>(0 + "_" + t._1, t._2));
	                }
	                return list;
			}
		});
		
		
		expandedRDD.foreach(new VoidFunction<Tuple2<String,String>>() {
			@Override
			public void call(Tuple2<String, String> t) throws Exception {
				// TODO Auto-generated method stub
				System.out.println("begin"+"--"+t._1+"  "+t._2);
			}
		});

		List<Tuple2<Long,String>> blackListTuple2 = new ArrayList<Tuple2<Long, String>>();
		 
		blackListTuple2.add(new Tuple2<Long, String>(101L, "AB"));
		blackListTuple2.add(new Tuple2<Long, String>(100L, "AA"));
		

		JavaPairRDD<Long,String> rdd2 =  sc.parallelizePairs(blackListTuple2);


		// 其次，将另一个有数据倾斜key的RDD，每条数据都打上100以内的随机前缀。
		JavaPairRDD<String, String> mappedRDD = rdd2.mapToPair(
		        new PairFunction<Tuple2<Long,String>, String, String>() {
		            private static final long serialVersionUID = 1L;
		            @Override
		            public Tuple2<String, String> call(Tuple2<Long, String> tuple)
		                    throws Exception {
		                Random random = new Random();
		                int prefix = random.nextInt(100);
		                return new Tuple2<String, String>(prefix + "_" + tuple._1, tuple._2);
		            }
		        });

		
		
		mappedRDD.foreach(new VoidFunction<Tuple2<String,String>>() {
			@Override
			public void call(Tuple2<String, String> t) throws Exception {
				System.out.println("second"+"--"+t._1+"  "+t._2);
			}
			
		});




		// 将两个处理后的RDD进行join即可。
		JavaPairRDD<String, Tuple2<String, String>> joinedRDD = mappedRDD.join(expandedRDD);
		joinedRDD.foreach(new VoidFunction<Tuple2<String,Tuple2<String,String>>>() {

			@Override
			public void call(Tuple2<String, Tuple2<String, String>> t) throws Exception {
				// TODO Auto-generated method stub
				System.out.println(t._1+"="+t._2);
			}
		});
		
		
		System.out.println(joinedRDD.toString());
		System.out.println(joinedRDD.toDebugString());
	}

}
