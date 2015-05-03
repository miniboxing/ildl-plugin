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

  trait T {
    def foo(cp: (Int, Int)): (Int, Int) = ???
  }
  
  adrt(IntPairAsLong) {
    trait U extends T {
      // should have a bridge:
      override def foo(cp: (Int, Int)): (Int, Int) = ???
    }
  }

  adrt(IntPairAsLong) {
    trait V extends U {
      // should have one bridge:
      override def foo(cp: (Int, Int)): (Int, Int) = ???
    }
  }
 
  adrt(IntPairAsFloat) {
    trait W extends V {
      // should have two bridges:
      override def foo(cp: (Int, Int)): (Int, Int) = ???
    }
  }
}
