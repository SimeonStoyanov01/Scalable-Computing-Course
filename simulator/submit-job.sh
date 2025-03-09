#!/bin/sh
/opt/spark/bin/spark-submit \
  --name "ants-simulator" \
  --verbose \
  --class "com.rug.SimulatorApp.SimulatorApp" \
  --conf "spark.hadoop.fs.s3a.endpoint=http://localhost:9000" \
  --conf "spark.hadoop.fs.s3a.access.key=minio" \
  --conf "spark.hadoop.fs.s3a.secret.key=minio123" \
  --conf "spark.hadoop.fs.s3a.path.style.access=true" \
  --conf "spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem" \
  "target/scala-2.12/Simulator-assembly-1.0.jar"
  # --packages "org.apache.kafka:kafka-clients:3.9.0,org.apache.hadoop:hadoop-client:3.3.4,org.apache.hadoop:hadoop-aws:3.3.4" \
  # "target/scala-2.12/simulator_2.12-1.0.jar"
  #--jars "/home/s4617096/hadoop-3.4.1/share/hadoop/tools/lib/bundle-2.24.6.jar,/home/s4617096/hadoop-3.4.1/share/hadoop/tools/lib/hadoop-aws-3.4.1.jar" \
  #--conf "spark.hadoop.fs.s3a.endpoint=http://myminio-hl.myminio.svc.cluster.local:9000" \
