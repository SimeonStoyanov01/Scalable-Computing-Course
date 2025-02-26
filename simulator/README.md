# Simulator

While we dont have any deployment running, you can run the simulation code with
```bash
docker build . -t simulator:latest
docker run simulator
```

This container submits a graph to kafka in json format where each message is a single cell in the graph. These kafka messages can be forwaded to MongoDB using the Kafka Connector. The entries in mongoDB can look like this:
```
  {
    _id: ObjectId('67bef5d076ee3521f1f25758'),
    ants: [ { direction: 'South' } ],
    colInd: Long('10'),
    colour: true,
    rowInd: Long('6')
  },
```

The kafka connection with mongoDB is unfortunately not currently implemented in this repo however the simulator does work as explained because I tested it using the setup presented in this guide https://www.mongodb.com/docs/kafka-connector/current/quick-start/#std-label-kafka-quick-start using the command:
```bash
curl -X POST \
     -H "Content-Type: application/json" \
     --data '
     {"name": "mongo-sink3-robinio",
      "config": {
         "connector.class":"com.mongodb.kafka.connect.MongoSinkConnector",
         "connection.uri":"mongodb://mongo1:27017/?replicaSet=rs0",
         "database":"quickstart",
         "collection":"topicData",
         "topics":"quickstart.sampleData",
         "key.converter.schemas.enable":false,
         "value.converter.schemas.enable":false,
         "key.converter":"org.apache.kafka.connect.json.JsonConverter",
         "value.converter":"org.apache.kafka.connect.json.JsonConverter"
         }
     }
     ' \
     http://connect:8083/connectors -w "\n"
```
to create the mongo sink.