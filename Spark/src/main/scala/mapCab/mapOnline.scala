package mapCab

//import java.util.Properties
import kafka.serializer.StringDecoder
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

import scala.util.parsing.json.JSON
import org.apache.spark.SparkConf
//import com.cloudera.spark.hbase.HBaseContext




object mapOnline{
  def hbase_open_connection(zkQuorum:String) = {
    val hbase_conf = HBaseConfiguration.create()
/*
    hbase_conf.addResource(new Path("/etc/hbase/conf/hdfs-site.xml"))
    hbase_conf.addResource(new Path("/etc/hbase/conf/core-site.xml"))
    hbase_conf.addResource(new Path("/etc/hbase/conf/hbase-site.xml"))
*/
    hbase_conf.set("hbase.zookeeper.quorum", zkQuorum)
    val conn = ConnectionFactory.createConnection(hbase_conf)
    conn
  }

  def main(args: Array[String]) {
    System.out.println("currnent args length:" +  args.length)
    System.out.println(args.toString)
    if (args.length < 3) {
      System.err.println("Usage: mapCab.mapOnline <zkQuorum> <group> <topics>")
      System.exit(1)
    }
    val Array(zkQuorum, group, topics) = args

    val sparkConf = new SparkConf().setAppName("Map Online")
    //sparkConf.set("spark.streaming.receiver.writeAheadLog.enable", "true")
    //sparkConf.set("spark.streaming.receiver.maxRate", maxRate)
    //sparkConf.set("spark.streaming.blockInterval", blockInterval)


    val dstream_sec = 2
    val ssc = new StreamingContext(sparkConf, Seconds(dstream_sec.toInt))

    val numThreads = 1
    val topicMap = topics.split(",").map((_, numThreads.toInt) ).toMap
    //val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)

    val kafkaParams = Map(
      "zookeeper.connect" -> zkQuorum,
      "group.id" -> group,
      /*"zookeeper.connection.timeout.ms" -> "30000",*/
      //"kafka.auto.offset.reset" -> "smallest",
      "auto.offset.reset" -> "smallest"
    )

    val lines = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicMap, StorageLevel.MEMORY_ONLY).map(_._2)
    //val jsonf = lines.map(JSON.parseFull(_)).map(_.get.asInstanceOf[scala.collection.immutable.Map[String, String]])
    val array = lines.map(x => x.split(",")).map(x => (x(0), "{\"lat\": "+ x(1) + ", \"lng\": " + x(2) + "}"))

    array.foreachRDD(rdd => {
      rdd.foreachPartition(part =>{
        val conn = hbase_open_connection(zkQuorum)
        val htable = conn.getTable(TableName.valueOf("map_online"))

        part.foreach(tuple =>{
          val p = new Put("city".getBytes)
          val event_timestamp = System.currentTimeMillis
          p.addColumn("data".getBytes, tuple._1.getBytes, event_timestamp, tuple._2.getBytes())
          htable.put(p)
        })
        conn.close()
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
