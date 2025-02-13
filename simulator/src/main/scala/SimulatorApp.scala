import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SparkSession
import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators


object SimulatorApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.appName("Simulator").getOrCreate()
    import spark.implicits._

    // A graph with edge attributes containing distances
    val graph: Graph[Long, Double] =
      GraphGenerators.logNormalGraph(spark.sparkContext, numVertices = 100).mapEdges(e => e.attr.toDouble)
    val sourceId: VertexId = 42 // The ultimate source
    // Initialize the graph such that all vertices except the root have distance infinity.
    val initialGraph = graph.mapVertices((id, _) =>
        if (id == sourceId) 0.0 else Double.PositiveInfinity)

// graph.vertices.filter { case (id, (name, pos)) => pos == "postdoc" }.count

    val sssp = initialGraph.pregel(Double.PositiveInfinity)(
      (id, dist, newDist) => math.min(dist, newDist), // Vertex Program
      triplet => {  // Send Message
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },
      (a, b) => math.min(a, b) // Merge Message
    )
    val df = sssp.vertices.map { case (id, dist) => dist }.toDF()
    .write
    .format("console")
    .save()
    println("asdkasjdlkjasldkj\n")
    // println(sssp.vertices.collect.mkString("\n"))

    spark.stop()
  }
}
