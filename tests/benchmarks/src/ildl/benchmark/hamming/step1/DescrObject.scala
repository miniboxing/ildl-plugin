package ildl
package benchmark
package hamming
package step1

import collection.mutable.Queue

/**
 *  A transformation object that transforms the Queue[BigInt] to a [[FunnyQueue]].
 *  @see the comment in [[ildl.benchmark.hamming.HammingNumbers]] for more information
 */
object QueueOfLongAsFunnyQueue extends TransformationDescription {

  // coercions:
  def toRepr(in: Queue[BigInt]): FunnyQueue @high =
    throw new Exception("We shouldn't need this!")
  def toHigh(q: FunnyQueue @high): Queue[BigInt] =
    throw new Exception("We shouldn't need this!")

  // constructor:
  def ctor_Queue(): FunnyQueue @high =
    new FunnyQueue()


  // extension methods and implicits:
  def implicit_QueueWithEnqueue1_enqueue1(q: FunnyQueue @high)(bi: BigInt): Unit = {
    q.enqueue(bi)
  }

  def extension_enqueue(q: FunnyQueue @high, bis: BigInt*): Unit = {
    // we don't support more than one element :)
    assert(bis.size == 1)
    val bi = bis.apply(0)
    assert(bi.isValidLong)
    q.enqueue(bi.longValue())
  }

  def extension_dequeue(q: FunnyQueue @high): BigInt = q.dequeue()

  def extension_head(q: FunnyQueue @high): BigInt = q.head()
}