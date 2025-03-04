name := "Simulator"
organization := "com.rug"
version := "1.0"

scalaVersion := "2.12.18"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.4" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-graphx" % "3.5.4" % "provided"

// libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.5.4"

// libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.4"

// libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.18.2"

// libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.18.2"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.4.1"

libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.4.1"

//libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "3.4.1"

// libraryDependencies += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.18.2"

// libraryDependencies += "org.apache.kafka" %% "kafka-streams" % "3.9.0"

// libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "3.9.0"

// libraryDependencies += "org.apache.kafka" % "kafka_2.13" % "3.9.0"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.9.0"


// dependencyOverrides += "com.github.luben" % "zstd-jni" % "1.5.5-4" 
// assemblyMergeStrategy := {
//   case PathList("META-INF", xs @ _*) => MergeStrategy.discard  // Discard META-INF files (often duplicates)
//   case path if path.endsWith("module-info.class") => MergeStrategy.discard //added to remove module-info errors
//   case "io.netty.versions.properties" => MergeStrategy.discard // added to remove netty errors
//   case "Log4j2Plugins.dat" => MergeStrategy.discard
//   case x => MergeStrategy.first // Take the first occurrence for everything else
// }
//assembly / assemblyMergeStrategy := {
//  case _ => MergeStrategy.first
//  case x =>
//    val oldStrategy = (assembly / assemblyMergeStrategy).value
//    oldStrategy(x)
//} 
ThisBuild / assemblyMergeStrategy := {
  case PathList("org", xs @ _*)         => MergeStrategy.first
  case PathList("javax", xs @ _*)         => MergeStrategy.first
//  case PathList("METAINF", xs @ _*)         => MergeStrategy.first
  case x if x.endsWith("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.discard

  //case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  //case "application.conf"                            => MergeStrategy.concat
  //case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

