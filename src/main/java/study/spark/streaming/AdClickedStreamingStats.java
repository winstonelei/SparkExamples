package study.spark.streaming;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.google.common.base.Optional;

import kafka.serializer.StringDecoder;
import scala.Tuple2;
/**
 *
   在线处理广告点击流
 * 广告点击的基本数据格式：timestamp、ip、userID、adID、province、city
 *
 * @author hp
 *
 */
public class AdClickedStreamingStats {

   public static void main(String[] args) {
      SparkConf conf = new SparkConf().setMaster("local[5]").
              setAppName("AdClickedStreamingStats");

      /*SparkConf conf = new SparkConf().setMaster("spark://Master:7077").
            setAppName("SparkStreamingOnKafkaReceiver");*/

      JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(10));

      /**
       * 创建Kafka元数据,来让Spark Streaming这个Kafka Consumer利用
       */
      Map<String, String> kafkaParameters = new HashMap<String, String>();
      kafkaParameters.put("metadata.broker.list", "Master:9092,Worker1:9092,Worker2:9092");

      Set<String> topics =  new HashSet<String>();
      topics.add("AdClicked");

      JavaPairInputDStream<String, String> adClickedStreaming = KafkaUtils.createDirectStream(jsc,
              String.class, String.class,
              StringDecoder.class, StringDecoder.class,
              kafkaParameters,
              topics);
      /**
       * 因为要对黑名单进行在线过滤，而数据是在RDD中的，所以必然使用transform这个函数；
       * 但是在这里我们必须使用transformToPair，原因是读取进来的Kafka的数据是Pair<String,String>类型的,另外
       * 一个原因是过滤后的数据要进行进一步处理，所以必须是读进来的Kafka数据的原始类型DStream<String, String>
       * 在此：再次说明每个Batch Duration中实际上讲输入的数据就是被一个且仅仅被一个RDD封装的，你可以有多个
       * InputDstream，但是其实在产生Job的时候，这些不同的InputDstream在Batch Duration中就相当于Spark基于
       * HDFS数据操作的不同文件来源而已罢了。
       */

      JavaPairDStream<String, String> filteredadClickedStreaming = adClickedStreaming.transformToPair(new Function<JavaPairRDD<String,String>, JavaPairRDD<String,String>>() {

         @Override
         public JavaPairRDD<String, String> call(JavaPairRDD<String, String> rdd) throws Exception {
            /**
             * 在线黑名单过滤思路步骤：
             * 1，从数据库中获取黑名单转换成RDD，即新的RDD实例封装黑名单数据；
             * 2，然后把代表黑名单的RDD的实例和Batch Duration产生的rdd进行join操作,准确的说是进行
             * leftOuterJoin操作，也就是说使用Batch Duration产生的rdd和代表黑名单的RDD的实例进行
             * leftOuterJoin操作，如果两者都有内容的话，就会是true，否则的话就是false；
             * 我们要留下的是leftOuterJoin操作结果为false；
             */

            final List<String> blackListNames = new ArrayList<String>();
            JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
            jdbcWrapper.doQuery("SELECT * FROM blacklisttable", null, new ExecuteCallBack(){
               @Override
               public void resultCallBack(ResultSet result) throws Exception {

                  while(result.next()){
                     blackListNames.add(result.getString(1));
                  }
               }

            });

            List<Tuple2<String, Boolean>> blackListTuple = new ArrayList<Tuple2<String, Boolean>>();

            for (String name : blackListNames){
               blackListTuple.add(new Tuple2<String,Boolean>(name, true));
            }

            List<Tuple2<String, Boolean>> blackListFromDB = blackListTuple; //数据来自于查询的黑名单表并且映射成为<String, Boolean>

            JavaSparkContext jsc = new JavaSparkContext(rdd.context());

            /**
             * 黑名单的表中只有userID，但是如果要进行join操作的话，就必须是Key-Value，所以
             * 在这里我们需要基于数据表中的数据产生Key-Value类型的数据集合；
             */
            JavaPairRDD<String, Boolean> blackListRDD = jsc.parallelizePairs(blackListFromDB);


            /**
             * 进行操作的时候肯定是基于userID进行join的，所以必须把传入的rdd进行mapToPair操作转化成为符合
             * 格式的rdd
             *
             * 广告点击的基本数据格式：timestamp、ip、userID、adID、province、city
             */

            JavaPairRDD<String, Tuple2<String, String>>  rdd2Pair = rdd.mapToPair(new PairFunction<Tuple2<String,String>, String, Tuple2<String,String>>() {
               @Override
               public Tuple2<String, Tuple2<String, String>> call(Tuple2<String, String> t) throws Exception {
                  String userID = t._2.split("\t")[2];
                  return new Tuple2<String, Tuple2<String, String>>(userID, t);
               }
            });

            JavaPairRDD<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>> joined = rdd2Pair.leftOuterJoin(blackListRDD);

            JavaPairRDD<String, String> result = joined.filter(new Function<Tuple2<String,
                    Tuple2<Tuple2<String,String>,Optional<Boolean>>>, Boolean>() {
               @Override
               public Boolean call(Tuple2<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>> v1)
                       throws Exception {
                  Optional<Boolean> optional = v1._2._2;
                  if (optional.isPresent() && optional.get()){
                     return false;
                  } else {
                     return true;
                  }
               }
            }).mapToPair(new PairFunction<Tuple2<String,Tuple2<Tuple2<String,String>,Optional<Boolean>>>, String, String>() {
               @Override
               public Tuple2<String, String> call(
                       Tuple2<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>> t) throws Exception {
                  return t._2._1;
               }
            });
            return result;
         }
      });


      /*
       * 第四步：接下来就像对于RDD编程一样基于DStream进行编程！！！原因是DStream是RDD产生的模板（或者说类），
       * 在Spark Streaming具体
       * 发生计算前，其实质是把每个Batch的DStream的操作翻译成为对RDD的操作！！！
       *对初始的DStream进行Transformation级别的处理，例如map、filter等高阶函数等的编程，来进行具体的数据计算
        *     广告点击的基本数据格式：timestamp、ip、userID、adID、province、city
        */

      JavaPairDStream<String, Long> pairs = filteredadClickedStreaming.mapToPair(new PairFunction<Tuple2<String,String>, String, Long>() {

         @Override
         public Tuple2<String, Long> call(Tuple2<String, String> t) throws Exception {
            String[] splited = t._2.split("\t");
            String timestamp = splited[0]; //yyyy-MM-dd
            String ip = splited[1];
            String userID = splited[2];
            String adID = splited[3];
            String province = splited[4];
            String city = splited[5];

            String clickedRecord = timestamp + "_" + ip + "_" + userID + "_" + adID + "_"
                    + province + "_" + city;

            return new Tuple2<String, Long>(clickedRecord, 1L);
         }
      });

       /*
          * 第四步：对初始的DStream进行Transformation级别的处理，例如map、filter等高阶函数等的编程，来进行具体的数据计算
          *   计算每个Batch Duration中每个User的广告点击量
          */
      JavaPairDStream<String, Long> adClickedUsers = pairs.reduceByKey(new Function2<Long, Long, Long>(){

         @Override
         public Long call(Long v1, Long v2) throws Exception {
            // TODO Auto-generated method stub
            return v1 + v2;
         }
      });

      JavaPairDStream<String, Long>  filteredClickInBatch = adClickedUsers.filter(new Function<Tuple2<String,Long>, Boolean>() {

         @Override
         public Boolean call(Tuple2<String, Long> v1) throws Exception {
            if ( 1 < v1._2){
               //更新一下黑名单的数据表
               return false;
            } else {
               return true;
            }

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
       */
//    filteredClickInBatch.print();

      filteredClickInBatch.foreachRDD(new Function<JavaPairRDD<String,Long>, Void>() {

         @Override
         public Void call(JavaPairRDD<String, Long> rdd) throws Exception {

            rdd.foreachPartition(new VoidFunction<Iterator<Tuple2<String,Long>>>() {
               @Override
               public void call(Iterator<Tuple2<String, Long>> partition) throws Exception {
                  /**
                   * 在这里我们使用数据库连接池的高效读写数据库的方式把数据写入数据库MySQL;
                   * 由于传入的参数是一个Iterator类型的集合，所以为了更加高效的操作我们需要批量处理
                   * 例如说一次性插入1000条Record，使用insertBatch或者updateBatch类型的操作；
                   * 插入的用户信息可以只包含：timestamp、ip、userID、adID、province、city
                   * 这里面有一个问题：可能出现两条记录的Key是一样的，此时就需要更新累加操作
                   */

                  List<UserAdClicked> userAdClickedList = new ArrayList<UserAdClicked>();

                  while (partition.hasNext()){
                     Tuple2<String, Long> record = partition.next();
                     String[] splited = record._1.split("\t");

                     UserAdClicked userClicked = new UserAdClicked();
                     userClicked.setTimestamp(splited[0]);
                     userClicked.setIp(splited[1]);
                     userClicked.setUserID(splited[2]);
                     userClicked.setAdID(splited[3]);
                     userClicked.setProvince(splited[4]);
                     userClicked.setCity(splited[5]);
                     userAdClickedList.add(userClicked);

                  }

                  final List<UserAdClicked> inserting  = new ArrayList<UserAdClicked>();
                  final  List<UserAdClicked> updating  = new ArrayList<UserAdClicked>();

                  JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
                  //adclicked 表的字段：timestamp、ip、userID、adID、province、city、clickedCount
                  for (final UserAdClicked clicked : userAdClickedList){
                     jdbcWrapper.doQuery("SELECT count(1) FROM adclicked WHERE "
                                     + " timestamp = ? AND userID = ? AND adID = ?",
                             new Object[]{clicked.getTimestamp(), clicked.getUserID(), clicked.getAdID()},
                             new ExecuteCallBack() {

                                @Override
                                public void resultCallBack(ResultSet result) throws Exception {
                                   if(result.next()){
                                      long count = result.getLong(1);
                                      clicked.setClickedCount(count);
                                      updating.add(clicked);
                                   } else {
                                      inserting.add(clicked);
                                   }

                                }
                             });
                  }
                  //adclicked 表的字段：timestamp、ip、userID、adID、province、city、clickedCount
                  ArrayList<Object[]> insertParametersList = new ArrayList<Object[]>();
                  for(UserAdClicked inserRecord : inserting){
                     insertParametersList.add(new Object[]{
                             inserRecord.getTimestamp(),
                             inserRecord.getIp(),
                             inserRecord.getUserID(),
                             inserRecord.getAdID(),
                             inserRecord.getProvince(),
                             inserRecord.getCity(),
                             inserRecord.getClickedCount()
                     });
                  }
                  jdbcWrapper.doBatch("INSERT INTO adclicked VALUES(?,?,?,?,?,?,?)", insertParametersList);
                  //adclicked 表的字段：timestamp、ip、userID、adID、province、city、clickedCount
                  ArrayList<Object[]> updateParametersList = new ArrayList<Object[]>();
                  for(UserAdClicked updateRecord : updating){
                     updateParametersList.add(new Object[]{
                             updateRecord.getTimestamp(),
                             updateRecord.getIp(),
                             updateRecord.getUserID(),
                             updateRecord.getAdID(),
                             updateRecord.getProvince(),
                             updateRecord.getCity(),
                             updateRecord.getClickedCount()
                     });
                  }
                  jdbcWrapper.doBatch("UPDATE adclicked set clickedCount = ? WHERE "
                          + " timestamp = ? AND ip = ? AND userID = ? AND adID = ? AND province = ? "
                          + "AND city = ? ", updateParametersList);
               }
            });
            return null;
         }
      });


      JavaPairDStream<String, Long> blackListBasedOnHistory = filteredClickInBatch.filter(new Function<Tuple2<String,Long>, Boolean>() {
         @Override
         public Boolean call(Tuple2<String, Long> v1) throws Exception {
            //广告点击的基本数据格式：timestamp、ip、userID、adID、province、city
            String[] splited = v1._1.split("\t");

            String date = splited[0];
            String userID = splited[2];
            String adID = splited[3];

            /**
             * 接下来根据date、userID、adID等条件去查询用户点击广告的数据表，获得总的点击次数
             * 这个时候基于点击次数判断是否属于黑名单点击             *
             */

            int clickedCountTotalToday = 81;

            if (clickedCountTotalToday > 50)
            {
               return true;
            } else {
               return false;
            }

         }
      });


      /**
       * 必须对黑名单的整个RDD进行去重操作！！！
       */


      JavaDStream<String> blackListuserIDtBasedOnHistory = blackListBasedOnHistory.map(new Function<Tuple2<String,Long>, String>() {
         @Override
         public String call(Tuple2<String, Long> v1) throws Exception {
            // TODO Auto-generated method stub
            return v1._1.split("\t")[2];
         }
      });

      JavaDStream<String> blackListUniqueuserIDtBasedOnHistory = blackListuserIDtBasedOnHistory.transform(new Function<JavaRDD<String>, JavaRDD<String>>() {
         @Override
         public JavaRDD<String> call(JavaRDD<String> rdd) throws Exception {
            return rdd.distinct();
         }
      });
      //下一步写入黑名单数据表中
      blackListUniqueuserIDtBasedOnHistory.foreachRDD(new Function<JavaRDD<String>, Void>() {
         @Override
         public Void call(JavaRDD<String> rdd) throws Exception {

            rdd.foreachPartition(new VoidFunction<Iterator<String>>() {
               @Override
               public void call(Iterator<String> t) throws Exception {
                  /**
                   * 在这里我们使用数据库连接池的高效读写数据库的方式把数据写入数据库MySQL;
                   * 由于传入的参数是一个Iterator类型的集合，所以为了更加高效的操作我们需要批量处理
                   * 例如说一次性插入1000条Record，使用insertBatch或者updateBatch类型的操作；
                   * 插入的用户信息可以只包含：useID
                   * 此时直接插入黑名单数据表即可。
                   */
                  List<Object[]> blackList = new ArrayList<Object[]>();

                  while(t.hasNext()){
                     blackList.add(new Object[]{(Object)t.next()});
                  }
                  JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
                  jdbcWrapper.doBatch("INSERT INTO blacklisttable VALUES (?) ", blackList);
               }
            });
            return null;
         }
      });

      /**
       * 广告点击累计动态更新,每个updateStateByKey都会在Batch Duration的时间间隔的基础上进行更高点击次数的更新，
       * 更新之后我们一般都会持久化到外部存储设备上，在这里我们存储到MySQL数据库中；
       */
      filteredadClickedStreaming.mapToPair(new PairFunction<Tuple2<String,String>, String, Long>() {

         @Override
         public Tuple2<String, Long> call(Tuple2<String, String> t) throws Exception {
            String[] splited = t._2.split("\t");

            String timestamp = splited[0]; //yyyy-MM-dd
            String ip = splited[1];
            String userID = splited[2];
            String adID = splited[3];
            String province = splited[4];
            String city = splited[5];

            String clickedRecord = timestamp + "_" +  adID + "_"
                    + province + "_" + city;

            return new Tuple2<String, Long>(clickedRecord, 1L);
         }
      }).updateStateByKey(new Function2<List<Long>, Optional<Long>, Optional<Long>>() {
                 @Override
                 public Optional<Long> call(List<Long> v1, Optional<Long> v2) throws Exception {
                    /**在历史的数据的基础上进行更新
                     * v1:代表是当前的key在当前的Batch Duration中出现次数的集合，例如{1,1,1,1,1,1}
                     * v2:代表当前key在以前的Batch Duration中积累下来的结果；我们要再v2的基础上不断加v1的值
                     */
                    Long clickedTotalHistory = 0L;
                    if(v2.isPresent()) {//如果v2存在
                       clickedTotalHistory = v2.get();//拿v2的值
                    }
                    //不用reduceBykey是因为会产生很多shuffle，shuffle里面有很多内容的。updateStateByKey可以算过去一天，1年
                    for(Long one : v1){//循环v1
                       clickedTotalHistory += one;//一直在基础上进行累加
                    }

                    return Optional.of(clickedTotalHistory);
                 }
              }).foreachRDD(new Function<JavaPairRDD<String,Long>, Void>() {

         @Override
         public Void call(JavaPairRDD<String, Long> rdd) throws Exception {
            rdd.foreachPartition(new VoidFunction<Iterator<Tuple2<String,Long>>>() {

               @Override
               public void call(Iterator<Tuple2<String, Long>> partition) throws Exception {
                  /**
                   * 在这里我们使用数据库连接池的高效读写数据库的方式把数据写入数据库MySQL;
                   * 由于传入的参数是一个Iterator类型的集合，所以为了更加高效的操作我们需要批量处理
                   * 例如说一次性插入1000条Record，使用insertBatch或者updateBatch类型的操作；
                   * 插入的用户信息可以只包含：timestamp、adID、province、city
                   * 这里面有一个问题：可能出现两条记录的Key是一样的，此时就需要更新累加操作
                   */

                  List<AdClicked> adClickedList = new ArrayList<AdClicked>();

                  while (partition.hasNext()){
                     Tuple2<String, Long> record = partition.next();
                     String[] splited = record._1.split("\t");

                     AdClicked adClicked = new AdClicked();
                     adClicked.setTimestamp(splited[0]);
                     adClicked.setAdID(splited[1]);
                     adClicked.setProvince(splited[2]);
                     adClicked.setCity(splited[3]);
                     adClicked.setClickedCount(record._2);
                     adClickedList.add(adClicked);
                  }

                  JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
                  final List<AdClicked> inserting  = new ArrayList<AdClicked>();
                  final List<AdClicked> updating  = new ArrayList<AdClicked>();

                  //adclicked 表的字段：timestamp、ip、userID、adID、province、city、clickedCount
                  for (final AdClicked clicked : adClickedList){
                     jdbcWrapper.doQuery("SELECT count(1) FROM adclickedcount WHERE "
                                     + " timestamp = ? AND adID = ? AND province = ? AND city = ? ",
                             new Object[]{clicked.getTimestamp(), clicked.getAdID(), clicked.getProvince(),clicked.getCity()},
                             new ExecuteCallBack() {

                                @Override
                                public void resultCallBack(ResultSet result) throws Exception {
                                   if(result.next()){
                                      long count = result.getLong(1);
                                      clicked.setClickedCount(count);
                                      updating.add(clicked);
                                   } else {
                                      inserting.add(clicked);
                                   }

                                }
                             });
                  }
                  //adclicked 表的字段：timestamp、ip、userID、adID、province、city、clickedCount
                  ArrayList<Object[]> insertParametersList = new ArrayList<Object[]>();
                  for(AdClicked inserRecord : inserting){
                     insertParametersList.add(new Object[]{
                             inserRecord.getTimestamp(),
                             inserRecord.getAdID(),
                             inserRecord.getProvince(),
                             inserRecord.getCity(),
                             inserRecord.getClickedCount()
                     });
                  }
                  jdbcWrapper.doBatch("INSERT INTO adclickedcount VALUES(?,?,?,?,?)", insertParametersList);

                  //adclicked 表的字段：timestamp、ip、userID、adID、province、city、clickedCount
                  ArrayList<Object[]> updateParametersList = new ArrayList<Object[]>();
                  for(AdClicked updateRecord : updating){
                     updateParametersList.add(new Object[]{
                             updateRecord.getTimestamp(),
                             updateRecord.getAdID(),
                             updateRecord.getProvince(),
                             updateRecord.getCity(),
                             updateRecord.getClickedCount()
                     });
                  }
                  jdbcWrapper.doBatch("UPDATE adclickedcount set clickedCount = ? WHERE "
                          + " timestamp = ? AND adID = ? AND province = ? AND city = ? ", updateParametersList);


               }
            });
            return null;
         }
      });


      /*
       * Spark Streaming执行引擎也就是Driver开始运行，Driver启动的时候是位于一条新的线程中的，当然其内部有消息循环体，用于
       * 接受应用程序本身或者Executor中的消息；
       */
      jsc.start();

      jsc.awaitTermination();
      jsc.close();

   }

}

class JDBCWrapper {

   private static JDBCWrapper jdbcInstance = null;
   private static LinkedBlockingQueue<Connection> dbConnectionPool = new LinkedBlockingQueue<Connection> ();

   static {
      try {
         Class.forName("com.mysql.jdbc.Driver");
      } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }


   public static JDBCWrapper getJDBCInstance(){
      if (jdbcInstance == null){

         synchronized(JDBCWrapper.class){
            if (jdbcInstance == null){
               jdbcInstance = new JDBCWrapper();
            }
         }

      }

      return jdbcInstance;
   }

   private JDBCWrapper(){

      for (int i = 0; i < 10; i++){


         try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://Master:3306/sparkstreaming","root","root");
            dbConnectionPool.put(conn);
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      }

   }


   public synchronized Connection getConnection(){
      while (0 == dbConnectionPool.size()){
         try {
            Thread.sleep(20);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      return dbConnectionPool.poll();
   }

   public int[] doBatch(String sqlText, List<Object[]> paramsList) {

      Connection conn = getConnection();
      PreparedStatement preparedStatement = null;
      int[] result = null;
      try {
         conn.setAutoCommit(false);
         preparedStatement = conn.prepareStatement(sqlText);

         for (Object[] parameters : paramsList){
            for(int i = 0; i < parameters.length; i++){
               preparedStatement.setObject(i+1, parameters[i]);
            }

            preparedStatement.addBatch();
         }

         result = preparedStatement.executeBatch();


         conn.commit();

      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } finally {
         if (preparedStatement != null){
            try {
               preparedStatement.close();
            } catch (SQLException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }

         if (conn != null){
            try {
               dbConnectionPool.put(conn);
            } catch (InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }




      return result;
   }


   public void doQuery(String sqlText, Object[] paramsList, ExecuteCallBack callBack) {

      Connection conn = getConnection();
      PreparedStatement preparedStatement = null;
      ResultSet result = null;
      try {

         preparedStatement = conn.prepareStatement(sqlText);


         for(int i = 0; i < paramsList.length; i++){
            preparedStatement.setObject(i+1, paramsList[i]);
         }



         result = preparedStatement.executeQuery();

         callBack.resultCallBack(result);


      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } finally {
         if (preparedStatement != null){
            try {
               preparedStatement.close();
            } catch (SQLException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }

         if (conn != null){
            try {
               dbConnectionPool.put(conn);
            } catch (InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }

   }
}

interface ExecuteCallBack {
   void resultCallBack(ResultSet result) throws Exception;
}

class UserAdClicked {
   private String timestamp;
   private String ip;
   private String userID;
   private String adID;
   private String province;
   private String city;
   private Long clickedCount;


   public Long getClickedCount() {
      return clickedCount;
   }
   public void setClickedCount(Long clickedCount) {
      this.clickedCount = clickedCount;
   }
   public String getTimestamp() {
      return timestamp;
   }
   public void setTimestamp(String timestamp) {
      this.timestamp = timestamp;
   }
   public String getIp() {
      return ip;
   }
   public void setIp(String ip) {
      this.ip = ip;
   }
   public String getUserID() {
      return userID;
   }
   public void setUserID(String userID) {
      this.userID = userID;
   }
   public String getAdID() {
      return adID;
   }
   public void setAdID(String adID) {
      this.adID = adID;
   }
   public String getProvince() {
      return province;
   }
   public void setProvince(String province) {
      this.province = province;
   }
   public String getCity() {
      return city;
   }
   public void setCity(String city) {
      this.city = city;
   }
}

class AdClicked{
   private String timestamp;
   private String adID;
   private String province;
   private String city;
   private Long clickedCount;

   public String getTimestamp() {
      return timestamp;
   }
   public void setTimestamp(String timestamp) {
      this.timestamp = timestamp;
   }
   public String getAdID() {
      return adID;
   }
   public void setAdID(String adID) {
      this.adID = adID;
   }
   public String getProvince() {
      return province;
   }
   public void setProvince(String province) {
      this.province = province;
   }
   public String getCity() {
      return city;
   }
   public void setCity(String city) {
      this.city = city;
   }
   public Long getClickedCount() {
      return clickedCount;
   }
   public void setClickedCount(Long clickedCount) {
      this.clickedCount = clickedCount;
   }

}


