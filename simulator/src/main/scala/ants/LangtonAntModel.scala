package com.rug.ants.LangtonAntModel

object Direction extends Enumeration {
    val North, East, South, West = Value
}

class Ant(val direction: Direction.Value) extends Serializable {
    override def toString: String = s"Ant(direction=$direction)"
    def display: String = s"${direction.toString.head}"
}

class Cell(val colour: Boolean, val ants: Set[Ant]) extends Serializable {
    override def toString: String = s"Cell(colour=$colour, ants=$ants)"
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
