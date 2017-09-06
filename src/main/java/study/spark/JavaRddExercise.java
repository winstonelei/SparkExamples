package study.spark;
import java.util.*;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.catalyst.expressions.In;
import org.apache.spark.sql.execution.datasources.jdbc.JDBCRDD;
import scala.Tuple2;
import scala.Tuple3;

/**
 * Created by winstone on 2017/6/29.
 * spark rdd 的一些基本用法
 *
 */
public class JavaRddExercise {

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("FlatMap").setMaster("local");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        List<Integer> list=Arrays.asList(1,2,3,4,5);
        JavaRDD<Integer> rdd= ctx.parallelize(list);
/*
       mapPartition可以倒过来理解，先partition，再把每个partition进行map函数，如果在映射的过程中需要频繁创建额外的对象，使用mapPartitions要比map高效的过。
       将RDD中的所有数据通过JDBC连接写入数据库，如果使用map函数，可能要为每一个元素都创建一个connection，
       这样开销很大，如果使用mapPartitions，那么只需要针对每一个分区建立一个connection。
       将每个元素变为,也就是将每个元素从单个元素变为一个tuple   i ==> i,i*i

       JavaRDD<Tuple2<Integer,Integer>> rdds = rdd.mapPartitions(new FlatMapFunction<Iterator<Integer>,  Tuple2<Integer,Integer>>() {
           @Override
           public Iterable<Tuple2<Integer, Integer>> call(Iterator<Integer> integerIterator) throws Exception {
               ArrayList<Tuple2<Integer,Integer>> list = new ArrayList<Tuple2<Integer, Integer>>();
               while(integerIterator.hasNext()){
                   Integer integer = integerIterator.next();
                   list.add(new Tuple2<Integer, Integer>(integer,integer*integer));
               }
               return list;
           }
       });

        rdds.foreach(new VoidFunction<Tuple2<Integer, Integer>>() {
            @Override
            public void call(Tuple2<Integer, Integer> integerIntegerTuple2) throws Exception {
                System.out.println(integerIntegerTuple2);
            }
        });*/


/*
          mappartitionRdd 实现每个元素 i*i,只是单个元素
          JavaRDD<Integer> mapPartitionRDD = rdd.mapPartitions(new FlatMapFunction<Iterator<Integer>, Integer>() {
            @Override
            public Iterable<Integer> call(Iterator<Integer> it) throws Exception {
                ArrayList<Integer> results = new ArrayList<>();
                while (it.hasNext()) {
                    int i = it.next();
                    results.add(i*i);
                }
                return results;
            }
        });

        mapPartitionRDD.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer integer) throws Exception {
                System.out.println(integer);
            }
        });*/


      /*
        mapPartitionWithIndex类似，也是按照分区进行的map操作，不过mapPartitionsWithIndex传入的参数多了一个分区的值
       JavaRDD<Integer> rdds = ctx.parallelize(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 3);
       JavaRDD<Tuple2<Integer,Integer>>  tuple2Rdd = rdds.mapPartitionsWithIndex(new Function2<Integer, Iterator<Integer>, Iterator<Tuple2<Integer,Integer>>>() {
           @Override
           public  Iterator<Tuple2<Integer,Integer>> call(Integer v1, Iterator<Integer> v2) throws Exception {
               ArrayList<Tuple2<Integer,Integer>> list = new ArrayList<Tuple2<Integer, Integer>>();
               while(v2.hasNext()){
                   Integer it = v2.next();
                   list.add(new Tuple2<Integer, Integer>(v1,it));
               }
              return list.iterator();

           }
       },false);


       tuple2Rdd.foreach(new VoidFunction<Tuple2<Integer, Integer>>() {
           @Override
           public void call(Tuple2<Integer, Integer> integerIntegerTuple2) throws Exception {
               System.out.println(integerIntegerTuple2);
           }
       });

        //实现键值对形式的rdd
        JavaRDD<Tuple2<Integer, Integer>> rdd1 = ctx.parallelize(Arrays.asList(new Tuple2<Integer, Integer>(1, 1), new Tuple2<Integer, Integer>(1, 2)
                , new Tuple2<Integer, Integer>(2, 3), new Tuple2<Integer, Integer>(2, 4)
                , new Tuple2<Integer, Integer>(3, 5), new Tuple2<Integer, Integer>(3, 6)
                , new Tuple2<Integer, Integer>(4, 7), new Tuple2<Integer, Integer>(4, 8)
                , new Tuple2<Integer, Integer>(5, 9), new Tuple2<Integer, Integer>(5, 10)
        ), 3);

        JavaPairRDD<Integer,Integer> pairRDD = JavaPairRDD.fromJavaRDD(rdd1);

        JavaRDD<Tuple2<Integer,Tuple2<Integer,Integer>>>  mapPairRdd = pairRDD.mapPartitionsWithIndex(new Function2<Integer, Iterator<Tuple2<Integer, Integer>>, Iterator<Tuple2<Integer,Tuple2<Integer,Integer>>>>() {
            @Override
            public Iterator<Tuple2<Integer,Tuple2<Integer,Integer>>> call(Integer v1, Iterator<Tuple2<Integer, Integer>> v2) throws Exception {
               ArrayList<Tuple2<Integer,Tuple2<Integer,Integer>>> list = new ArrayList<Tuple2<Integer, Tuple2<Integer, Integer>>>();
               while(v2.hasNext()){
                  Tuple2<Integer,Integer> t2 = v2.next();
                   list.add(new Tuple2<Integer, Tuple2<Integer, Integer>>(v1,t2));
               }
               return list.iterator();
            }
        },false);

        mapPairRdd.foreach(new VoidFunction<Tuple2<Integer, Tuple2<Integer, Integer>>>() {
            @Override
            public void call(Tuple2<Integer, Tuple2<Integer, Integer>> integerTuple2Tuple2) throws Exception {
                System.out.println(integerTuple2Tuple2);
            }
        });
*/


  /*     //flatMap的函数应用于每一个元素，对于每一个元素返回的是多个元素组成的迭代器
        JavaRDD<String> lines = ctx.textFile("test.txt");
        JavaRDD<String> flatMapRDD = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String s) throws Exception {
                String[] split = s.split("\\s+");
                return Arrays.asList(split);
            }
        });
        //输出第一个
        System.out.println(flatMapRDD.first());


        JavaRDD<Iterable<String>> mapRDD  = lines.map(new Function<String, Iterable<String>>() {
            @Override
            public Iterable<String> call(String v1) throws Exception {
                String[] split = v1.split("\\s+");
                return Arrays.asList(split);
            }
        });

        System.out.println(mapRDD.first());
        System.out.println(mapRDD);*/



         /*//java 版本去重
         JavaRDD<String> rdd1 = ctx.parallelize(Arrays.asList("aa", "aa", "bb", "cc", "dd"));

         JavaRDD<String> distinctRdd = rdd1.distinct();

         List<String> collect= distinctRdd.collect();

         for(String str : collect){
             System.out.println(str);
         }
*/

        //cartesian 笛卡尔乘积
/*        JavaRDD<String> RDD1 = ctx.parallelize(Arrays.asList("1", "2", "3"));
        JavaRDD<String> RDD2 = ctx.parallelize(Arrays.asList("a","b","c"));
        JavaPairRDD<String, String> cartesian = RDD1.cartesian(RDD2);

        List<Tuple2<String, String>> collect1 = cartesian.collect();
        for (Tuple2<String, String> tp:collect1) {
            System.out.println("("+tp._1+" "+tp._2+")");
        }*/

        /*//subtract RDD1.subtract(RDD2),返回在RDD1中出现，但是不在RDD2中出现的元素，不去重

        JavaRDD<String> RDD1 = ctx.parallelize(Arrays.asList("aa", "aa", "bb","cc", "dd"));
        JavaRDD<String> RDD2 = ctx.parallelize(Arrays.asList("aa","dd","ff"));
        JavaRDD<String> subtractRDD = RDD1.subtract(RDD2);
        List<String> collect = subtractRDD.collect();
        for (String str:collect) {
            System.out.print(str+" ");
        }*/


/*
        //两个rdd合并
        JavaRDD<String> RDD1 = ctx.parallelize(Arrays.asList("aa", "aa", "bb", "cc", "dd"));
        JavaRDD<String> RDD2 = ctx.parallelize(Arrays.asList("aa","dd","ff"));
        JavaRDD<String> unionRDD = RDD1.union(RDD2);
        List<String> collect = unionRDD.collect();
        for (String str:collect) {
            System.out.print(str+", ");
        }
*/

      /*  //mapToPair
        JavaRDD<String> lines = ctx.textFile("test.txt");
        JavaPairRDD<String,Integer> pairRdd = lines.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String, Integer>(s.split("\\s+")[0],1);
            }
        });
        Iterator<Tuple2<String,Integer>> iter=pairRdd.collect().iterator();
        while(iter.hasNext()){
            Tuple2<String,Integer> tuple = iter.next();
            System.out.println(tuple._1+"="+tuple._2);
        }

       //类似于xxx连接 mapToPair是一对一，一个元素返回一个元素，而flatMapToPair可以一个元素返回多个，相当于先flatMap,在mapToPair
       // 例子: 将每一个单词都分成键为
        JavaPairRDD<String, Integer> wordPairRDD = lines.flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
            @Override
            public Iterable<Tuple2<String, Integer>> call(String s) throws Exception {
                ArrayList<Tuple2<String, Integer>> tpLists = new ArrayList<Tuple2<String, Integer>>();
                String[] split = s.split("\\s+");
                for (int i = 0; i <split.length ; i++) {
                    Tuple2 tp = new Tuple2<String,Integer>(split[i], 1);
                    tpLists.add(tp);
                }
                return tpLists;
            }
        });
        Iterator<Tuple2<String,Integer>> iter1=wordPairRDD.collect().iterator();
        while(iter1.hasNext()){
            Tuple2<String,Integer> tuple = iter1.next();
            System.out.println(tuple._1+"="+tuple._2);
        }
*/


/*        //reducebykey
        JavaRDD<String> lines = ctx.textFile("test.txt");
        JavaPairRDD<String,Integer> pairRdd = lines.flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
            @Override
            public Iterable<Tuple2<String, Integer>> call(String s) throws Exception {
                ArrayList<Tuple2<String,Integer>> list = new ArrayList<Tuple2<String, Integer>>();
                String[] split = s.split("\\s");
                for(int i=0;i<split.length;i++){
                    Tuple2 tp = new Tuple2<String,Integer>(split[i],1);
                    list.add(tp);
                }
                return list;
            }
        });

        JavaPairRDD<String,Integer> jpd = pairRdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1+v2;
            }
        });

       Map<String,Integer> collectMap = jpd.collectAsMap();

       for(String key : collectMap.keySet()){
           System.out.println(key+"="+collectMap.get(key));
       }*/



/*        //groupByKey会将RDD[key,value] 按照相同的key进行分组，形成RDD[key,Iterable[value]]的形式， 有点类似于sql中的groupby，例如类似于MySQL中的group_concat
        JavaRDD<Tuple2<String,Integer>> scoreDetails = ctx.parallelize(Arrays.asList(new Tuple2<String,Integer>("xiaoming",25),
                new Tuple2<String,Integer>("xiaoming",100),new Tuple2<String,Integer>("xiaohua",55)));

        //从普通的javaRDD转换为javaPairRdd需要使用 javaPairRDD.fromJavaRDD
        JavaPairRDD<String,Integer> pairRDD = JavaPairRDD.fromJavaRDD(scoreDetails);

        Map<String,Iterable<Integer>> map =pairRDD.groupByKey().collectAsMap();

        for(String key:map.keySet()){
            System.out.println(key+"="+map.get(key));
        }*/


/*        //groupByKey是对单个 RDD 的数据进行分组，还可以使用一个叫作 cogroup() 的函数对多个共享同一个键的 RDD 进行分组
        //RDD1.cogroup(RDD2) 会将RDD1和RDD2按照相同的key进行分组，得到(key,RDD[key,Iterable[value1],Iterable[value2]])的形式
        //cogroup也可以多个进行分组

        JavaRDD<Tuple2<String,Integer>> scoreDetails1 = ctx.parallelize(Arrays.asList(new Tuple2<String,Integer>("xiaoming", 75)
                , new Tuple2<String,Integer>("xiaoming", 90)
                , new Tuple2<String,Integer>("lihua", 95)
                , new Tuple2<String,Integer>("lihua", 96)));
        JavaRDD<Tuple2<String,Integer>> scoreDetails2 = ctx.parallelize(Arrays.asList(new Tuple2<String,Integer>("xiaoming", 75)
                , new Tuple2<String,Integer>("lihua", 60)
                , new Tuple2<String,Integer>("lihua", 62)));
        JavaRDD<Tuple2<String,Integer>> scoreDetails3 = ctx.parallelize(Arrays.asList(new Tuple2<String,Integer>("xiaoming", 75)
                , new Tuple2<String,Integer>("xiaoming", 45)
                , new Tuple2<String,Integer>("lihua", 24)
                , new Tuple2<String,Integer>("lihua", 57)));

        JavaPairRDD<String, Integer> scoreMapRDD1 = JavaPairRDD.fromJavaRDD(scoreDetails1);
        JavaPairRDD<String, Integer> scoreMapRDD2 = JavaPairRDD.fromJavaRDD(scoreDetails2);
        JavaPairRDD<String, Integer> scoreMapRDD3 = JavaPairRDD.fromJavaRDD(scoreDetails2);

        JavaPairRDD<String, Tuple3<Iterable<Integer>, Iterable<Integer>, Iterable<Integer>>> cogroupRDD = (JavaPairRDD<String, Tuple3<Iterable<Integer>, Iterable<Integer>, Iterable<Integer>>>) scoreMapRDD1.cogroup(scoreMapRDD2, scoreMapRDD3);
        Map<String, Tuple3<Iterable<Integer>, Iterable<Integer>, Iterable<Integer>>> tuple3 = cogroupRDD.collectAsMap();
        for (String key:tuple3.keySet()) {
            System.out.println("("+key+", "+tuple3.get(key)+")");
        }*/


/*
        RDD中的join操作
        JavaRDD<Tuple2<Integer,Integer>> rddPre = ctx.parallelize(Arrays.asList(new Tuple2<Integer,Integer>(1,2)
                , new Tuple2<Integer,Integer>(3,4)
                , new Tuple2<Integer,Integer>(3,6)));
        JavaRDD<Tuple2<Integer,Integer>> otherPre = ctx.parallelize(Arrays.asList(new Tuple2<Integer,Integer>(3,10)));

        //JavaRDD转换成JavaPairRDD
        JavaPairRDD<Integer, Integer> rdd1 = JavaPairRDD.fromJavaRDD(rddPre);
        JavaPairRDD<Integer, Integer> other = JavaPairRDD.fromJavaRDD(otherPre);
        //subtractByKey
        JavaPairRDD<Integer, Integer> subRDD = rdd1.subtractByKey(other);

        //join
        JavaPairRDD<Integer, Tuple2<Integer, Integer>> joinRDD =  rdd1.join(other);

        //leftOuterJoin
        //JavaPairRDD<Integer, Tuple2<Integer, Optional<Integer>>> integerTuple2JavaPairRDD =
        rdd1.leftOuterJoin(other);

        //rightOutJoin
        //JavaPairRDD<Integer, Tuple2<Optional<Integer>, Integer>> rightOutJoin =
        rdd1.rightOuterJoin(other);

        */


        /*
        //从普通的javaRDD转换成为javaPairRDD，countByKey
        JavaRDD<Tuple2<Integer, Integer>> tupleRDD =
                ctx.parallelize(Arrays.asList(new Tuple2<>(1, 2),
                        new Tuple2<>(2, 4),
                        new Tuple2<>(2, 5),
                        new Tuple2<>(3, 4),
                        new Tuple2<>(3, 5),
                        new Tuple2<>(3, 6)));
        JavaPairRDD<Integer, Integer> mapRDD = JavaPairRDD.fromJavaRDD(tupleRDD);
        //countByKey
        Map<Integer, Object> countByKeyRDD = mapRDD.countByKey();
        for (Integer i:countByKeyRDD.keySet()) {
            System.out.println("("+i+", "+countByKeyRDD.get(i)+")");
        }


*/








        ctx.close();




    }

}
