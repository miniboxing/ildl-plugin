package test

import ildl._

object GCDTest {

  object IntPairAsLong extends RigidTransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = ???
    def toHigh(l: Long @high): (Int, Int) = ???
  }

  object IntAsLong extends RigidTransformationDescription {
    type High = Int
    type Repr = Long
    def toRepr(pair: Int): Float @high = ???
    def toHigh(l: Float @high): Int = ???
  }

  adrt(IntPairAsLong) {
    adrt(IntAsLong) {
      var n1 = (1, 0)
      var n2 = 1
      // should produce an error, even though
      // both high types are represented as Long:
      n2 = n1
    }
  }
}
