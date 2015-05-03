package tests

import ildl._

object Test {
  
  object ListAsVector extends TransformationDescription {
    def toRepr[T](list: List[T]): Vector[T] @high = list.toVector
    def toHigh[T](vec: Vector[T] @high): List[T] = vec.toList
  }

  def main(args: Array[String]): Unit = {
    adrt(ListAsVector) {
      val l1 = List(1,2,3)
      val l2 = l1
      val l3: Any = l1
    }
    println(l3)
    println("OK")
  }
}
