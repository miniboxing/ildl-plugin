package ildl
package benchmark
package hamming
package step3

/** An array-based ring buffer used as a queue.
 *  This is the version that stores Long. */
class FunnyQueue {

  private[this] final val MAX = 6000
  private[this] val array = new Array[Long](MAX)
  private[this] var index_start = 0
  private[this] var index_stop = 0

  def enqueue(l: Long): Unit = {
    array(index_stop) = l
    index_stop = (index_stop + 1) % MAX
  }

  def dequeue(): Long = {
    val res = array(index_start)
    index_start = (index_start + 1) % MAX
    res
  }

  def head(): Long =
    array(index_start)
}