package ildl
package benchmark
package deforest

import scala.collection.optimizer._

/**
 * The benchmark object. The current benchmark is the least squares method,
 * explained on [[http://en.wikipedia.org/wiki/Linear_regression]].
 *
 * The transformation performs several optimizations at once:
 *  - introducing a specialized tuple
 *  - encoding the tuple in a long integer
 *  - stack-allocating the long integer
 *
 *  {{{
 *
 *         +--- class specialization: the tuple class is already specialized in the Scala backend,
 *         |                          so the two integers are stored in the unboxed format. However,
 *         |                          a limitation in the Scala specialization (fixed by the
 *         |                          miniboxing transformation) dictates that even the specialized
 *         |                          class has a pair of generic fields. By adding our
 *         |                          hand-specialized alternative to tuples, we eliminate those
 *         |                          fields, saving on the heap memory footprint and thus on the
 *         |                          GC cycles.
 *         |
 *         |                  +--> encoding: instead of having a class with two fields we have the
 *         |                  |              java.lang.Long which encodes the two fields in one. We
 *         |                  |              expect to get a small performance hit, as the real and
 *         |                  |              imaginary components have to be encoded.
 *         |                  |
 *         |                  |                   +--> unboxing: we need to convert j.l.Long to
 *         |                  |                   |              scala.Long and the backend unboxes it
 *         |                  |                   |              without our intervention.
 *         |                  |                   |
 * (3, 4) ===> Complex(3, 4) ===> java.lang.Long ===> scala.Long (compiled to Java's unboxed long)
 *    \             ^                    ^                ^
 *     \___step1___/                    /                /
 *      \ adrt(IntPairTupleSpec)       /                /
 *       \                            /                /
 *        \                          /                /
 *         \___step2________________/                /
 *          \ adrt(IntPairAsBoxedLong)              /
 *           \                                     /
 *            \                                   /
 *             \___step3_________________________/
 *               adrt(IntPairAsGaussianInteger)
 * }}}
 *
 */
object LeastSquares {

  /** Least squares with no optimization */
  def leastSquaresDirect(data: List[(Double, Double)]): (Double, Double) = {
    val size = data.length
    val sumx = data.map(_._1).sum
    val sumy = data.map(_._2).sum
    val sumxy = data.map(p => p._1 * p._2).sum
    val sumxx = data.map(p => p._1 * p._1).sum

    val slope  = (size * sumxy - sumx * sumy) / (size * sumxx - sumx * sumx)
    val offset = (sumy * sumxx - sumx * sumxy) / (size * sumxx - sumx * sumx)

    (slope, offset)
  }

  /** Least squares with no optimization */
  adrt(erased.ListAsLazyList){
    def leastSquaresADRTGeneric(data: List[(Double, Double)]): (Double, Double) = {
      val size = data.length
      val sumx = data.map(_._1).sum
      val sumy = data.map(_._2).sum
      val sumxy = data.map(p => p._1 * p._2).sum
      val sumxx = data.map(p => p._1 * p._1).sum

      val slope  = (size * sumxy - sumx * sumy) / (size * sumxx - sumx * sumx)
      val offset = (sumy * sumxx - sumx * sumxy) / (size * sumxx - sumx * sumx)

      (slope, offset)
    }
  }

  adrt(specialized.ListAsLazyList){
    def leastSquaresADRTSpecialized(data: List[(Double, Double)]): (Double, Double) = {
      println("started")
      val size = data.length
      val sumx = data.map(_._1).sum
      val sumy = data.map(_._2).sum
      val sumxy = data.map(p => p._1 * p._2).sum
      val sumxx = data.map(p => p._1 * p._1).sum

      val slope  = (size * sumxy - sumx * sumy) / (size * sumxx - sumx * sumx)
      val offset = (sumy * sumxx - sumx * sumxy) / (size * sumxx - sumx * sumx)
      println("done")

      (slope, offset)
    }
  }

  def leastSquaresBlitz(data: List[(Double, Double)]): (Double, Double) = optimize {
    val size = data.length
    val sumx = data.map(_._1).sum
    val sumy = data.map(_._2).sum
    val sumxy = data.map(p => p._1 * p._2).sum
    val sumxx = data.map(p => p._1 * p._1).sum

    val slope  = (size * sumxy - sumx * sumy) / (size * sumxx - sumx * sumx)
    val offset = (sumy * sumxx - sumx * sumxy) / (size * sumxx - sumx * sumx)

    (slope, offset)
  }

  /** Least squares with manual traversal */
  def leastSquaresManual1(data: List[(Double, Double)]): (Double, Double) = {
    val size = data.length
    var list = data
    var sumx = 0.0
    var sumy = 0.0
    var sumxy = 0.0
    var sumxx = 0.0
    // val sumx = data.map(_._1).sum
    list = data
    while (!list.isEmpty) {
      sumx = sumx + list.head._1
      list = list.tail
    }
    // val sumy = data.map(_._2).sum
    list = data
    while (!list.isEmpty) {
      sumy = sumy + list.head._2
      list = list.tail
    }
    // val sumxy = data.map(p => p._1 * p._2).sum
    list = data
    while (!list.isEmpty) {
      sumxy = sumxy + list.head._1 * list.head._2
      list = list.tail
    }
    // val sumxx = data.map(p => p._1 * p._1).sum
    list = data
    while (!list.isEmpty) {
      sumxx = sumxx + list.head._1 * list.head._1
      list = list.tail
    }

    val slope  = (size * sumxy - sumx * sumy) / (size * sumxx - sumx * sumx)
    val offset = (sumy * sumxx - sumx * sumxy) / (size * sumxx - sumx * sumx)

    (slope, offset)
  }

  /** Least squares with manual traversal + fusion */
  def leastSquaresManual2(data: List[(Double, Double)]): (Double, Double) = {
    val size = data.length
    var list = data
    var sumx = 0.0
    var sumy = 0.0
    var sumxy = 0.0
    var sumxx = 0.0
    // val sumx = data.map(_._1).sum
    // val sumy = data.map(_._2).sum
    // val sumxy = data.map(p => p._1 * p._2).sum
    // val sumxx = data.map(p => p._1 * p._1).sum
    list = data
    while (!list.isEmpty) {
      sumx = sumx + list.head._1
      sumy = sumy + list.head._2
      sumxy = sumxy + list.head._1 * list.head._2
      sumxx = sumxx + list.head._1 * list.head._1
      list = list.tail
    }

    val slope  = (size * sumxy - sumx * sumy) / (size * sumxx - sumx * sumx)
    val offset = (sumy * sumxx - sumx * sumxy) / (size * sumxx - sumx * sumx)

    (slope, offset)
  }
}