package test

import ildl._
import scala.BigInt
import collection.mutable.Queue

object ScopesCollaboratingTest {

  object BigIntAsLong extends TransformationDescription {
    def toRepr(high: BigInt): Long @high = {
      assert(high.isValidLong)
      high.longValue()
    }
    def toHigh(repr: Long @high): BigInt = BigInt(repr)

// These guys require Java8:
//    def extension_*(recv: Long @high, other: Long @high) =
//      // note: Math.multiplyExact requires Java 8
//      java.lang.Math.multiplyExact(recv, other)
//
//    def extension_+(recv: Long @high, other: Long @high) =
//      // note: Math.multiplyExact requires Java 8
//      java.lang.Math.addExact(recv, other)
  }

  object BigIntQueueAsLongQueue extends TransformationDescription {

    def toRepr(high: Queue[BigInt]): Queue[Long] @high =
      high.map(x => { assert(x.isValidLong); x.longValue() })

    def toHigh(repr: Queue[Long] @high): Queue[BigInt] =
      repr.map(x => BigInt(x))

    def extension_apply(queue: Queue[Long] @high, idx: Int): Long @high(BigIntAsLong) =
      queue(idx)
  }

 adrt(BigIntAsLong) {
   adrt(BigIntQueueAsLongQueue) {
     val q = Queue(BigInt(1), BigInt(2))
     val d = q(0)
   }
  }
}
