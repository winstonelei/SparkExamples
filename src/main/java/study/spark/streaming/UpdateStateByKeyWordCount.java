package study.spark.streaming;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import com.google.common.base.Optional;

import scala.Tuple2;

public class UpdateStateByKeyWordCount {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("wordcount").setMaster("local[2]");
		JavaStreamingContext jssc = new JavaStreamingContext(conf,Durations.seconds(5));
		
		// 第一点,如果要使用updateStateByKey算子,就必须设置一个checkpoint目录,开启checkpoint机制
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
		
		// updateStateByKey,就可以实现直接通过spark维护一份每个单词的全局的统计次数
		JavaPairDStream<String, Integer> wordcounts = pairs.updateStateByKey(
				
				// 这里的Optional,相当于scala中的样例类,就是Option,可以理解它代表一个状态,可能之前存在,也可能之前不存在
				new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>(){

					private static final long serialVersionUID = 1L;

					// 实际上,对于每个单词,每次batch计算的时候,都会调用这个函数,第一个参数,values相当于这个batch中,这个key的新的值,
					// 可能有多个,比如一个hello,可能有2个1,(hello,1) (hello,1) 那么传入的是(1,1)
					// 那么第二个参数表示的是这个key之前的状态,其实泛型的参数是你自己指定的
					@Override
					public Optional<Integer> call(List<Integer> values,	Optional<Integer> state) throws Exception {
						// 定义一个全局的计数
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
		
		wordcounts.print();
		
		jssc.start();
		jssc.awaitTermination();
		jssc.close();
	}
}
