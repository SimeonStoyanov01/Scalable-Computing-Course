package com.rug.SimulatorApp

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{EdgeContext, EdgeDirection, Edge, EdgeTriplet, Graph, VertexId, Pregel}
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.util.GraphGenerators
import com.rug.ants.LangtonAntModel.{Ant, Cell, Direction, CellUpdate}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.io.StringWriter
import org.apache.kafka.clients.producer.{Producer, Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer 
import java.util.Properties

object SimulatorApp {
  object MyUtils {
    @transient val objectMapper = new ObjectMapper() // Transient!
    objectMapper.registerModule(DefaultScalaModule)
  }

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    //props.put("bootstrap.servers", "ants-kafka.default.svc.cluster.local:9092")
    //props.put("bootstrap.servers", "localhost:9092")
    props.put("bootstrap.servers", "192.168.49.2:30092")
    props.put("acks", "all")
    // props.put("retries", 0)
    // props.put("batch.size", 16384)
    // props.put("linger.ms", 1)
    // props.put("buffer.memory", 33554432)
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)

    @transient lazy val producer: Producer[String, String] = new KafkaProducer[String, String](props)

    val checkpointDir = "s3a://checkpoints/"

    val spark = SparkSession.builder
      .appName("Simulator")
      // .config("spark.checkpoint.dir", checkpointDir) // Seems to not work
      .config("spark.graphx.pregel.checkpointInterval", 20)
      .getOrCreate()

    val sc: SparkContext = spark.sparkContext
    println("setting checkpointing")
    //println(s"ROBIN: ${spark.conf.get("spark.hadoop.fs.s3a.endpoint")}")
    sc.setCheckpointDir(checkpointDir)
    println("done setting checkpointing")
    // sc.setLogLevel("DEBUG")
    // import spark.implicits._

    val gridSize = 100

    val emptygraph: Graph[Cell, Direction.Value] = constructGridGraph(gridSize, spark)

    var graph = emptygraph.mapVertices((vertexId, cell) => {
      // val rowInd = (vertexId / gridSize).toInt 
      // val colInd = (vertexId % gridSize).toInt 
      // 
      // if (vertexId == 130) { 
      //   Cell(false, Set(Ant(Direction.South)), rowInd, colInd, 0) 
      // } else {
      //   Cell(false, Set.empty[Ant], rowInd, colInd, 0)
      // }
      cell.copy(
        ants = if(
          vertexId == 5050
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
      producer.send(new ProducerRecord[String, String]("new-ants2", "lmaoheaderamirite", json));
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
        val srcUpdate = new CellUpdate(Some(!triplet.dstAttr.colour), Some(Set()), Some(triplet.srcAttr.time))
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
            assume(x==y, "Error: Invariant not satisfied: One cell recieved multiple different colour updates.")
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
    println(s"ROBIN: starting pregel")
    val finalGraph = Pregel(graph, new CellUpdate(None, None, None), 10000)(
      handleIncomingAnts, msgAnts, mergeCellUpdates)   
    println(s"ROBIN: done pregelling")
      
    printPrettyGrid(finalGraph, gridSize)

    // while(true){
    //scala.io.StdIn.readLine() // Hack for keeping spark open
    // }

    spark.stop()
    producer.close()
  }

  def createGrid(sc: SparkContext, n: Int): RDD[Edge[Direction.Value]] = {
    val edgesRDD: RDD[Edge[Direction.Value]] = sc.parallelize(0 until (n * n)).flatMap { vertexIdLong =>
      val vertexId = vertexIdLong.toLong
      var neighborEdges = Seq[Edge[Direction.Value]]()

      // Edge to East (right)
      if ((vertexId + 1) < (n * n) && (vertexId % n) < (n - 1)) { // Not last column and within bounds
        neighborEdges = neighborEdges ++ Seq(
          Edge(vertexId, vertexId + 1, Direction.East),
          Edge(vertexId + 1, vertexId, Direction.West) // Reverse direction
        )
      }

      // Edge to South (down)
      if ((vertexId + n) < (n * n)) { // Within bounds
        neighborEdges = neighborEdges ++ Seq(
          Edge(vertexId, vertexId + n, Direction.South),
          Edge(vertexId + n, vertexId, Direction.North) // Reverse direction
        )
      }
      neighborEdges
    }
    edgesRDD
  }
  def printPrettyGrid(graph: Graph[Cell, Direction.Value], n: Int): Unit = {
    val vertices = graph.vertices.collect().sortBy(_._1)

    for (row <- 0 until n) {
      for (col <- 0 until n) {
        val vertexId = (row * n + col).toLong
        val cellInfo = vertices.find(_._1 == vertexId).map(_._2).getOrElse(new Cell(false, Set(), 0, 0, 0))
        print(s"${cellInfo.display} ")
      }
      println()
    }
  }
  def constructGridGraph(n: Int, spark: SparkSession): Graph[Cell, Direction.Value] = {
    import spark.implicits._

    // 1. Create RDD of Cells (Vertices)
    val verticesRDD: RDD[(VertexId, Cell)] = spark.sparkContext.parallelize(0 until n).flatMap { rowInd =>
        (0 until n).map { colInd =>
            val vertexId: VertexId = rowInd.toLong * n + colInd // Unique vertex ID based on row and col
            (vertexId, Cell(false, Set.empty[Ant], rowInd, colInd, 0)) // Initial cells are white and empty
        }
    }

    // 2. Create RDD of Edges
    val edgesRDD: RDD[Edge[Direction.Value]] = verticesRDD.flatMap { case (vertexId, cell) =>
        val rowInd = cell.rowInd
        val colInd = cell.colInd
        val neighborIndices = Seq(
            (rowInd - 1, colInd, Direction.North),
            (rowInd + 1, colInd, Direction.South),
            (rowInd, colInd - 1, Direction.West),
            (rowInd, colInd + 1, Direction.East)
        )

        neighborIndices.flatMap { case (neighborRow, neighborCol, direction) =>
            if (neighborRow >= 0 && neighborRow < n && neighborCol >= 0 && neighborCol < n) {
                val srcVertexId = vertexId
                val dstVertexId: VertexId = neighborRow.toLong * n + neighborCol
                Some(Edge(srcVertexId, dstVertexId, direction))
            } else {
                None // Filter out of bounds edges
            }
        }
    }

    // 3. Construct the Graph
    Graph(verticesRDD, edgesRDD)
    }
}
