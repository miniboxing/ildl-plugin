package ildl.example.transformed
import scala.collection.mutable.Queue

class MyQueue {
  private[this] val array = new Array[Long](20000)
  private[this] var index_start = 0
  private[this] var index_stop = 0
  def enqueue1(l: Long) = {
    array(index_stop) = l
    index_stop = (index_stop + 1) % 20000
  }
  def dequeue1(): Long = {
    val res = array(index_start)
    index_start = (index_start + 1) % 20000
    res
  }
  def head1(): Long =
    array(index_start)
}

object QueueOfLongAsArrayOfLong {
  type In = Queue[Long]
  type Out = MyQueue
  def toRepr(in: Queue[Long]): MyQueue = new MyQueue
  def fromRepr(q: MyQueue): Queue[Long] = ???
  def extension_enqueue(q: MyQueue, l: Long): Unit = q.enqueue1(l)
  def extension_dequeue(q: MyQueue): Long = q.dequeue1()
  def extension_head(q: MyQueue): Long = q.head1()
}

// taken from http://rosettacode.org/wiki/Hamming_numbers#Scala
class Hamming extends Iterator[Long] {
  import scala.collection.mutable.Queue
  val q2: MyQueue = QueueOfLongAsArrayOfLong.toRepr(new Queue[Long])
  val q3: MyQueue = QueueOfLongAsArrayOfLong.toRepr(new Queue[Long])
  val q5: MyQueue = QueueOfLongAsArrayOfLong.toRepr(new Queue[Long])
  def enqueue(n: Long) = {
    QueueOfLongAsArrayOfLong.extension_enqueue(q2, n * 2)
    QueueOfLongAsArrayOfLong.extension_enqueue(q3, n * 3)
    QueueOfLongAsArrayOfLong.extension_enqueue(q5, n * 5)
  }
  def next = {
    val n: Long = QueueOfLongAsArrayOfLong.extension_head(q2) min QueueOfLongAsArrayOfLong.extension_head(q3) min QueueOfLongAsArrayOfLong.extension_head(q5)
    if (QueueOfLongAsArrayOfLong.extension_head(q2) == n) { QueueOfLongAsArrayOfLong.extension_dequeue(q2) }
    if (QueueOfLongAsArrayOfLong.extension_head(q3) == n) { QueueOfLongAsArrayOfLong.extension_dequeue(q3) }
    if (QueueOfLongAsArrayOfLong.extension_head(q5) == n) { QueueOfLongAsArrayOfLong.extension_dequeue(q5) }
    enqueue(n)
    n
  }
  def hasNext = true
  QueueOfLongAsArrayOfLong.extension_enqueue(q2,1)
  QueueOfLongAsArrayOfLong.extension_enqueue(q3,1)
  QueueOfLongAsArrayOfLong.extension_enqueue(q5,1)
}

object HammingTestTrans {

  def timed[T](op: () => T): T = {
    val start = System.currentTimeMillis
    var iter = 1000
    while (iter > 0) {
      op()
      iter -= 1
    }
    val stop  = System.currentTimeMillis
    println("The operation took " + (stop - start) + " us.")
    op()
  }

  def main(args: Array[String]): Unit = {
//    println(timed(() => (new Hamming().take(200)).toList))
    println(timed(() => (new Hamming().drop(2000)).next()))
  }
}