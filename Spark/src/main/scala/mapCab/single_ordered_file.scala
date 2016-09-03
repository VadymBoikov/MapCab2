package mapCab

import com.javadocmd.simplelatlng.util.LengthUnit
import com.javadocmd.simplelatlng.{LatLng, LatLngTool}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.expressions.Window



object single_ordered_file {

  def main(args: Array[String]) {
    System.out.println("currnent args length:" + args.length)
    System.out.println(args.toString)

    if (args.length < 2) {
      System.err.println("Usage: mapCab.make_single_file <directory_files> <destination_file>")
      System.exit(1)
    }
    val Array(directory_path, destination_file) = args

    val sparkConf = new SparkConf().setAppName("make_single_file")
    val sc = new SparkContext(sparkConf)
    //val sqlContext = new org.apache.spark.sql.SQLContext(sc)


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
        (cab_name, arr(0), arr(1), arr(2), arr(3))
      }
    }).sortBy(_._5).repartition(1).saveAsTextFile(destination_file)


  }



}
