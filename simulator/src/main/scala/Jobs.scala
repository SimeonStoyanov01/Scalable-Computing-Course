package com.rug.jobs

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

@JsonDeserialize
case class JobRequest(val gridRows: Int, val gridCols: Int /* TODO: , ants : ??? */) extends Serializable {
}