package test

import ildl._

object GCDTest {

  object IntPairAsLong extends TransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = ???
    def fromRepr(l: Long @high): (Int, Int) = ???
  }

  adrt(IntPairAsLong) {
    // The accessor should be specialized as well
    val n1 = (1, 2)
  }
}
