package ildl
package benchmark
package hamming

import collection.mutable.Queue

object QueueOfLongAsFunnyQueue extends RigidTransformationDescription {

  type High = Queue[Long]
  type Repr = FunnyQueue

  def toRepr(in: Queue[Long]): FunnyQueue @high = {
    assert(in.isEmpty, "Cannot start from a non-empty queue!")
    new FunnyQueue
  }

  def toHigh(q: FunnyQueue @high): Queue[Long] = {
    assert(false, "We shouldn't need this!")
    ???
  }

  def implicit_QueueWithEnqueue1_enqueue1(q: FunnyQueue @high)(bi: Long): Unit = {
    q.enqueue(bi.toLong)
  }

  def extension_enqueue(q: FunnyQueue @high)(bi: Long*): Unit = {
    // we don't support more than one element :)
    assert(bi.size == 1)
    q.enqueue(bi.apply(0))
  }

  def extension_dequeue(q: FunnyQueue @high): Long = q.dequeue()
  def extension_head(q: FunnyQueue @high): Long = q.head()
}