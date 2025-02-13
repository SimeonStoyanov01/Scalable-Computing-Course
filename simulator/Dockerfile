FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0

WORKDIR /app

COPY . .

RUN sbt package

FROM apache/spark

WORKDIR /app

COPY <<EOF ./README.md
bla bla bla
la la al
EOF

COPY --from=0 /app/target/scala-2.12/simple-project_2.12-1.0.jar out.jar

CMD ["/opt/spark/bin/spark-submit", "out.jar"]