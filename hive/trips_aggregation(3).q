-- Store the maximum durations for idle times per day and by cab into a table
INSERT OVERWRITE TABLE max_idle_dur
SELECT cabid, month, year, day, MAX(duration) as maxDur
FROM trip_tbl
WHERE idle = 1
GROUP BY cabid, month, year, day;

DROP TABLE IF EXISTS idle_trips;
CREATE TABLE idle_trips (
    tripId STRING,
    day INT,
    month INT,
    year INT,
    duration INT
   );

-- Select the tripIds with the max duration of idle times to identify cab and its shift
INSERT OVERWRITE TABLE idle_trips
SELECT tripId, day, month, year, duration FROM trip_tbl
WHERE trip_tbl.duration IN (
SELECT maxDur FROM max_idle_dur WHERE max_idle_dur.cabid = trip_tbl.cabid AND max_idle_dur.day = trip_tbl.day);

DROP TABLE IF EXISTS avg_trip_dur;
CREATE TABLE avg_trip_dur (
    year INT,
    month INT,
    dow STRING,
    avg_dur FLOAT);

-- Find out the average trip duration for a day of week
INSERT OVERWRITE TABLE avg_trip_dur
SELECT e.* FROM
(SELECT year, month, dow, avg(duration) AS avg_dur
FROM trip_tbl
WHERE trip = 1
GROUP BY year, month, dow) e ;
