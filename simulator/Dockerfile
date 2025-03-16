FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0

WORKDIR /app

COPY project /app/project
COPY build.sbt .

RUN sbt update

COPY src /app/src

# RUN sbt package
RUN sbt -J-Xmx4G assembly

# CMD ["/bin/bash"]

FROM apache/spark

WORKDIR /app

# COPY --from=0 /app/target/scala-2.12/simulator_2.12-1.0.jar out.jar
#USER root
COPY --from=0 /app/target/scala-2.12/Simulator-assembly-1.0.jar out.jar

USER root
RUN chown -R 185:185 /app && chmod 777 /app/*
RUN mkdir /checkpoint
RUN touch /checkpoint/hello
RUN chown -R 185:185 /checkpoint && chmod 777 /checkpoint/*

WORKDIR /work-dir

RUN chown -R 185:185 /work-dir

USER spark

#RUN chown 185:185 out.jar && chmod o+w out.jar
#USER 185

# RUN cat /etc/passwd
# RUN cat /etc/group
# RUN chown spark:spark out.jar

# RUN curl https://repo1.maven.org/maven2/org/apache/spark/spark-sql-kafka-0-10_2.12/3.5.4/spark-sql-kafka-0-10_2.12-3.5.4.jar --output /opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar
# RUN curl https://repo1.maven.org/maven2/org/apache/spark/spark-sql-kafka-0-10_2.12/3.5.4/spark-sql-kafka-0-10_2.12-3.5.4.jar --output /opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar
# RUN curl https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.9.0/kafka-clients-3.9.0.jar --output /opt/spark/jars/kafka-clients-3.9.0.jar
# RUN curl https://repo1.maven.org/maven2/org/apache/kafka/kafka-streams/3.9.0/kafka-streams-3.9.0.jar --output /opt/spark/jars/kafka-streams-3.9.0.jar

# CMD ["/opt/spark/bin/spark-submit", "--verbose", "--class", "com.rug.SimulatorApp.SimulatorApp", "--jars", "/opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar", "out.jar"]
# CMD ["/opt/spark/bin/spark-submit", "--verbose", "--jars", "/opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar,/opt/spark/jars/kafka-clients-3.9.0.jar,kafka-clients-3.9.0.jar", "out.jar"]
# CMD ["/opt/spark/bin/spark-submit", "--verbose", "out.jar"]
# CMD ["/opt/spark/bin/spark-submit", "--verbose", "--packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.1", "out.jar"]
# CMD ["/bin/bash"]
CMD [ "/opt/spark/bin/spark-submit", \
  "--name", "ants-simulator", \
  "--verbose", \
  "--class", "com.rug.SimulatorApp.SimulatorApp", \
  "--conf", "spark.hadoop.fs.s3a.endpoint=http://myminio-hl.default.svc.cluster.local:9000", \
  "--conf", "spark.hadoop.fs.s3a.access.key=minio", \
  "--conf", "spark.hadoop.fs.s3a.secret.key=minio123", \
  "--conf", "spark.hadoop.fs.s3a.path.style.access=true", \
  "--conf", "spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem", \
  "local:///app/out.jar"] 
