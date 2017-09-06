package study.spark.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by winstone on 2017/7/4.
 */
public class RDD2DataFrameByProgrammatically {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("RDD2DataFrameByProgrammatically");//.setMaster("local")
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);

        JavaRDD<String> lines = sc.textFile("persons.txt");

        /**
         * 第一步：在RDD的基础上创建类型为Row的RDD
         */
        JavaRDD<Row> personsRDD = lines.map(new Function<String, Row>() {

            @Override
            public Row call(String line) throws Exception {
                String[] splited = line.split(",");
                return RowFactory.create(Integer.valueOf(splited[0]), splited[1],Integer.valueOf(splited[2]));
            }
        });

        /**
         * 第二步：动态构造DataFrame的元数据，一般而言，有多少列以及每列的具体类型可能来自于JSON文件，也可能来自于DB
         */
        List<StructField> structFields = new ArrayList<StructField>();
        structFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, true));
        structFields.add(DataTypes.createStructField("name", DataTypes.StringType, true));
        structFields.add(DataTypes.createStructField("age", DataTypes.IntegerType, true));
        //构建StructType，用于最后DataFrame元数据的描述
        StructType structType =DataTypes.createStructType(structFields);

        /**
         * 第三步：基于以后的MetaData以及RDD<Row>来构造DataFrame
         */
        DataFrame personsDF = sqlContext.createDataFrame(personsRDD, structType);

        /**
         * 第四步：注册成为临时表以供后续的SQL查询操作
         */
        personsDF.registerTempTable("persons");

        /**
         * 第五步，进行数据的多维度分析
         */
        DataFrame result = sqlContext.sql("select * from persons where age > 8");

        /**
         * 第六步：对结果进行处理，包括由DataFrame转换成为RDD<Row>，以及结构持久化
         */
        List<Row> listRow = result.javaRDD().collect();
        for(Row row : listRow){
            System.out.println(row);
        }

    }



}
