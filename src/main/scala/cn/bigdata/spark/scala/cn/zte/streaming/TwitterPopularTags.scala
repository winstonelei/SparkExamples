/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package main.scala.cn.bigdata.spark.scala.cn.zte.streaming

import kafka.serializer.StringDecoder
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils

/**
 * Calculates popular hashtags (topics) over sliding 10 and 60 second windows from a Twitter
 * stream. The stream is instantiated with credentials and optionally filters supplied by the
 * command line arguments.
 *
 * Run this on your local machine as
 *
 */
object TwitterPopularTags {
  def main(args: Array[String]) {
    val ssc = new StreamingContext("local[2]", "PageViewStream", Seconds(1),
      System.getenv("SPARK_HOME"), StreamingContext.jarOfClass(this.getClass).toSeq)
/*
    val stream = TwitterUtils.createStream(ssc, None, filters)
    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))
*/
    val Array(brokers, topics) = Array("114.55.29.86:9092,114.55.29.241:9092","test")
    val topicsSet = topics.split(",").toSet

    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, topicsSet)
    // Get the lines, split them into words, count the words and print
    val lines = messages.map(_._2)
    val words = lines.flatMap(_.split(" "))

    val topCounts60 = words.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
                     .map{case (topic, count) => (count, topic)}
                     .transform(_.sortByKey(false))

    val topCounts10 = words.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(10))
                     .map{case (topic, count) => (count, topic)}
                     .transform(_.sortByKey(false))

    // Print popular hashtags
    topCounts60.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })

    topCounts10.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
// scalastyle:on println
