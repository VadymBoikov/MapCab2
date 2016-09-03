package mapCab

//import java.util.Properties
import com.javadocmd.simplelatlng.util.LengthUnit
import com.javadocmd.simplelatlng.{LatLng, LatLngTool}
import kafka.serializer.StringDecoder
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.hive.HiveContext


object prepare_data {

  def main(args: Array[String]) {
    System.out.println("currnent args length:" + args.length)
    System.out.println(args.toString)

    if (args.length < 2) {
      System.err.println("Usage: mapCab.prepare_data <directory_files> <destination_file>")
      System.exit(1)
    }
    val Array(directory_path, destination_file) = args

    val sparkConf = new SparkConf().setAppName("prepare_data")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)
    //val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._


    // new_omwyek.txt,37.75452,-122.46566,0,1211018404
    val files = sc.wholeTextFiles(directory_path + "*")
    val extracted_data = files.flatMap(file => {
      val content = file._2
      val reg_pattern = "[^/]+$".r
      val cab_file = reg_pattern.findFirstIn(file._1).getOrElse("None") // substring to extract name [^/]+$
      val cab_name = if(cab_file.endsWith(".txt")) cab_file.dropRight(4) else cab_file
      for (line <- content.lines) yield {
        val arr = line.split(" ") //lat,lng, occ, timestamp
        //val posit = new LatLng(arr(0).toDouble, arr(1).toDouble)
        (cab_name, arr(0).toDouble, arr(1).toDouble, arr(2).toInt, arr(3).toLong)
      }
    })


    def dist_udf = udf((lat_old:Double, lng_old:Double, lat_new:Double, lng_new:Double) => {
      var dist : Double = 0.0
      try {
        dist = LatLngTool.distance(new LatLng(lat_old, lng_old), new LatLng(lat_new, lng_new), LengthUnit.KILOMETER)
      }
      dist
    })

    def pick_udf = udf((occ_old:Int, occ_new:Int) => if(occ_new - occ_old > 0) 1 else 0 )
    def drop_udf = udf((occ_old:Int, occ_new:Int) => if(occ_new - occ_old < 0) 1 else 0 )


    val df = extracted_data.toDF("cab_name", "lat", "lng", "occ", "timestamp")
    val overCategory = Window.partitionBy('cab_name).orderBy('timestamp)
    val lagged = df.
      withColumn("lat_old", lag("lat", 1).over(overCategory)).
      withColumn("lng_old", lag("lng", 1).over(overCategory)).
      withColumn("occ_old", lag("occ", 1).over(overCategory))

    val changed = lagged.
      withColumn("distance", dist_udf(col("lat_old"), col("lng_old"), col("lat"),col("lng"))).
      withColumn("pick", pick_udf(col("occ_old"), col("occ"))).
      withColumn("drop", drop_udf(col("occ_old"), col("occ"))).
      na.fill(0)

    val result = changed.select('cab_name, 'lat, 'lng, 'occ, 'timestamp,'distance, 'pick, 'drop).
      sort('timestamp).map(_.mkString(","))


    result.saveAsTextFile(destination_file)
  }



}
