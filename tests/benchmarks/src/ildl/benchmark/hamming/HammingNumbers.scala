package ildl
package benchmark
package hamming

object HammingNumbers {

  // taken from http://rosettacode.org/wiki/Hamming_numbers#Scala
  class HammingDirect extends Iterator[BigInt] {
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

  adrt(QueueOfBigIngAsFunnyQueue) {
    class HammingADRT extends Iterator[BigInt] {
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
  }
}