package study.spark.streaming;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * 论坛数据自动生成代码，该生成的数据会作为Producer的方式发送给Kafka，然后SparkStreaming程序会从
 * Kafka中在线Pull到论坛或者网站的用户在线行为信息，进而进行多维度的在线分析
 * 这里产生数据，就会发送给kafka，kafka那边启动消费者，就会接收到数据，这一步是用来测试生成数据和消费数据没有问题的，确定没问题后要关闭消费者，
 * 启动99讲的类作为消费者，就会按pv，uv等方式处理这些数据。因为一个topic只能有一个消费者，所以启动程序前必须关闭kakka方式启动的消费者
 * 数据格式如下：
 * date：日期，格式为yyyy-MM-dd
 * timestamp：时间戳
 * userID:用户ID
 * pageID:页面ID
 * chanelID：板块的ID
 * action：点击和注册
 */
public class SparkStreamingDataManuallyProducerForKafkas extends Thread
{

    //具体的论坛频道
    static String[] channelNames = new  String[]{
            "Spark","Scala","Kafka","Flink","Hadoop","Storm",
            "Hive","Impala","HBase","ML"
    };
    //用户的两种行为模式
    static String[] actionNames = new String[]{"View", "Register"};

    private String topic; //发送给Kafka的数据的类别
    private Producer<Integer, String> producerForKafka;

    private static String dateToday;
    private static Random random;

    public SparkStreamingDataManuallyProducerForKafkas(String topic){
        dateToday = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.topic = topic;
        random = new Random();
        Properties conf = new Properties();
        conf.put("metadata.broker.list","Master:9092,Worker1:9092,Worker2:9092");
        conf.put("serializer.class", "kafka.serializer.StringEncoder");
        producerForKafka = new Producer<Integer, String>(new ProducerConfig(conf)) ;
    }


    @Override
    public void run() {
        int counter = 0;
        while(true){
            counter++;
            String userLog = userlogs();
            System.out.println("product:"+userLog);
            producerForKafka.send(new KeyedMessage<Integer, String>(topic, userLog));
            if(0 == counter%500){
                counter = 0;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main( String[] args )
    {

        new SparkStreamingDataManuallyProducerForKafkas("UserLogs").start();


    }



    private static String userlogs() {

        StringBuffer userLogBuffer = new StringBuffer("");
        int[] unregisteredUsers = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        long timestamp = new Date().getTime();
        Long userID = 0L;
        long pageID = 0L;

        //随机生成的用户ID
        if(unregisteredUsers[random.nextInt(8)] == 1) {
            userID = null;
        } else {
            userID = (long) random.nextInt((int) 2000);
        }


        //随机生成的页面ID
        pageID =  random.nextInt((int) 2000);

        //随机生成Channel
        String channel = channelNames[random.nextInt(10)];

        //随机生成action行为
        String action = actionNames[random.nextInt(2)];


        userLogBuffer.append(dateToday)
                .append("\t")
                .append(timestamp)
                .append("\t")
                .append(userID)
                .append("\t")
                .append(pageID)
                .append("\t")
                .append(channel)
                .append("\t")
                .append(action);   //这里不要加\n换行符，因为kafka自己会换行，再append一个换行符，消费者那边就会处理不出数据


        return userLogBuffer.toString();

    }

}
