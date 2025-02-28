FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0

WORKDIR /app

COPY project /app/project
COPY build.sbt .

RUN sbt update

COPY src /app/src

# RUN sbt package
RUN sbt assembly

# CMD ["/bin/bash"]

FROM apache/spark

WORKDIR /app

# COPY --from=0 /app/target/scala-2.12/simulator_2.12-1.0.jar out.jar
COPY --from=0 /app/target/scala-2.12/Simulator-assembly-1.0.jar out.jar

# RUN curl https://repo1.maven.org/maven2/org/apache/spark/spark-sql-kafka-0-10_2.12/3.5.4/spark-sql-kafka-0-10_2.12-3.5.4.jar --output /opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar
# RUN curl https://repo1.maven.org/maven2/org/apache/spark/spark-sql-kafka-0-10_2.12/3.5.4/spark-sql-kafka-0-10_2.12-3.5.4.jar --output /opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar
# RUN curl https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.9.0/kafka-clients-3.9.0.jar --output /opt/spark/jars/kafka-clients-3.9.0.jar
# RUN curl https://repo1.maven.org/maven2/org/apache/kafka/kafka-streams/3.9.0/kafka-streams-3.9.0.jar --output /opt/spark/jars/kafka-streams-3.9.0.jar

# CMD ["/opt/spark/bin/spark-submit", "--verbose", "--class", "com.rug.SimulatorApp.SimulatorApp", "--jars", "/opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar", "out.jar"]
# CMD ["/opt/spark/bin/spark-submit", "--verbose", "--jars", "/opt/spark/jars/spark-sql-kafka-0-10_2.12-3.5.4.jar,/opt/spark/jars/kafka-clients-3.9.0.jar,kafka-clients-3.9.0.jar", "out.jar"]
CMD ["/opt/spark/bin/spark-submit", "--verbose", "out.jar"]
# CMD ["/opt/spark/bin/spark-submit", "--verbose", "--packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.1", "out.jar"]
# CMD ["/bin/bash"]