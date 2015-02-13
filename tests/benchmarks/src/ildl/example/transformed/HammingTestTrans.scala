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

object QueueOfBigIntAsArrayOfLong {
  type In = Queue[BigInt]
  type Out = MyQueue
  def toRepr(in: Queue[BigInt]): MyQueue = new MyQueue
  def fromRepr(q: MyQueue): Queue[BigInt] = ???
  def extension_enqueue(q: MyQueue, l: BigInt): Unit = q.enqueue1(l.toLong)
  def extension_dequeue(q: MyQueue): BigInt = q.dequeue1()
  def extension_head(q: MyQueue): BigInt = q.head1()
}

// taken from http://rosettacode.org/wiki/Hamming_numbers#Scala
class Hamming extends Iterator[BigInt] {
  import scala.collection.mutable.Queue
  val q2: MyQueue = QueueOfBigIntAsArrayOfLong.toRepr(new Queue[BigInt])
  val q3: MyQueue = QueueOfBigIntAsArrayOfLong.toRepr(new Queue[BigInt])
  val q5: MyQueue = QueueOfBigIntAsArrayOfLong.toRepr(new Queue[BigInt])
  def enqueue(n: BigInt) = {
    QueueOfBigIntAsArrayOfLong.extension_enqueue(q2, n * 2)
    QueueOfBigIntAsArrayOfLong.extension_enqueue(q3, n * 3)
    QueueOfBigIntAsArrayOfLong.extension_enqueue(q5, n * 5)
  }
  def next = {
    val n: BigInt = QueueOfBigIntAsArrayOfLong.extension_head(q2) min
                    QueueOfBigIntAsArrayOfLong.extension_head(q3) min
                    QueueOfBigIntAsArrayOfLong.extension_head(q5)
    if (QueueOfBigIntAsArrayOfLong.extension_head(q2) == n) { QueueOfBigIntAsArrayOfLong.extension_dequeue(q2) }
    if (QueueOfBigIntAsArrayOfLong.extension_head(q3) == n) { QueueOfBigIntAsArrayOfLong.extension_dequeue(q3) }
    if (QueueOfBigIntAsArrayOfLong.extension_head(q5) == n) { QueueOfBigIntAsArrayOfLong.extension_dequeue(q5) }
    enqueue(n)
    n
  }
  def hasNext = true
  QueueOfBigIntAsArrayOfLong.extension_enqueue(q2,1)
  QueueOfBigIntAsArrayOfLong.extension_enqueue(q3,1)
  QueueOfBigIntAsArrayOfLong.extension_enqueue(q5,1)
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