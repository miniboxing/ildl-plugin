package ildl
package benchmark
package hamming

import collection.mutable.Queue

object QueueOfBigIngAsFunnyQueue extends RigidTransformationDescription {

  type High = Queue[BigInt]
  type Repr = FunnyQueue

  def toRepr(in: Queue[BigInt]): FunnyQueue @high = {
    assert(in.isEmpty, "Cannot start from a non-empty queue!")
    new FunnyQueue
  }

  def fromRepr(q: FunnyQueue @high): Queue[BigInt] = {
    assert(false, "We shouldn't need this!")
    ???
  }

  def extension_enqueue(q: FunnyQueue @high)(bi: BigInt*): Unit = {
    // we don't support more than one element :)
    assert(bi.size == 1)
    q.enqueue(bi.apply(0).toLong)
  }

  def extension_dequeue(q: FunnyQueue @high): BigInt = q.dequeue()
  def extension_head(q: FunnyQueue @high): BigInt = q.head()
}