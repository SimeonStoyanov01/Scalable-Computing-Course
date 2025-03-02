package com.rug.ants.LangtonAntModel
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

object Direction extends Enumeration {
    val North, East, South, West = Value

    implicit class DirectionValueOps(val dir: Value) extends AnyVal {
        def rotateClockwise: Value = dir match {
            case North => East
            case East => South
            case South => West
            case West => North
        }

        def rotateCounterClockwise: Value = dir match {
            case North => West
            case West => South
            case South => East
            case East => North
        }
    }
}

class DirectionType extends TypeReference[Direction.type] // Hack because Scala enums are wierd

case class Ant(@JsonScalaEnumeration(classOf[DirectionType]) direction: Direction.Value) extends Serializable {
    // override def toString: String = s"Ant(direction=$direction)"
    def display: String = s"${direction.toString.head}"
}

case class Cell(val colour: Boolean, val ants: Set[Ant], val rowInd: Int, val colInd: Int) extends Serializable {
    // override def toString: String = s"Cell(colour=$colour, ants=$ants)"
    def display: String = {
        if(ants.isEmpty) {
            if (colour) "(#)" else "[ ]"
        } else {
           var antDisplays = ants.map(_.display).mkString("") 
           if(colour) {
            s"($antDisplays)"
           } else {
            s"[$antDisplays]"
           }
        }
    }
}



case class CellUpdate(val newColour: Option[Boolean], val incomingAnts: Option[Set[Ant]]) extends Serializable {

}