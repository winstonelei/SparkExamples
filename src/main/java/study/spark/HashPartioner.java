package study.spark;

import org.apache.spark.HashPartitioner;
import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by winstone on 2017/6/29.
 */
public class HashPartioner {

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("FlatMap").setMaster("local");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        List<Tuple2<Integer,String>> scoreList = Arrays.asList(new Tuple2<Integer,String>(1,"a")
                ,new Tuple2<Integer,String>(2,"b"),new Tuple2<Integer,String>(3,"c"),
                new Tuple2<Integer,String>(3,"d"), new Tuple2<Integer,String>(4,"e"),
                new Tuple2<Integer,String>(5,"f")
        );
        List<Tuple2<Integer,String>> scoreList2 = Arrays.asList(new Tuple2<Integer,String>(1,"Ad")
                ,new Tuple2<Integer,String>(2,"Bd"),new Tuple2<Integer,String>(3,"Cd"),
                new Tuple2<Integer,String>(3,"Dd"), new Tuple2<Integer,String>(4,"Ed")
        );
        JavaPairRDD<Integer,String> pair = ctx.parallelizePairs(scoreList);//.partitionBy(new HashPartitioner(3));
        JavaPairRDD<Integer,String> pair2 = ctx.parallelizePairs(scoreList2); //ctx.parallelizePairs(scoreList2,2);
        JavaPairRDD<Integer,Tuple2<String,String>> pair3=pair.join(pair2);

        Map<Integer,Tuple2<String,String>> map = pair3.collectAsMap();

   /*     pair.groupByKey(new Partitioner(){
            @Override
            public int numPartitions() {
                return 0;
            }

            @Override
            public int getPartition(Object key) {
                return 0;
            }
        });*/

        for(Integer key : map.keySet()){
            System.out.println(key+"="+map.get(key));
        }

  /*      pair3.foreach(new VoidFunction<Tuple2<Integer,Tuple2<String,String>>>() {
            @Override
            public void call(Tuple2<Integer, Tuple2<String, String>> t) throws Exception {
                System.out.println(t._1 +"==="+t._2._1+"---"+t._2._1);
            }
        });*/


    }
}
