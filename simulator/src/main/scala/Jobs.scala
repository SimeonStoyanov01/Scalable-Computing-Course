package com.rug.jobs

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

@JsonDeserialize
case class JobRequest(val gridRows: Int, val gridCols: Int, val ants: Int, val numSteps: Option[Int], val checkpointInterval: Option[Int], val waitBeforeRun: Option[Int]) extends Serializable {
}