package test

import ildl._

object GCDTest {

  object IntPairAsLong extends RigidTransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = ???
    def fromRepr(l: Long @high): (Int, Int) = ???
  }

  object IntAsLong extends RigidTransformationDescription {
    type High = Int
    type Repr = Long
    def toRepr(pair: Int): Long @high = ???
    def fromRepr(l: Long @high): Int = ???
  }

  adrt(IntPairAsLong) {
    adrt(IntAsLong) {
      var n1 = (1, 0)
      var n2 = 1
      // the signature should take 2 longs and return a long:
      def foo(n1: (Int, Int), n2: Int): Int = ???
    }
  }
}
