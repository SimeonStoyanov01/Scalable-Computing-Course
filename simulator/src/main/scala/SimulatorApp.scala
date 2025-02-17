import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{EdgeContext, EdgeDirection, Edge, EdgeTriplet, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.util.GraphGenerators
import com.rug.ants.LangtonAntModel.{Ant, Cell, Direction}

object SimulatorApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.appName("Simulator").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    import spark.implicits._

    val emptyCell = new Cell(false, Set())

    val gridSize = 6
    val edges = createGrid(sc, gridSize)

    val emptygraph: Graph[Cell, Direction.Value] = Graph.fromEdges(edges, emptyCell)

    val graph = emptygraph.mapVertices((id, _) =>
    if (id == 1) new Cell(false, Set(new Ant(Direction.South))) else new Cell(false, Set()))

    def handleIncomingAnts(id: VertexId, cell: Cell, ants: Set[Ant]): Cell 
      = if (cell.ants.isEmpty) {
        new Cell(cell.colour, ants) 
      } else {
        new Cell(!cell.colour, ants)
      }

    def msgAnts(triplet: EdgeContext[Cell, Direction.Value, Set[Ant]]) {
      triplet.sendToDst(triplet.srcAttr.ants.filter(ant => ant.direction == triplet.attr))
    }

    def mergeAnts(a: Set[Ant], b: Set[Ant]): Set[Ant] = a ++ b

    println(graph.vertices.collect.mkString("\n"))

    // Performs one step of simulation
    val messages = graph.aggregateMessages[Set[Ant]](msgAnts, mergeAnts)

    val result = graph.joinVertices(messages)(handleIncomingAnts)

    println(result.vertices.collect.mkString("\n"))
    printPrettyGrid(result, gridSize)

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
        val cellInfo = vertices.find(_._1 == vertexId).map(_._2).getOrElse(new Cell(false, Set()))
        print(s"${cellInfo.display} ")
      }
      println()
    }
  }
}
