package ildl
package benchmark
package hamming

import collection.mutable.Queue

object BigIntAsLong extends TransformationDescription {
  def toRepr(high: BigInt): Long @high = {
    assert(high.isValidLong)
    high.longValue()
  }
  def toHigh(repr: Long @high): BigInt = BigInt(repr)

  def extension_*(recv: Long @high, other: Long @high): Long @high =
    recv * other
}

object HammingNumbers {

  adrt(BigIntAsLong) {
    class HammingADRT {
      def x(n: BigInt*) = 1
      val n = BigInt(1)
      x(n * 2)
    }
  }

  def main(args: Array[String]): Unit = {
    new HammingADRT()
  }
}
