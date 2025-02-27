package com.rug.SimulatorApp

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{EdgeContext, EdgeDirection, Edge, EdgeTriplet, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.util.GraphGenerators
import com.rug.ants.LangtonAntModel.{Ant, Cell, Direction}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object SimulatorApp {
  object MyUtils {
    @transient val objectMapper = new ObjectMapper() // Transient!
    objectMapper.registerModule(DefaultScalaModule)

    def rowToJson(row: org.apache.spark.sql.Row): String = {
      try {
        objectMapper.writeValueAsString(row.getValuesMap(row.schema.fieldNames))
      } catch {
        case e: Exception =>
          println(s"Error converting row to JSON: $e")
          null
      }
    }

    val rowToJsonUDF = udf(rowToJson _) // Define the UDF here
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder.appName("Simulator").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    import spark.implicits._

    // val emptyCell = new Cell(false, Set())

    val gridSize = 20
    // val edges = createGrid(sc, gridSize)

    // val emptygraph: Graph[Cell, Direction.Value] = Graph.fromEdges(edges, emptyCell)
    val emptygraph: Graph[Cell, Direction.Value] = constructGridGraph(gridSize, spark)

    var graph = emptygraph.mapVertices((vertexId, oldCell) => {
      val rowInd = (vertexId / gridSize).toInt 
      val colInd = (vertexId % gridSize).toInt 
      //     printPrettyGrid(graph, gridSize) || vertexId == 170
      if (vertexId == 130) { 
        Cell(false, Set(Ant(Direction.South)), rowInd, colInd) 
      } else {
        Cell(false, Set.empty[Ant], rowInd, colInd)
      }
    })

    // var graph = emptygraph.mapVertices((id, _) =>
    // if (id == 6 || id == 8) new Cell(false, Set(new Ant(Direction.South))) else new Cell(false, Set()))

    def handleIncomingAnts(id: VertexId, cell: Cell, ants: Set[Ant]): Cell 
      = if (cell.ants.isEmpty) {
        new Cell(cell.colour, ants, cell.rowInd, cell.colInd) 
      } else {
        new Cell(!cell.colour, ants, cell.rowInd, cell.colInd)
      }
    
    def antRule(cell: Cell, direction: Direction.Value): Set[Ant] = {
      val newAnts = if(cell.colour) { // Black square
        cell.ants.map(ant => new Ant(ant.direction.rotateCounterClockwise))
      } else { // White square
        cell.ants.map(ant => new Ant(ant.direction.rotateClockwise))
      }
      newAnts.filter(ant => ant.direction == direction)
    }

    def msgAnts(triplet: EdgeContext[Cell, Direction.Value, Set[Ant]]) {
      // val dirAnts = triplet.srcAttr.ants.filter(ant => ant.direction == triplet.attr)
      val dirAnts = antRule(triplet.srcAttr, triplet.attr)
      if (!dirAnts.isEmpty) {
        triplet.sendToDst(dirAnts)
      }
    }

    def mergeAnts(a: Set[Ant], b: Set[Ant]): Set[Ant] = a ++ b

    def clearAnts(id: VertexId, cell: Cell): Cell 
      = if (cell.ants.isEmpty) {
        new Cell(cell.colour, Set(), cell.rowInd, cell.colInd) 
      } else {
        new Cell(!cell.colour, Set(), cell.rowInd, cell.colInd)
      }
    

    for (i <- 1 to 10000) {
      val messages = graph.aggregateMessages[Set[Ant]](msgAnts, mergeAnts).cache()

      graph = graph.mapVertices(clearAnts).cache()

      graph = graph.joinVertices(messages)(handleIncomingAnts).cache()
      printPrettyGrid(graph, gridSize)
    }

    graph.vertices.toDF
      // .withColumnRenamed("_2", "value")
      // .withColumn("value", col("_2").cast("string"))
      .withColumn("value", to_json(struct($"_2.*"))) 
      // .withColumn("value", udf(MyUtils.rowToJson _).apply(struct("*"))) // Apply the UDF
      .select("value") // Select only the JSON string column
      .write
      // .format("console")
      // .save()
      .format("kafka")
      .option("kafka.bootstrap.servers", "broker:29092")
      .option("topic", "quickstart.sampleData")
      .save()

    // while(true){
    //    scala.io.StdIn.readLine() // Hack for keeping spark open
    // }

    spark.stop()
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
        val cellInfo = vertices.find(_._1 == vertexId).map(_._2).getOrElse(new Cell(false, Set(), 0, 0))
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
            (vertexId, Cell(false, Set.empty[Ant], rowInd, colInd)) // Initial cells are white and empty
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
