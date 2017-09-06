package study.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

/**
 * 二次排序，具体的实现步骤：
 * 第一步：按照Ordered和Serrializable接口实现自定义排序的Key
 * 第二步：将要进行二次排序的文件加载进来身材<Key,Value>类型的RDD
 * 第三步：使用sortByKey基于自定义的Key进行二次排序
 * 第四步：去除掉排序的Key，只保留排序的结果
 */
public class SecondarySortApp {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("SecondarySortApp").setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> lines = sc.textFile("sort.txt");
		JavaPairRDD<SecondarySortKey, String>  pairs = lines.mapToPair(new PairFunction<String, SecondarySortKey, String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Tuple2<SecondarySortKey, String> call(String line) throws Exception {
				String[] splited = line.split(" ");
				SecondarySortKey key = new SecondarySortKey(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]));
				return new Tuple2<SecondarySortKey, String>(key, line);
			}
		});
		
		JavaPairRDD<SecondarySortKey, String>  sorted = pairs.sortByKey(); //完成二次排序！！！
		
		//过滤掉排序后自定的Key，保留排序的结果
		JavaRDD<String> secondarySorted = sorted.map(new Function<Tuple2<SecondarySortKey,String>, String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String call(Tuple2<SecondarySortKey, String> sortedContent) throws Exception {
				return sortedContent._2;
			}
		});
		secondarySorted.foreach(new VoidFunction<String>() {
			@Override
			public void call(String sorted) throws Exception {
				System.out.println(sorted);
			}
		});
	}
}
