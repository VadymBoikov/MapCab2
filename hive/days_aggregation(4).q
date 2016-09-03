
drop table if exists hour_agg;
drop table if exists day_agg;
drop table if exists dow_agg;
drop table if exists month_agg;

-- Hourly aggregations
CREATE TABLE hour_agg
AS SELECT * FROM
(SELECT hour, day, month, year, dow, AVG(occ) as occ, SUM(pick) as picks, SUM(drop) as drops, sum(distance) as distance
FROM master_tbl
GROUP BY year, month, dow, day, hour
ORDER BY hour ASC, day ASC, month ASC, year ASC ) a;

-- Daily aggregations
CREATE TABLE day_agg
AS SELECT * FROM
(SELECT day, month, year, AVG(occ) as occ, SUM(pick) as picks, SUM(drop) as drops, sum(distance) as distance
FROM master_tbl
GROUP BY year, month, day
ORDER BY day ASC, month ASC, year ASC ) b;

-- Day of Week aggregations
CREATE TABLE dow_agg
AS SELECT * FROM
(SELECT year, month, dow, hour, AVG(occ) as occ, AVG(picks) as picks, AVG(drops) as drops, sum(distance) as distance
FROM hour_agg
GROUP BY year, month, dow, hour
ORDER BY dow ASC, month ASC, year ASC ) c;

-- Monthly aggregations
CREATE TABLE month_agg
AS SELECT * FROM
(SELECT year, month, AVG(occ) as occ, SUM(pick) as picks, SUM(drop) as drops, sum(distance) as distance
FROM master_tbl GROUP BY year, month
ORDER BY year, month ASC, year ASC ) d;
