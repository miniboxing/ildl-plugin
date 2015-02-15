package test

import ildl._

object GCDTest {

  object IntPairAsLong extends TransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = ???
    def fromRepr(l: Long @high): (Int, Int) = ???
  }

  object LongAsFloat extends TransformationDescription {
    type High = Long
    type Repr = Float
    def toRepr(pair: Long): Float @high = ???
    def fromRepr(l: Float @high): Long = ???
  }

 adrt(IntPairAsLong) {
    // the next scope will not activate, since we
    // don't currently support cascading transforms:
    adrt(LongAsFloat) {
      val n1 = (1, 0)
      val n3 = n1
    }
  }
}
