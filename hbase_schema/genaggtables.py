import happybase
import ast
import json 

connection = happybase.Connection('10.128.0.7')

connection.open()
print connection.tables()

master_table = connection.table('dow')
connection.disable_table('dow_stats')
connection.delete_table('dow_stats')

connection.create_table(
   'dow_stats',
   {'data':dict()}
)

statsTbl = connection.table('dow_stats')

for key, data in master_table.scan():
    metrics = {'picks': data['data:picks'], 'drops': data['data:drops'], 'occ': data['data:occ'], 'distance': data['data:distance']}
    new_key = key.split('-')[0]
    statsTbl.put(new_key, {'data:' + data['data:hour']: json.dumps(metrics)}) 

# Scan the aggregate table to store the sums as the last column for fast retrieval
for key, val in statsTbl.scan():
    val_ovr_hrs = [ast.literal_eval(val[col]) for col in val]
    TotPickups = sum(float(item['picks']) for item in val_ovr_hrs)
    TotDrops = sum(float(item['drops']) for item in val_ovr_hrs)
    AvgOcc = sum(float(item['occ']) for item in val_ovr_hrs)/float(len(val_ovr_hrs))
    AvgDist = sum(float(item['distance']) for item in val_ovr_hrs)/float(len(val_ovr_hrs))    
    metrics = {'TPickups':TotPickups, 'TDropoffs':TotDrops, 'Avocc':AvgOcc, 'Avdist':AvgDist}
    statsTbl.put(key, {'data:' + 'Totals':json.dumps(metrics)}) 

