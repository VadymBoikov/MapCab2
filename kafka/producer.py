# Kafka producer that reads the input data in a loop in order to simulate real time events
import os
import sys
from kafka import KafkaClient, KeyedProducer, SimpleConsumer
from datetime import datetime
import time

kafka = KafkaClient("10.128.0.6:9092")
source_file = '/home/vadym_boikov/ordered_cabs.txt'

def genData(topic):
    producer = KeyedProducer(kafka)
    while True:
        with open(source_file) as f:
            for line in f:
                key = line.split(" ")[0]
                producer.send(topic, key, line.rstrip()) 
	        time.sleep(0.1)  # Creating some delay to allow proper rendering of the cab locations on the map
        
        source_file.close()

genData("one_cab")
