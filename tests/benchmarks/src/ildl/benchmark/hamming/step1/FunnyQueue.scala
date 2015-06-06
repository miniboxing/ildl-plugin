package ildl
package benchmark
package hamming
package step1

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-%7E-Efficient-Collections
//

/** An array-based ring buffer used as a queue.
 *  This is the version that stores BigInts. */
class FunnyQueue {

  private[this] final val MAX = 6000
  private[this] val array = new Array[BigInt](MAX)
  private[this] var index_start = 0
  private[this] var index_stop = 0

  def enqueue(l: BigInt): Unit = {
    array(index_stop) = l
    index_stop = (index_stop + 1) % MAX
  }

  def dequeue(): BigInt = {
    val res = array(index_start)
    index_start = (index_start + 1) % MAX
    res
  }

  def head(): BigInt =
    array(index_start)
}