- make tabke with trips 

CREATE TABLE IF NOT EXISTS trip_tbl(
    tripId STRING,
    cabid STRING,
    year INT,
    month INT,
    day INT,
    dow STRING,
    timestamp INT,
    trip INT,
    idle INT,
    lat FLOAT,
    lng FLOAT,
    duration INT    )
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ' ';


INSERT OVERWRITE TABLE trip_tbl
SELECT tripId, cabid, year, month, day, dow, timestamp, trip, idle, lat, lng, duration
FROM (
    SELECT tripId, cabid, timestamp, year, month, day, dow, trip, idle, lat, lng,
       LEAD(timestamp, 1, 0) OVER (PARTITION BY cabid ORDER BY timestamp) - timestamp AS duration
    FROM (
        SELECT concat(cabid, '_', timestamp) AS tripId, cabid, timestamp,
          pick AS trip, drop AS idle, lat, lng, year, month, day, dow
        FROM master_tbl
        WHERE pick = 1 OR drop = 1) t) view
    WHERE duration > 0;
