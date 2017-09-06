package study.spark.streaming;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import com.google.common.base.Optional;

import scala.Tuple2;

public class PersistMySQLWordCount {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("wordcount").setMaster("local[2]");
		JavaStreamingContext jssc = new JavaStreamingContext(conf,Durations.seconds(5));
		
		jssc.checkpoint("hdfs://spark001:9000/wordcount_checkpoint");
		
		JavaReceiverInputDStream<String> lines = jssc.socketTextStream("spark001", 9999);
		
		JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>(){

			private static final long serialVersionUID = 1L;

			@Override
			public Iterable<String> call(String line) throws Exception {
				return Arrays.asList(line.split(" "));
			}
			
		});
		
		JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>(){

			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
				return new Tuple2<String, Integer>(word, 1);
			}
			
		});
		
		JavaPairDStream<String, Integer> wordcounts = pairs.updateStateByKey(
				
				new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>(){

					private static final long serialVersionUID = 1L;

					@Override
					public Optional<Integer> call(List<Integer> values,
							Optional<Integer> state) throws Exception {
						Integer newValue = 0;
						if(state.isPresent()){
							newValue = state.get();
						}
						for(Integer value : values){
							newValue += value;
						}
						return Optional.of(newValue);
					}
		});
		
		// 每次得到当前所有单词的统计计数之后,讲其持久化以便于后续J2EE程序显示
		wordcounts.foreachRDD(new VoidFunction<JavaPairRDD<String,Integer>>(){

			private static final long serialVersionUID = 1L;

			@Override
			public void call(JavaPairRDD<String, Integer> wordcountsRDD)
					throws Exception {
				wordcountsRDD.foreachPartition(new VoidFunction<Iterator<Tuple2<String,Integer>>>(){

					private static final long serialVersionUID = 1L;

					@Override
					public void call(Iterator<Tuple2<String, Integer>> wordcounts)
							throws Exception {
					/*	Connection conn = ConnectionPool.getConnection();
						Tuple2<String,Integer> wordcount = null;
						while(wordcounts.hasNext()){
							wordcount = wordcounts.next();
							String sql = "insert into wordcount(word,count) "
									+ "values('" + wordcount._1 + "'," + wordcount._2 + ")";
							Statement stmt = conn.createStatement();
							stmt.executeUpdate(sql);
						}
						ConnectionPool.returnConnection(conn);*/
					}
				});
			}
			
		});
		
		jssc.start();
		jssc.awaitTermination();
		jssc.close();
	}
}
