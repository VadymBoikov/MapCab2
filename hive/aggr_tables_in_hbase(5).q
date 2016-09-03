CREATE TABLE hbase_dow(
    rowkey STRING,
    hour STRING,
    occ FLOAT,
    picks FLOAT,
    drops FLOAT,
    distance FLOAT)
STORED BY "org.apache.hadoop.hive.hbase.HBaseStorageHandler"
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key, data:hour, data:occ, data:picks, data:drops, data:distance")
TBLPROPERTIES ("hbase.table.name" = "dow")


INSERT OVERWRITE TABLE hbase_dow 
SELECT CONCAT(year,'_',month,'_',dow, '-',hour), hour, occ, picks, drops, distance FROM dow_agg



CREATE TABLE IF NOT EXISTS idleTrips (
    tripId STRING,
    day INT,
    month INT,
    year INT,
    duration INT)

STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key, data:day, data:month, data:year, data:duration");

INSERT OVERWRITE TABLE idleTrips SELECT tripId, day, month, year, duration FROM idle_trips;