package test

import ildl._

object GCDTest {

  object IntPairAsLong extends TransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = ???
    def fromRepr(l: Long @high): (Int, Int) = ???
  }

  object IntPairAsFloat extends TransformationDescription {
    type High = (Int, Int)
    type Repr = Float
    def toRepr(pair: (Int, Int)): Float @high = ???
    def fromRepr(l: Float @high): (Int, Int) = ???
  }

  adrt(IntPairAsLong) {
    def test(): (Int, Int) = {
      adrt(IntPairAsFloat) {
        val n1 = (1, 0)
      }
      val n2 = (2, 3)
      n1
    }
  }
}
