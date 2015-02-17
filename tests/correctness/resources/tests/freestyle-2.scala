package tests

import ildl._

object Test {
  
  object ListAsVector extends FreestyleTransformationDescription {
    def toRepr[T](list: List[T]): Vector[T] @high = list.toVector
    // notice: `fromRepr` only accepts `Vector[Long]`:
    def fromRepr(vec: Vector[Long] @high): List[Long] = vec.toList
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
