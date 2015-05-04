package ildl
package tests
package constructors

object ArrayOfPairToPairOfArray extends TransformationDescription {

  // Main coercions:
  def toRepr(high: Array[(Int, Int)]): (Array[Int], Array[Int]) @high = ???
  def toHigh(repr: (Array[Int], Array[Int]) @high): Array[(Int, Int)] = ???
  // TODO: Can we prevent these guys from being introduced?
  // TODO: We should have a 3rd array of Boolean, to capture `null` values

  // Hijack the constructor:
  def ctor_Array(size: Int): (Array[Int], Array[Int]) @high = (new Array[Int](size), new Array[Int](size))
  
  // Hijack methods:
  def extension_apply(recv: (Array[Int], Array[Int]) @high, idx: Int) =
    (recv._1(idx), recv._2(idx))
  def extension_update(recv: (Array[Int], Array[Int]) @high, idx: Int, value: (Int, Int)) = {
    assert(value != null, "null is not yet supported!")
    recv._1(idx) = value._1
    recv._2(idx) = value._2
  }
  def extension_length(recv: (Array[Int], Array[Int]) @high) =
    recv._1.length
}

object Test {
  def main(args: Array[String]): Unit = {
    adrt(ArrayOfPairToPairOfArray) {
      val ar = new Array[(Int, Int)](3)
      ar(0) = (1, 5)
      ar(1) = (2, 4)
      ar(2) = (3, 3)
      // TODO: Sort by 2nd element of the pair

      for (i <- 0 until ar.length)
        println(ar(i))
    }
  }
}
