package ildl
package benchmark
package hamming

import scala.collection.mutable.Queue

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-%7E-Efficient-Collections
//

/**
 * The actual benchmark. The current benchmark is finding the 10001-th Hamming number,
 * and the implementation is lifted directly from the Rosetta code website:
 *   [[http://rosettacode.org/wiki/Hamming_numbers#Scala]]
 * and adapted to improve performance.
 *
 * Several factors influence the overall speedup. In the following diagram, we added
 * intermediate steps to the transformation, in order to isolate the individual factors
 * influencing the overall speedup.
 *
 * The diagram follows the transformation of the main two data types in the program:
 * BigInt and Queue[BigInt]. The top part explains the transformation, the middle shows
 * the updated types and the bottom part shows the exact transformation description
 * objects used for the `adrt` scopes:
 *
 *  {{{
 *                +--> this transformation is the one that brought the most benefit, since it achieved
 *                |    two results at once: being implemented as a fixed-size circular buffer queue
 *                |    and backed by a simple array, it improved locality and reduced access time.
 *                |    Furthermore, the Queue.enque method takes a variable number of arguments, which
 *                |    adds further overhead for packing and unpacking them. Using enqueue1, we used
 *                |    the fact that we only introduce one element at a time to our advantage: this
 *                |    way, we avoid the overhead associated to the variable number arguments.
 *                |
 *                |                +--> knowing the range is limited, we can reduce the size of the
 *                |                |    data by switching from the scala.BigInt object (backed by
 *                |                |    the java.math.BigInteger) to a smaller java.lang.Long. This
 *                |                |    saves some memory and some cycles when operating on the data,
 *                |                |    although there's not a lot of saving since both BigInt and
 *                |                |    java.lang.Long are heap-allocated objects.
 *                |                |
 *                |                |                   +--> unboxing: we switch from j.l.Long to
 *                |                |                   |              scala.Long and the backend
 *                |                |                   |              automatically unboxes it in
 *                |                |                   |              method signatures and the
 *                |                |                   |              underlying array used by the
 *                |                |                   |              FunnyQueue data structure.
 *                |                |                   |
 * BigInt        ===> BigInt      ===> java.lang.Long ===> scala.Long  (compiled to Java's unboxed long)
 * Queue[BigInt] ===> FunnyQueue* ===> FunnyQueue*    ===> FunnyQueue* (* FunnyQueue-s are specialized
 *    \                  ^                  ^                  ^          by hand for the element type)
 *     \________________/                  /                  /
 *      \ step1.QueueOfLongAsFunnyQueue   /                  /
 *       \                               /                  /
 *        \                             /                  /
 *         \___________________________/                  /
 *          \ step2.BigIntAsLong +                       /
 *           \ step2.QueueOfLongAsFunnyQueue            /
 *            \                                        /
 *             \______________________________________/
 *               step3.BigIntAsLong +
 *                step3.QueueOfLongAsFunnyQueue
 * }}}
 *
 * These are the numbers we obtained:
 *
 *    +------------------------------------+--------------+---------+
 *    | Benchmark                          | Running Time | Speedup |
 *    +------------------------------------+--------------+---------|
 *    | 10001-th Hamming number, original  |      6.56 ms |    none |
 *    | 10001-th Hamming number, step1     |      2.70 ms |    2.4x |
 *    | 10001-th Hamming number, step2     |      2.16 ms |    3.0x |
 *    | 10001-th Hamming number, step3     |      1.64 ms |    4.0x |
 *    +------------------------------------+--------------+---------+
 *
 * We apologize for the fact that the numbers in the paper were more optimistic, indicating a speedup
 * of 8x -- this was a flaw in the benchmark that misled us. However, we have double-checked the
 * benchmark now and do not expect any further discrepancies.
 */
object HammingNumbers {

  // we want to be able to enqueue a single element at once -- please see the
  // comment above for the explanation of enqueue1 vs enqueue. Note that we
  // have made QueueWithEnqueue1 a value class, thus preventing the creation
  // of an object in order to perform the enqueue1 operation:
  implicit class QueueWithEnqueue1[T](val q: Queue[T]) extends AnyVal {
    def enqueue1(t: T) = q.enqueue(t)
  }

  class HammingDirect extends Iterator[BigInt] {
    val q2 = new Queue[BigInt]
    val q3 = new Queue[BigInt]
    val q5 = new Queue[BigInt]
    def enqueue(n: BigInt) = {
      q2 enqueue1 n * 2
      q3 enqueue1 n * 3
      q5 enqueue1 n * 5
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

  adrt(step1.QueueOfLongAsFunnyQueue) {
    class HammingADRT_1 extends Iterator[BigInt] {
      val q2 = new Queue[BigInt]
      val q3 = new Queue[BigInt]
      val q5 = new Queue[BigInt]
      def enqueue(n: BigInt) = {
        q2 enqueue1 n * 2
        q3 enqueue1 n * 3
        q5 enqueue1 n * 5
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

  adrt(step2.BigIntAsLong) {
    adrt(step2.QueueOfLongAsFunnyQueue) {
      class HammingADRT_2 extends Iterator[BigInt] {
        val q2 = new Queue[BigInt]
        val q3 = new Queue[BigInt]
        val q5 = new Queue[BigInt]
        def enqueue(n: BigInt) = {
          q2 enqueue1 n * 2
          q3 enqueue1 n * 3
          q5 enqueue1 n * 5
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

  adrt(step3.BigIntAsLong) {
    adrt(step3.QueueOfLongAsFunnyQueue) {
      class HammingADRT_3 extends Iterator[BigInt] {
        val q2 = new Queue[BigInt]
        val q3 = new Queue[BigInt]
        val q5 = new Queue[BigInt]
        def enqueue(n: BigInt) = {
          q2 enqueue1 n * 2
          q3 enqueue1 n * 3
          q5 enqueue1 n * 5
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
}