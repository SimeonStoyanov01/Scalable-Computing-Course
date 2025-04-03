package com.rug.SimulatorApp

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{EdgeContext, EdgeDirection, Edge, EdgeTriplet, Graph, VertexId, Pregel}
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.util.GraphGenerators
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import java.io.StringWriter
import org.apache.kafka.clients.producer.{Producer, Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.clients.consumer.{Consumer, KafkaConsumer, ConsumerRecords, ConsumerRecord}
import org.apache.kafka.common.serialization.{StringSerializer, StringDeserializer} 
import java.util.Properties
import java.util.Arrays
import scala.collection.JavaConverters._
import scala.util.{Try, Success, Failure}
import com.rug.ants.LangtonAntModel.{Ant, Cell, Direction, CellUpdate}
import com.rug.jobs.JobRequest
import java.time.Instant
import java.time.Duration

object SimulatorApp {
  object MyUtils {
    @transient val objectMapper = new ObjectMapper() // Transient!
    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  // Simulator Context
  object SimCont {
    val props = new Properties()
    props.put("bootstrap.servers", "kafka.default.svc.cluster.local:9092")
    //props.put("bootstrap.servers", "localhost:9092")
    //props.put("bootstrap.servers", "192.168.49.2:30092")
    props.put("acks", "all")
    // props.put("retries", 0)
    // props.put("batch.size", 16384)
    // props.put("linger.ms", 1)
    // props.put("buffer.memory", 33554432)
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    props.put("key.deserializer", classOf[StringDeserializer].getName)
    props.put("value.deserializer", classOf[StringDeserializer].getName)
    props.put("group.id", "ants-consumer1")

    @transient lazy val producer: Producer[String, String] = new KafkaProducer[String, String](props)
    @transient lazy val consumer: Consumer[String, String] = new KafkaConsumer[String, String](props)
    SimCont.consumer.subscribe(Arrays.asList("jobs"))
  }

  def main(args: Array[String]): Unit = {
    if (args.length != 6) {
      println("Waiting for jobs")
      while (true) {
        val jobs = SimCont.consumer.poll(2000)
        for (job <- jobs.asScala) {
          println(s"Received job=$job")
          SimCont.consumer.commitSync()
          Try {
            MyUtils.objectMapper.readValue(job.value(), classOf[JobRequest])
          } match {
            case Success(jobRequest) =>
              println(s"Parsed jobRequest=$jobRequest")
              runSimulation(jobRequest)
            case Failure(e) =>
              println(s"Failed to parse job request: ${e.getMessage}")
          }
        }
      }

      SimCont.producer.close()
    }

    if (args.length == 6) {
      val gridRows = args(0).toInt
      val gridCols = args(1).toInt
      val ants = args(2).toInt
      val numSteps = args(3).toInt
      val checkpointInterval = args(4).toInt
      val repeatSimulation = args(5).toInt
      for (i <- 1 to repeatSimulation) {
        runSimulation(new JobRequest(gridRows, gridCols, ants, Some(numSteps), Some(checkpointInterval)))
      }
    } else {
      println("Usage: SimulatorApp <gridRows> <gridCols> <ants> <numSteps> <checkpointInterval> <repeat>")
      // runSimulation(new JobRequest(100, 100, 1000)) // Default values
    }
  }

  def runSimulation(jobRequest: JobRequest) {
    println(s"Running simulation with jobRequest: $jobRequest")
    val gridRowSize = jobRequest.gridRows
    val gridColSize = jobRequest.gridCols
    val numAnts = jobRequest.ants
    val numSteps = jobRequest.numSteps.getOrElse(10)
    val checkpointInterval = jobRequest.checkpointInterval.getOrElse(25)

    val checkpointDir = "s3a://checkpoints/"

    val spark = SparkSession.builder
      .appName("Simulator")
      // .config("spark.checkpoint.dir", checkpointDir) // Seems to not work
      .config("spark.graphx.pregel.checkpointInterval", checkpointInterval)
      .getOrCreate()

    val sc: SparkContext = spark.sparkContext
    println("setting checkpointing")
    //println(s"ROBIN: ${spark.conf.get("spark.hadoop.fs.s3a.endpoint")}")
    sc.setCheckpointDir(checkpointDir)
    println("done setting checkpointing")
    // sc.setLogLevel("DEBUG")
    // import spark.implicits._
    val emptygraph: Graph[Cell, Direction.Value] = constructGridGraph(gridRowSize, gridColSize, spark)

    val antSquareSize = math.ceil(math.sqrt(numAnts)).toInt
    val antRowPeriod = Math.ceil((gridRowSize + 1).toDouble / (antSquareSize + 1)).toInt
    val antColPeriod = Math.ceil((gridColSize + 1).toDouble / (antSquareSize + 1)).toInt
    val antRowOffset = 0 
    val antColOffset = 0 

    var graph = emptygraph.mapVertices((vertexId, cell) => {
      cell.copy(
        ants = if(
          (cell.rowInd + antRowOffset) % antRowPeriod == antRowPeriod-1
          && (cell.colInd + antColOffset) % antColPeriod == antColPeriod-1
          && antSquareSize * ((cell.rowInd+antRowOffset)/antRowPeriod) + ((cell.colInd+1+antColOffset)/antColPeriod) <= numAnts
          // cell.rowInd == gridRowSize / 2 &&  cell.colInd == gridColSize / 2
          // || vertexId == 170
        ) {
          Set(Ant(Direction.South))
        } else {
          Set.empty[Ant]
        }
      )
    })

    def handleIncomingAnts(id: VertexId, cell: Cell, cellUpdate: CellUpdate): Cell = {
      val newCell = cell.copy(
        colour = cellUpdate.newColour.getOrElse(cell.colour),
        ants = cellUpdate.incomingAnts.getOrElse(cell.ants),
        time = cellUpdate.newTime.getOrElse(cell.time) + 1
      )
      val out = new StringWriter
      MyUtils.objectMapper.writeValue(out, newCell)
      val json = out.toString()
      println(s"ROBIN: SENDING $json")
      SimCont.producer.send(new ProducerRecord[String, String]("langton_ant_updates", "lmaoheaderamirite", json));
      newCell
    }

    def antRule(cell: Cell, direction: Direction.Value): Set[Ant] = {
      val newAnts = if(cell.colour) { // Black square
        cell.ants.map(ant => new Ant(ant.direction.rotateCounterClockwise))
      } else { // White square
        cell.ants.map(ant => new Ant(ant.direction.rotateClockwise))
      }
      newAnts.filter(ant => ant.direction == direction)
    }

    def msgAnts(triplet: EdgeTriplet[Cell, Direction.Value]): Iterator[(VertexId, CellUpdate)] = {
      val dirAnts = antRule(triplet.srcAttr, triplet.attr)
      if (!dirAnts.isEmpty) {
        val dstUpdate = new CellUpdate(None, Some(dirAnts), Some(triplet.srcAttr.time))
        val srcUpdate = new CellUpdate(Some(!triplet.srcAttr.colour), Some(Set()), Some(triplet.srcAttr.time))
        Iterator((triplet.dstId, dstUpdate), (triplet.srcId, srcUpdate))
      } else {
        Iterator()
      }
    }
    
    def mergeCellUpdates(a: CellUpdate, b: CellUpdate): CellUpdate = 
      new CellUpdate(
        (a.newColour, b.newColour) match {
          case (Some(x), None) => Some(x)
          case (None, Some(x)) => Some(x)
          case (None, None) => None
          case (Some(x), Some(y)) => {
            //assume(x==y, s"Error: Invariant not satisfied: One cell recieved multiple different colour updates. Recieved ($a) and ($b).")
            if(x!=y) {
              println(s"ROBIN Error: Invariant not satisfied: One cell recieved multiple different colour updates. Recieved ($a) and ($b).")
            }
            Some(x)
          }
        },
        (a.incomingAnts, b.incomingAnts) match {
          case (Some(x), None) => Some(x)
          case (None, Some(y)) => Some(y)
          case (None, None) => None
          case (Some(x), Some(y)) => Some(x ++ y)
        },
        (a.newTime, b.newTime) match {
          case (Some(x), None) => Some(x)
          case (None, Some(x)) => Some(x)
          case (None, None) => None
          case (Some(x), Some(y)) => {
            assume(x==y, "Error: Invariant not satisfied: Time updates are not equal.")
            Some(x)
          }
        },
      )


    // println("pregellssss")
    val startTime = Instant.now()

    println(s"ROBIN: starting pregel")
    val finalGraph = graph.pregel(new CellUpdate(None, None, None), numSteps)(
      handleIncomingAnts, msgAnts, mergeCellUpdates)
    println(s"ROBIN: done pregelling")
      
    printPrettyGrid(finalGraph, gridRowSize, gridColSize)

    val endTime = Instant.now()

    val duration = Duration.between(startTime, endTime)

    println(s"Simulation time: ${duration.toMillis} milliseconds")

    //spark.stop()
  }

  def printPrettyGrid(graph: Graph[Cell, Direction.Value], rowSize: Int, colSize: Int): Unit = {
    val vertices = graph.vertices.collect().sortBy(_._1)

    for (row <- 0 until rowSize) {
      for (col <- 0 until colSize) {
        val vertexId = (row * colSize + col).toLong
        val cellInfo = vertices.find(_._1 == vertexId).map(_._2).getOrElse(new Cell(false, Set(), 0, 0, 0))
        print(s"${cellInfo.display} ")
      }
      println()
    }
  }

  def constructGridGraph(rowSize: Int, colSize: Int, spark: SparkSession): Graph[Cell, Direction.Value] = {
    import spark.implicits._

    // 1. Create RDD of Cells (Vertices)
    val verticesRDD: RDD[(VertexId, Cell)] = spark.sparkContext.parallelize(0 until rowSize).flatMap { rowInd =>
        (0 until colSize).map { colInd =>
            val vertexId: VertexId = rowInd.toLong * colSize + colInd // Unique vertex ID based on row and col
            (vertexId, Cell(false, Set.empty[Ant], rowInd, colInd, 0)) // Initial cells are white and empty
        }
    }

    // 2. Create RDD of Edges
    val edgesRDD: RDD[Edge[Direction.Value]] = verticesRDD.flatMap { case (vertexId, cell) =>
        val rowInd = cell.rowInd
        val colInd = cell.colInd
        val neighborIndices = Seq(
            ((rowSize + rowInd - 1) % rowSize, colInd, Direction.North),
            ((rowInd + 1) % rowSize, colInd, Direction.South),
            (rowInd, (colInd - 1 + colSize) % colSize, Direction.West),
            (rowInd, (colInd + 1) % colSize, Direction.East)
        )

        neighborIndices.flatMap { case (neighborRow, neighborCol, direction) =>
            if (neighborRow >= 0 && neighborRow < rowSize && neighborCol >= 0 && neighborCol < colSize) {
                val srcVertexId = vertexId
                val dstVertexId: VertexId = neighborRow.toLong * colSize + neighborCol
                Some(Edge(srcVertexId, dstVertexId, direction))
            } else {
                None // Filter out of bounds edges
            }
        }
    }

    // Display vertices and their partitions
    println("\nROBIN--- Vertices RDD Partitioning ---")
    verticesRDD.mapPartitionsWithIndex { (partitionId, iterator) =>
      iterator.map(vertex => s"ROBINPartition: $partitionId, Vertex: $vertex")
    }.collect().foreach(println)

    // Display edges and their partitions with source and destination vertices
    println("\nROBIN--- Edges RDD Partitioning ---")
    edgesRDD.mapPartitionsWithIndex { (partitionId, iterator) =>
      iterator.map(edge => s"ROBINPartition: $partitionId, Edge: Source(${edge.srcId}) -> Destination(${edge.dstId}), Direction: ${edge.attr}")
    }.collect().foreach(println)

    // 3. Construct the Graph
    Graph(verticesRDD, edgesRDD)
  }
}
