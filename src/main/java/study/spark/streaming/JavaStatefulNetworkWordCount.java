package study.spark.streaming;

import com.google.common.base.Optional;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by dell on 2016/12/22.
 */
public class JavaStatefulNetworkWordCount {

    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) {
        //StreamingExamples.setStreamingLogLevels();

        // Create the context with a 1 second batch size
        String brokers = "114.55.253.15:9092,114.55.132.143:9092,114.55.252.185:9092";
        String topics = "kafka-demo,kafka-demo2";

        // Create context with a 2 seconds batch interval
        SparkConf sparkConf = new SparkConf().setAppName("JavaStatefulNetworkWordCount").setMaster("local[4]");
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(2));

        jssc.checkpoint("hdfs://hadoop26:9000/checkpoint");

        // Initial state RDD input to mapWithState
        @SuppressWarnings("unchecked")
        List<Tuple2<String, Integer>> tuples = Arrays.asList(new Tuple2<String, Integer>("zhangsan", 1),
                new Tuple2<String, Integer>("lisi", 1));
        JavaPairRDD<String, Integer> initialRDD = jssc.sparkContext().parallelizePairs(tuples);

        HashSet<String> topicsSet = new HashSet<String>(Arrays.asList(topics.split(",")));
        HashMap<String, String> kafkaParams = new HashMap<String, String>();
        kafkaParams.put("metadata.broker.list", brokers);

        // Create direct kafka stream with brokers and topics
        JavaPairInputDStream<String, String> messages = KafkaUtils.createDirectStream(
                jssc,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                kafkaParams,
                topicsSet
        );

        JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple2) {
                return tuple2._2();
            }
        });

        JavaPairDStream<String, Integer> wordsDstream = lines.mapToPair(
                new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(String s) {
                        return new Tuple2<String, Integer>(s, 1);
                    }
                });

        // Update the cumulative count function
        final Function3<String, Optional<Integer>, State<Integer>, Tuple2<String, Integer>> mappingFunc =
                new Function3<String, Optional<Integer>, State<Integer>, Tuple2<String, Integer>>() {

                    @Override
                    public Tuple2<String, Integer> call(String word, Optional<Integer> one, State<Integer> state) {
                        int sum = one.or(0) + (state.exists() ? state.get() : 0);
                        Tuple2<String, Integer> output = new Tuple2<String, Integer>(word, sum);
                        state.update(sum);
                        return output;
                    }
                };

        // DStream made of get cumulative counts that get updated in every batch
        JavaMapWithStateDStream<String, Integer, Integer, Tuple2<String, Integer>> stateDstream =
                wordsDstream.mapWithState(StateSpec.function(mappingFunc).initialState(initialRDD));

        stateDstream.print();
        jssc.start();
        jssc.awaitTermination();
    }
}
