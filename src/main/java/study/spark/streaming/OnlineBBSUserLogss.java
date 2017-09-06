/*
       * 第99讲，消费者消费SparkStreamingDataManuallyProducerForKafka类中逻辑级别产生的数据，这里pv，uv，注册人数，跳出率的方式
       */

package study.spark.streaming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import kafka.serializer.StringDecoder;
import scala.Tuple2;

public class OnlineBBSUserLogss {

   public static void main(String[] args) {

      /*
       * 第99讲，消费者消费SparkStreamingDataManuallyProducerForKafka类中逻辑级别产生的数据，这里pv，uv，注册人数，跳出率的方式
       */
      /*SparkConf conf = new SparkConf().setMaster("local[2]").
            setAppName("WordCountOnline");*/

      SparkConf conf = new SparkConf().setMaster("local[5]").
              setAppName("OnlineBBSUserLogs");


      JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));


      Map<String, String> kafkaParameters = new HashMap<String, String>();
      kafkaParameters.put("metadata.broker.list","Master:9092,Worker1:9092,Worker2:9092");

      Set topics = new HashSet<String>();
      topics.add("UserLogs");


      JavaPairInputDStream<String, String> lines = KafkaUtils.createDirectStream(jsc,
              String.class,
              String.class,
              StringDecoder.class,StringDecoder.class,
              kafkaParameters,
              topics);

      //在线PV计算
      onlinePagePV(lines);
      //在线UV计算
      onlineUV(lines);
      //在线计算注册人数
      onlineRegistered(lines);
      //在线计算跳出率
      onlineJumped(lines);
      //在线不同模块的PV
      onlineChannelPV(lines);

      /*
       * Spark Streaming执行引擎也就是Driver开始运行，Driver启动的时候是位于一条新的线程中的，当然其内部有消息循环体，用于
       * 接受应用程序本身或者Executor中的消息；
       */
      jsc.start();

      jsc.awaitTermination();
      jsc.close();

   }

   private static void onlineChannelPV(JavaPairInputDStream<String, String> lines) {
      lines.mapToPair(new PairFunction<Tuple2<String,String>, String, Long>() {

         @Override
         public Tuple2<String, Long> call(Tuple2<String,String> t) throws Exception {
            String[] logs = t._2.split("\t");
            String channelID =logs[4];
            return new Tuple2<String,Long>(channelID, 1L);
         }
      }).reduceByKey(new Function2<Long, Long, Long>() { //对相同的Key，进行Value的累计（包括Local和Reducer级别同时Reduce）

         @Override
         public Long call(Long v1, Long v2) throws Exception {
            return v1 + v2;
         }
      }).print();

   }

   private static void onlineJumped(JavaPairInputDStream<String, String> lines) {
      lines.filter(new Function<Tuple2<String,String>, Boolean>() {
         @Override
         public Boolean call(Tuple2<String, String> v1) throws Exception {
            String[] logs = v1._2.split("\t");
            String action = logs[5];
            if("View".equals(action)){
               return true;
            } else {
               return false;
            }
         }
      }).mapToPair(new PairFunction<Tuple2<String,String>, Long, Long>() {

         @Override
         public Tuple2<Long, Long> call(Tuple2<String,String> t) throws Exception {
            String[] logs = t._2.split("\t");
            // Long usrID = Long.valueOf(logs[2] != null ? logs[2] : "-1");
            Long usrID = Long.valueOf("null".equals(logs[2])  ? "-1" : logs[2]);
            return new Tuple2<Long,Long>(usrID, 1L);
         }
      }).reduceByKey(new Function2<Long, Long, Long>() { //对相同的Key，进行Value的累计（包括Local和Reducer级别同时Reduce）

         @Override
         public Long call(Long v1, Long v2) throws Exception {
            return v1 + v2;
         }
      }).filter(new Function<Tuple2<Long,Long>, Boolean>() {

         @Override
         public Boolean call(Tuple2<Long, Long> v1) throws Exception {

            if(1 == v1._2){
               return true;
            } else {
               return false;
            }
         }
      }).count().print();


   }

   private static void onlineRegistered(JavaPairInputDStream<String, String> lines) {

      lines.filter(new Function<Tuple2<String,String>, Boolean>() {

         @Override
         public Boolean call(Tuple2<String, String> v1) throws Exception {
            String[] logs = v1._2.split("\t");
            String action = logs[5];
            if("Register".equals(action)){
               return true;
            } else {
               return false;
            }
         }
      }).count().print();

   }

   /**
    * 因为要计算UV，所以需要获得同样的Page的不同的User，这个时候就需要去重操作，DStreamzhong有distinct吗？当然没有（截止到Spark 1.6.1的时候还没有该Api）
    * 此时我们就需要求助于DStream魔术般的方法tranform,在该方法内部直接对RDD进行distinct操作，这样就是实现了用户UserID的去重，进而就可以计算出UV了。
    * @param lines
    */
   private static void onlineUV(JavaPairInputDStream<String, String> lines) {
      /*
       * 第四步：接下来就像对于RDD编程一样基于DStream进行编程！！！原因是DStream是RDD产生的模板（或者说类），在Spark Streaming具体
       * 发生计算前，其实质是把每个Batch的DStream的操作翻译成为对RDD的操作！！！
       *对初始的DStream进行Transformation级别的处理，例如map、filter等高阶函数等的编程，来进行具体的数据计算
        *
        */
      JavaPairDStream<String, String> logsDStream = lines.filter(new Function<Tuple2<String,String>, Boolean>() {

         @Override
         public Boolean call(Tuple2<String, String> v1) throws Exception {
            String[] logs = v1._2.split("\t");
            String action = logs[5];
            if("View".equals(action)){
               return true;
            } else {
               return false;
            }
         }
      });



      logsDStream.map(new Function<Tuple2<String,String>,String>(){

         @Override
         public String call(Tuple2<String, String> v1) throws Exception {
            String[] logs =v1._2.split("\t");
            Long usrID = Long.valueOf(logs[2] != null ? logs[2] : "-1");
            Long pageID = Long.valueOf(logs[3]);
            return pageID+"_"+usrID;
         }

      }).transform(new Function<JavaRDD<String>,JavaRDD<String>>(){

         @Override
         public JavaRDD<String> call(JavaRDD<String> v1) throws Exception {
            // TODO Auto-generated method stub
            return v1.distinct();
         }

      }).mapToPair(new PairFunction<String, Long, Long>() {

         @Override
         public Tuple2<Long, Long> call(String t) throws Exception {
            String[] logs = t.split("_");

            Long pageId = Long.valueOf(logs[0]);

            return new Tuple2<Long,Long>(pageId, 1L);
         }
      }).reduceByKey(new Function2<Long, Long, Long>() { //对相同的Key，进行Value的累计（包括Local和Reducer级别同时Reduce）

         @Override
         public Long call(Long v1, Long v2) throws Exception {
            return v1 + v2;
         }
      }).print();

   }

   private static void onlinePagePV(JavaPairInputDStream<String, String> lines) {
      /*
       * 第四步：接下来就像对于RDD编程一样基于DStream进行编程！！！原因是DStream是RDD产生的模板（或者说类），在Spark Streaming具体
       * 发生计算前，其实质是把每个Batch的DStream的操作翻译成为对RDD的操作！！！
       *对初始的DStream进行Transformation级别的处理，例如map、filter等高阶函数等的编程，来进行具体的数据计算
        *
        */
      JavaPairDStream<String, String> logsDStream = lines.filter(new Function<Tuple2<String,String>, Boolean>() {

         @Override
         public Boolean call(Tuple2<String, String> v1) throws Exception {
            String[] logs = v1._2.split("\t");
            String action = logs[5];
            if("View".equals(action)){
               return true;
            } else {
               return false;
            }
         }
      });

       /*
          * 第四步：对初始的DStream进行Transformation级别的处理，例如map、filter等高阶函数等的编程，来进行具体的数据计算
          *
          */
      JavaPairDStream<Long, Long> pairs = logsDStream.mapToPair(new PairFunction<Tuple2<String,String>, Long, Long>() {
         @Override
         public Tuple2<Long, Long> call(Tuple2<String, String> t) throws Exception {
            String[] logs = t._2.split("\t");

            Long pageId = Long.valueOf(logs[3]);

            return new Tuple2<Long,Long>(pageId, 1L);
         }
      });

       /*
          * 第四步：对初始的DStream进行Transformation级别的处理，例如map、filter等高阶函数等的编程，来进行具体的数据计算
          *
          */
      JavaPairDStream<Long, Long> wordsCount = pairs.reduceByKey(new Function2<Long, Long, Long>() { //对相同的Key，进行Value的累计（包括Local和Reducer级别同时Reduce）

         @Override
         public Long call(Long v1, Long v2) throws Exception {
            return v1 + v2;
         }
      });

      /*
       * 此处的print并不会直接出发Job的执行，因为现在的一切都是在Spark Streaming框架的控制之下的，对于Spark Streaming
       * 而言具体是否触发真正的Job运行是基于设置的Duration时间间隔的
       *
       * 诸位一定要注意的是Spark Streaming应用程序要想执行具体的Job，对Dtream就必须有output Stream操作，
       * output Stream有很多类型的函数触发，类print、saveAsTextFile、saveAsHadoopFiles等，最为重要的一个
       * 方法是foraeachRDD,因为Spark Streaming处理的结果一般都会放在Redis、DB、DashBoard等上面，foreachRDD
       * 主要就是用用来完成这些功能的，而且可以随意的自定义具体数据到底放在哪里！！！
       *
       * 在企業生產環境下，一般會把計算的數據放入Redis或者DB中，采用J2EE等技术进行趋势的绘制等，这就像动态更新的股票交易一下来实现
       *  在线的监控等；
       */
      wordsCount.print();
   }

}
/**
 * 论坛数据自动生成代码，该生成的数据会作为Producer的方式发送给Kafka，然后SparkStreaming程序会从
 * Kafka中在线Pull到论坛或者网站的用户在线行为信息，进而进行多维度的在线分析
 * 这里产生数据，就会发送给kafka，kafka那边启动消费者，就会接收到数据，这一步是用来测试生成数据和消费数据没有问题的，确定没问题后要关闭消费者，
 * 启动99讲的类作为消费者，就会按pv，uv等方式处理这些数据。因为一个topic只能有一个消费者，所以启动程序前必须关闭kakka方式启动的消费者
 *
 */
