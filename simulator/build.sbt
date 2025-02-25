name := "Simulator"
organization := "com.rug"
version := "1.0"

scalaVersion := "2.12.18"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.4"

libraryDependencies += "org.apache.spark" %% "spark-graphx" % "3.5.4"

libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "3.8.1"

dependencyOverrides += "com.github.luben" % "zstd-jni" % "1.5.5-4" 
//diff