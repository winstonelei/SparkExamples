package study.spark.streaming;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.*;


public class SparkStreamingOnHDFS {
    public static void main(String[] args){
        /**
         * 第一步：配置SparkConf
         * 1. 至少两条线程：
         * 因为Spark Streaming应用程序在运行的时候，至少有一条线程用于不断的循环接收数据，
         * 并且至少有一条线程用于处理接收的数据（否则的话无法有线程用于处理数据，随着时间的推移，内存和磁盘都不堪重负）
         * 2. 对于集群而言，每个Executor一般而言肯定不止一个线程，对于处理Spark Streaming的应用程序而言，每个Executor一般
         * 分配多少个Core合适呢？根据我们过去的经验，5个左右的core是最佳的（分配为奇数个Core为最佳）。
         */
        final SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("SparkOnStreamingOnHDFS");
        /**
         * 第二步：创建SparkStreamingContext，这个是Spark Streaming应用程序所有功能的起始点和程序调度的核心
         * 1，SparkStreamingContext的构建可以基于SparkConf参数，也可以基于持久化SparkStreamingContext的内容
         * 来恢复过来(典型的场景是Driver崩溃后重新启动，由于Spark Streaming具有连续7*24小时不间断运行的特征，
         * 所有需要在Driver重新启动后继续上一次的状态，此时状态的恢复需要基于曾经的checkpoint)
         * 2，在一个Spark Streaming应用程序中可以创建若干个SparkStreamingContext对象，使用下一个SparkStreamingContext
         * 之前需要把前面正在运行的SparkStreamingContext对象关闭掉，由此，我们获得一个重大启发：SparkStreamingContext
         * 是Spark core上的一个应用程序而已，只不过Spark Streaming框架箱运行的话需要Spark工程师写业务逻辑
         */
//        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));//Durations.seconds(5)设置每隔5秒

        final String checkpointDirectory = "hdfs://hadoop16:9000/checkpoint";
        JavaStreamingContextFactory factory = new JavaStreamingContextFactory() {
            @Override
            public JavaStreamingContext create() {
                return createContext(checkpointDirectory,conf);
            }
        };
        /**
         * 可以从失败中恢复Driver，不过还需要制定Driver这个进程运行在Cluster，并且提交应用程序的时候
         * 指定 --supervise;
         */
        JavaStreamingContext jsc = JavaStreamingContext.getOrCreate(checkpointDirectory, factory);
        /**
         * 现在是监控一个文件系统的目录
         * 此处没有Receiver，Spark Streaming应用程序只是按照时间间隔监控目录下每个Batch新增的内容(把新增的)
         * 作为RDD的数据来源生成原始的RDD
         */

        Set<String> topicSet = new HashSet<String>();

        topicSet.add("order");

        Map<String,String> kafkaMap = new HashMap<String,String>();

        kafkaMap.put("metadata.broker.list", "172.16.206.17:9092,172.16.206.31:9092,172.16.206.32:9092");

/*        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(sparkConf,new Duration(2000));

        javaStreamingContext.checkpoint(CHECKPOINT_DIR);*/


        JavaPairInputDStream<String,String> message = KafkaUtils.createDirectStream(jsc,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                kafkaMap,
                topicSet);



        JavaDStream<String> javaDstream = message.map(new Function<Tuple2<String,String>, String>() {

            @Override
            public String call(Tuple2<String, String> stringStringTuple2) throws Exception {
                System.out.println("接受到的数据="+stringStringTuple2._2());
                return stringStringTuple2._2();
            }
        });


        javaDstream.count().print();

        //指定从HDFS中监控的目录
     //   JavaDStream lines = jsc.textFileStream("hdfs://Master:9000/library/SparkStreaming/Data");
        /**
         * 第四步：接下来就像对于RDD编程一样基于DStreaming进行编程！！！
         * 原因是：
         *  DStreaming是RDD产生的模板（或者说类）。
         *  在Spark Streaming具体发生计算前其实质是把每个batch的DStream的操作翻译成对RDD的操作！！
         *  对初始的DStream进行Transformation级别的处理，例如Map,filter等高阶函数的编程，来进行具体的数据计算。
         *  第4.1步：将每一行的字符串拆分成单个单词
         */
  /*      JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String,String>() {
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });*/
      /*  *//**
         * 第4.2步：对初始的JavaRDD进行Transformation级别的处理，例如map，filter等高阶函数等的编程，来进行具体的数据计算
         * 在4.1的基础上，在单词拆分的基础上对每个单词实例计数为1，也就是word => (word,1)
         *//*
        JavaPairDStream<String,Integer> pairs  = jsc.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String,Integer>(word,1);
            }
        });
        *//**
         * 第4.3步：在每个单词实例计数的基础上统计每个单词在文件中出现的总次数
         *//*
        JavaPairDStream<String,Integer> wordscount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        *//**
         * 此处的print并不会直接触发Job的执行，因为现在的一切都是在Spark Streaming框架控制下的，对于Spark而言具体是否
         * 触发真正的Job运行是基于设置的Duration时间间隔的
         * 一定要注意的是：Spark Streaming应用程序要想执行具体的Job，对DStream就必须有output Stream操作，
         * output Stream有很多类型的函数触发，例如：print，saveAsTextFile,saveAsHadoopFiles等，其实最为重要的一个方法是
         * foraeachRDD，因为Spark Streaming处理的结果一般都会放在Redis，DB，DashBoard等上面，foreachRDD主要就是用来完成这些
         * 功能的，而且可以随意的自定义具体数据到底存放在哪里！！！
         *//*
        wordscount.print();*/
        /**
         * Spark Streaming执行引擎也就是Driver开始运行，Driver启动的时候是位于一条新的线程中的。
         * 当然其内部有消息循环体用于接收应用程序本身或者Executor的消息；
         */
        jsc.start();
        jsc.awaitTermination();
        jsc.close();
    }
    /**
     * 工厂化模式构建JavaStreamingContext
     */
    private static JavaStreamingContext createContext(String checkpointDirectory,SparkConf conf){
        System.out.println("Creating new context");
        SparkConf sparkConf= conf;
        JavaStreamingContext ssc = new JavaStreamingContext(sparkConf,Durations.seconds(10));
        ssc.checkpoint(checkpointDirectory);
        return ssc;
    }
}