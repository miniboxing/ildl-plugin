package ildl
package benchmark
package hamming

/** An array-based ring buffer used as a queue */
class FunnyQueue {

  private[this] final val MAX = 20000
  private[this] val array = new Array[Long](MAX)
  private[this] var index_start = 0
  private[this] var index_stop = 0

  def enqueue(l: Long) = {
    array(index_stop) = l
    index_stop = (index_stop + 1) % 20000
  }

  def dequeue(): Long = {
    val res = array(index_start)
    index_start = (index_start + 1) % 20000
    res
  }

  def head(): Long =
    array(index_start)
}