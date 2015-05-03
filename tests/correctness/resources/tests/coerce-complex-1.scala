package test

import ildl._

object GCDTest {

  object IntPairAsLong extends RigidTransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = ???
    def toHigh(l: Long @high): (Int, Int) = ???
  }

  object IntPairAsFloat extends RigidTransformationDescription {
    type High = (Int, Int)
    type Repr = Float
    def toRepr(pair: (Int, Int)): Float @high = ???
    def toHigh(l: Float @high): (Int, Int) = ???
  }

  adrt(IntPairAsFloat) {
    val n1 = (1, 0)
  }
  adrt(IntPairAsLong) {
    val n3 = n1
  }
}
