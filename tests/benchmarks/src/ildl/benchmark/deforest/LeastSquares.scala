package ildl
package benchmark
package deforest

import scala.collection.optimizer._

object LeastSquares {

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

  adrt(ListAsLazyList){
    def leastSquaresADRT(data: List[(Double, Double)]): (Double, Double) = {
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
}