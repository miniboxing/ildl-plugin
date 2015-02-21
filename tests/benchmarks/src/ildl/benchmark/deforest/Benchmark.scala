package ildl
package benchmark
package deforest

import org.scalameter.CurveData
import org.scalameter.api._
import org.scalameter.Key
import org.scalameter.DSL._

object BenchmarkRunner extends PerformanceTest.Microbenchmark {

  import LeastSquares._

  val sizes = Gen.range("size")(1000000, 5000000, 1000000)
  val bench = Gen.enumeration("bench")("direct", "adrt__", "fused_")

  var data: List[(Double, Double)] = _
  var slope: Double = _
  var offset: Double = _
  val eps = 1E-6

  val interp = false

  measure method "leastSquares" in {
    using(Gen.tupled(sizes, bench)) config (
        exec.independentSamples -> 1,
        exec.benchRuns -> 1,
        exec.jvmflags -> flags(interp)
    ) setUp {
      case (size, bench) =>
        val data0 = (1 to size).map(_.toDouble).toList
        data = data0 zip data0
    } tearDown {
      _ =>
        assert(math.abs(slope - 1) < eps, "slope: " + slope)
        assert(math.abs(offset) < eps, "offset: " + offset)
        data = null
        System.gc()
    } in {
      case (size, bench) =>
        val (slope0, offset0) =
          bench match {
            case "direct" => leastSquaresDirect(data)
            case "adrt__" => leastSquaresADRT(data)
            case "fused_" => leastSquaresFused(data)
          }
      slope  = slope0
      offset = offset0
    }
  }

  def flags(interp: Boolean): String = interp match {
    case true =>  "-Xint"
    case false => ""
  }
}