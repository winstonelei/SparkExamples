package study.spark.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by winstone on 2017/7/4.
 */
public class Rdd2DataFrameByReflection {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("RDD2DataFrameByReflection");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        JavaRDD<String> lines = sc.textFile("persons.txt");

        JavaRDD<Person> persons = lines.map(new Function<String,Person>() {
            @Override
            public Person call(String v1) throws Exception {
                String[] splited = v1.split(",");
                Person p = new Person();
                p.setId(Integer.valueOf(splited[0].trim()));
                p.setName(splited[1]);
                p.setAge(Integer.valueOf(splited[2].trim()));
                return p;
            }
        });

        DataFrame df = sqlContext.createDataFrame(persons,Person.class);

        df.registerTempTable("persons");

        DataFrame  bigDatas = sqlContext.sql("select * from persons where age >= 6");

        JavaRDD<Row> javaRdd = bigDatas.javaRDD();

        JavaRDD<Person> result  =  javaRdd.map(new Function<Row, Person>() {
            @Override
            public Person call(Row v1) throws Exception {
                Person p = new Person();
                p.setId(v1.getInt(1));
                p.setName(v1.getString(2));
                p.setAge(v1.getInt(0));
                return p;
            }
        });

        List<Person> personList = result.collect();
        for(Person p : personList){
            System.out.println(p);
        }


      /*  JavaRDD<Person> persons = lines.map(new Function<String, Person>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Person call(String line) throws Exception {
                String[] splited = line.split(",");
                Person p = new Person();
                p.setId(Integer.valueOf(splited[0].trim()));
                p.setName(splited[1]);
                p.setAge(Integer.valueOf(splited[2].trim()));
                return p;
            }
        });


        //在底层通过反射的方式获得Person的所有fields，结合RDD本身，就生成了DataFrame
        DataFrame df = sqlContext.createDataFrame(persons, Person.class);

        df.registerTempTable("persons");

        DataFrame bigDatas = sqlContext.sql("select * from persons where age >= 6");

        JavaRDD<Row> bigDataRDD = bigDatas.javaRDD();

        JavaRDD<Person> result = bigDataRDD.map(new Function<Row, Person>() {

            @Override
            public Person call(Row row) throws Exception {
                Person p = new Person();
                p.setId(row.getInt(1));
                p.setName(row.getString(2));
                p.setAge(row.getInt(0));
                return p;
            }
        });

        List<Person> personList = result.collect();
        for(Person p : personList){
            System.out.println(p);
        }
*/


    }

   public  static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        public  int id;
        public String name;
        public int age;

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }
        @Override
        public String toString() {
            return "Person [id=" + id + ", name=" + name + ", age=" + age + "]";
        }

    }

}
