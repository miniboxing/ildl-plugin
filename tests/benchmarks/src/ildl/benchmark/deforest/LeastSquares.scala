package ildl
package benchmark
package deforest

import scala.collection.optimizer._

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-%7E-Deforestation
//

/**
 * The actual benchmark. The current benchmark is the linear regression,  a method for determining
 * the slope and offset of a straight line that best described a given set of points. This technique
 * is further explained at [[http://en.wikipedia.org/wiki/Linear_regression]].
 *
 * Several factors influence the overall speedup. In the following diagram, we added
 * intermediate steps to the transformation, in order to isolate the individual factors
 * influencing the overall speedup.
 *
 * The diagram follows the transformation of the main data type in the program: the generic
 * List[T] container. The top part explains the transformation, the middle shows the updated
 * types and the bottom part shows the exact transformation description objects used for
 * the `adrt` scopes.
 *
 * We also added two manual transformations, which correspond to the programmer manually
 * rewriting the code in order to improve performance. While these can't be automated, they
 * provide important feedback for the other transformations and their expected performance:
 *
 *  {{{
 *          +--> this is the deforestation optimization: it takes the basic code that uses lists and
 *          |    optimizes it to use LazyList instead of List, thus delaying the execution of the map
 *          |    functions. Furthermore, since all results are delivered via sum, there is no need to
 *          |    create an actual list -- the results produced are just individual values. Avoiding the
 *          |    intermediate list creation produces significant speedups, mostly in terms of saving
 *          |    garbage collection cycles.
 *          |
 *          |                +--> this is the most we can optimize in this example in an automated fashion.
 *          |                |    Not only that we perform fusion, but we also specialize the generic
 *          |                |    computation using the miniboxing plugin. This shows three LDL-based passes
 *          |                |    over the code collaborating:
 *          |                |     - the ildl-plugin (with one LDL phase)
 *          |                |     - the miniboxing plugin (with one LDL phase for specialization and one
 *          |                |                              one for transforming FunctionX to MbFunctionX)
 *          |                |    As a curiosity, the Scala compiler has 25 phases, but when both the
 *          |                |    plugins are active, they add 21 extra phases, 15 for the miniboxing
 *          |                |    transformation and 6 for the ildl-plugin. Of course, including these
 *          |                |    plugins into the Scala compiler would reduce the phases a lot. :)
 *          |                |
 *          |                |                           +--> this is the manual counterpart of the
 *          |                |                           |    automated deforestation that we have seen
 *          |                |                           |    in the previous example. It reduces the
 *          |                |                           |    running time to some extent, as there is
 *          |                |                           |    still some abstraction in applying the
 *          |                |                           |    functions instead of doing the element
 *          |                |                           |    manipulation by hand. This example serves
 *          |                |                           |    to show the additional benefit coming from
 *          |                |                           |    inlining the functions
 *          |                |                           |
 *          |                |                           |                     +--> the most effective
 *          |                |                           |                     |    transformation is
 *          |                |                           |                     |    performing both
 *          |                |                           |                     |    horizontal and
 *          |                |                           |                     |    vertical fusion,
 *          |                |                           |                     |    which is what this
 *          |                |                           |                     |    example does. In this
 *          |                |                           |                     |    example the list is
 *          |                |                           |                     |    only traversed once
 *          |                |                           |                     |    instead of four times,
 *          |                |                           |                     |    as a result of
 *          |                |                           |                     |    horizontal fusion.
 *          |                |                           |                     |    However,
 *          |                |                           |                     |    the conditions for
 *          |                |                           |                     |    which this techinque
 *          |                |                           |                     |    is applicable are
 *          |                |                           |                     |    very restrictive
 *          |                |                           |                     |    (such as the closed
 *          |                |                           |                     |    world restriction)
 *          |                |                           |                     |    This example is meant
 *          |                |                           |                     |    to give a lower bound
 *          |                |                           |                     |    on the running time,
 *          |                |                           |                     |    assuming unlimited
 *          |                |                           |                     |    power to transform
 *          |                |                           |                     |    the program
 *          |                |                           |                     |
 * List[T] ===> LazyList[T] ===> LazyList[@miniboxed T] -+-> manual traversal -+-> manual fusion
 *    \             ^                    ^              /                     /
 *     \___step1___/                    /              /                     /
 *      \ erased.ListAsLazyList        /              /                     /
 *       \                            /              /                     /
 *        \                          /              /                     /
 *         \___step2________________/              /                     /
 *           miniboxed.ListAsLazyList             /                     /
 *                                             manual                manual
 *                                        transformation         transformation
 * }}}
 *
 * These are the numbers we obtain for 5M elements:
 *
 *    +--------------------------------------------+--------------+---------+
 *    | Benchmark                                  | Running Time | Speedup |
 *    +--------------------------------------------+--------------+---------|
 *    | Least Squares Regression, original         |      8264 ms |    none |
 *    | Least Squares Regression, adrt generic     |       429 ms |   19.3x |
 *    | Least Squares Regression, adrt miniboxed   |       280 ms |   29.5x |
 *    | Least Squares Regression, scala-blitz      |      3464 ms |    2.4x |
 *    | Least Squares Regression, manual traversal |       195 ms |   42.4x |
 *    | Least Squares Regression, manual fusion    |        79 ms |  105.0x |
 *    +--------------------------------------------+--------------+---------+
 *
 * The numbers we obtained this time are significantly better than the ones reported in the paper.
 * We realized the difference when we implemented the manual traversal, which took only 195 ms. By
 * tracing back the difference, we realized we were triggering an unwarranted garbage collection run
 * exactly before executing the adrt scope, also counting that towards the execution time. Now, running
 * a garbage collection cycle in the setup phase, we do not incur any GC cycle for the miniboxed adrt
 * version (and the manual versions) and we only incur one GC cycle in the generic case.
 *
 * Note that, given an large enough heap (in this case, 8GB), the original benchmark runs in 700 ms,
 * instead of 8000ms. This corresponds only to the cost of traversing the list and allocating the
 * intermediate heap objects involved in the processing. However, on the HotSpot virtual machine,
 * allocation is highly optimized, but if we give it 8GB of heap we don't consider the garbage cost
 * of creating an intermediate list after the map.
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

  adrt(miniboxed.ListAsLazyList){
    def leastSquaresADRTMiniboxed(data: List[(Double, Double)]): (Double, Double) = {
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