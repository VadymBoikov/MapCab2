#MapCab
This is a modified reproduction of the <a href= "https://github.com/PreetikaKuls/Insight-MapMyCab/">MapMyCab</a> project.


Project was built on Hadoop Ecosystem.
Cluster was deployed using Cloudera distributive <a href= "https://www.cloudera.com/products/apache-hadoop/key-cdh-components.html"> CDH </a>.

Technologies used:
- Kafka
- Flume
- Hive
- HBase
- Spark
- Spark Streaming 
- Flask


# Dataset

Dataset contains 500 files each representing cab driving history. (lattitude, longitude, occupancy, timestamp)

<img src="https://github.com/VadymBoikov/MapCab2/tree/master/images/raw_data.png" alt="alt text" width="500" height="300">


Cab mobility traces are provided by the Exploratorium - the museum of science, art and human perception through the cabspotting project: http://cabspotting.org . "Each San Francisco based Yellow Cab vehicle is currently outfitted with a GPS tracking device that is used by dispatchers to efficiently reach customers. The data is transmitted from each cab to a central receiving station, and then delivered in real-time to dispatch computers via a central server. This system broadcasts the cab call number, location and whether the cab currently has a fare."(*) You can collect your own cab mobility traces following the instructions from http://cabspotting.org/api . 
You can also use this data set of cab mobility traces that were collected in May 2008. This archive contains file '_cabs.txt' with the list of all cabs and for each cab its mobility trace in a separate ASCII file, e.g. 'new_abboip.txt'. The format of each mobility trace file is the following - each line contains [latitude, longitude, occupancy, time], e.g.: [37.75134 -122.39488 0 1213084687], where latitude and longitude are in decimal degrees, occupancy shows if a cab has a fare (1 = occupied, 0 = free) and time is in UNIX epoch format.


# Data processing Framework
<img src="https://github.com/VadymBoikov/MapCab2/tree/master/images/raw_data.png" alt="alt text" width="500" height="300">


# How to run this Pipeline

0) Preprocess raw data into single file to simulate realtime data flow ( spark-submit --class mapCab.single_ordered_file --master yarn-client target/jar.jar input output )
1) Run kafka producer ( python kafka/producer.py )
2) Run Spark aggregation job ( spark-submit --class mapCab.prepare_data --master yarn-client target/jar.jar input output )
3) Run Hive scripts ( from hive directory)
4) Run Spark Spark Streaming Job (spark-submit --master yarn-client --class mapCab.mapOnline target/jar.jar 10.128.0.5:2181 kafka_group topic_name)
5) Run Flask web app ( > export FLASK_APP=run.py >flask run)


# Images
<img src="realtime.png" alt="alt text" width="500" height="300">