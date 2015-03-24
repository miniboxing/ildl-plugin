package test

import ildl._

object ScopePicklingTest {

  implicit class IntPairPickle(pair: (Int, Int)) {
    def pickle: Long = ???
    def unpickle(pickle: Long): (Int, Int) = ???
  }

  implicit class FloatPairPickle(pair: (Float, Float)) {
    def pickle: Long = ???
    def unpickle(pickle: Long): (Float, Float) = ???
  }

  object IntPairAsLong extends RigidTransformationDescription {
    type High = (Int, Int)
    type Repr = Long
    def toRepr(pair: (Int, Int)): Long @high = (pair._1.toLong << 32l) | (pair._2.toLong & 0xFFFFFFFFl)
    def fromRepr(l: Long @high): (Int, Int) = ((l >>> 32).toInt, (l & 0xFFFFFFFF).toInt)

    // pickling/unpickling
    def implicit_IntPairPickle_pickle(value: Long @high): Long = value
    def implicit_IntPairPickle_unpickle(value: Long @high, pickle: Long): Long @high = pickle
  }

  object FloatPairAsLong extends RigidTransformationDescription {
    type High = (Float, Float)
    type Repr = Long
    def toRepr(pair: (Float, Float)): Long @high = (java.lang.Float.floatToIntBits(pair._1).toLong << 32l) | (java.lang.Float.floatToIntBits(pair._2).toLong & 0xFFFFFFFFl)
    def fromRepr(l: Long @high): (Float, Float) = (java.lang.Float.intBitsToFloat((l >>> 32).toInt), java.lang.Float.intBitsToFloat((l & 0xFFFFFFFF).toInt))

    // pickling/unpickling
    def implicit_FloatPairPickle_pickle(value: Long @high): Long = ((java.lang.Float.intBitsToFloat((value >>> 32).toInt)).toInt.toLong << 32l) | ((java.lang.Float.intBitsToFloat((value & 0xFFFFFFFF).toInt)).toInt.toLong & 0xFFFFFFFFl)
    def implicit_FloatPairPickle_unpickle(value: Long @high, pickle: Long): Long @high = (java.lang.Float.floatToIntBits((pickle >>> 32).toInt).toLong << 32l) | (java.lang.Float.floatToIntBits((pickle & 0xFFFFFFFF).toInt).toLong & 0xFFFFFFFFl) 
  }

  def main(args: Array[String]): Unit = {
    adrt(IntPairAsLong) {
      adrt(FloatPairAsLong) {
        val n1 = (1, 0)
        val n2 = (3f, 4f)
        val n3 = n2.unpickle(n1.pickle)
        val n4 = n1.unpickle(n2.pickle)
      }
    }

    println("" + n1 + " and " + n3)
    println("" + n2 + " and " + n4)
  }
}
