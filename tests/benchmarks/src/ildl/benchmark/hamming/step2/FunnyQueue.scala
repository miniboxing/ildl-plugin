package ildl
package benchmark
package hamming
package step2

// use java.lang.Long, so it doesn't get unboxed
import scala.{Long => _}
import java.lang.Long

/** An array-based ring buffer used as a queue.
 *  This is the version that stores java.lang.Long-s. */
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