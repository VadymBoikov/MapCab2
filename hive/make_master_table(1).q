-- create main table from external file. ( File was created withh Spark)


create external table main_table(
  uid string,
  lat float,
  lng float,
  occ int,
  timestamp int,
  distance float,
  pick int,
  drop int)
  ROW FORMAT delimited
  fields terminated by ','
  stored as textfile
  location '/jobs/prepared_data/';


CREATE TABLE master_tbl
AS SELECT uid as cabID,
year(from_unixtime(timestamp)) AS year,
month(from_unixtime(timestamp )) AS month,
day(from_unixtime(timestamp )) AS day,
hour(from_unixtime(timestamp )) AS hour,
from_unixtime(timestamp, 'EEE') as dow,
timestamp,
occ,
pick,
drop,
lat,
lng,
distance
FROM main_table;
