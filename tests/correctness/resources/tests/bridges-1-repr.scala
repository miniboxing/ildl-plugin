package test

import ildl._

object GCDTest {

  object IntPairAsLong extends RigidTransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = ???
    def toHigh(l: Long @high): (Int, Int) = ???
  }

  trait T {
    def foo(cp: (Int, Int)): (Int, Int) = ???
  }
  
  adrt(IntPairAsLong) {
    trait U extends T {
      // should have a bridge:
      override def foo(cp: (Int, Int)): (Int, Int) = ???
    }
  }
}
