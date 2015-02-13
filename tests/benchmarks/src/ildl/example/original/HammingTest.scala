package ildl.example.original

// taken from http://rosettacode.org/wiki/Hamming_numbers#Scala
class Hamming extends Iterator[BigInt] {
  import scala.collection.mutable.Queue
  val q2 = new Queue[BigInt]
  val q3 = new Queue[BigInt]
  val q5 = new Queue[BigInt]
  def enqueue(n: BigInt) = {
    q2 enqueue n * 2
    q3 enqueue n * 3
    q5 enqueue n * 5
  }
  def next = {
    val n = q2.head min q3.head min q5.head
    if (q2.head == n) q2.dequeue
    if (q3.head == n) q3.dequeue
    if (q5.head == n) q5.dequeue
    enqueue(n)
    n
  }
  def hasNext = true
  q2 enqueue 1
  q3 enqueue 1
  q5 enqueue 1
}

object HammingTest {

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