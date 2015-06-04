package ildl
package benchmark
package deforest

import org.scalameter.CurveData
import org.scalameter.api._
import org.scalameter.Key
import org.scalameter.DSL._

/** The benchmark object */
object BenchmarkRunner extends PerformanceTest.Microbenchmark {
  //
  // The benchmark object. This object is the entry point into the current
  // benchmark and customizes the ScalaMeter configuration.
  //
  // **Note:** In the ScalaIDE, some of the benchmarked methods will appear
  // as not found. This is expected, and occurs since the presentation compiler
  // (the fast one which performs syntax highlighting and quick error checking)
  // is a stripped-down version of the Scala compiler and does not allow the
  // ildl-plugin to transform the program before the typer phase (in the
  //`post-parser` phase). Nevertheless, compiling and running occurs correctly.
  //

  import LeastSquares._

  val sizes = Gen.range("size")(1000000, 5000000, 1000000)
  override def aggregator = Aggregator.average

  val bench = Gen.enumeration("bench")("direct_____", "adrt_erased", "adrt_spec'd", "blitz______", "manual_trav", "manual_fuse")

  var data: List[(Double, Double)] = _
  val slope: Double = 1.0
  val offset: Double = 0.0
  val eps = 1E-6

  val interp = false

  measure method "leastSquares" in {
    using(Gen.tupled(sizes, bench)) config (
        exec.independentSamples -> 1,
        exec.benchRuns -> 10,
        exec.jvmflags -> ("-Xmx1536m" + " " + flags(interp))
    ) setUp {
      case (size, bench) =>
        data = (1 to size).map(_.toDouble).zip((1 to size).map(_.toDouble)).toList
        System.gc()
    } tearDown {
      _ =>
        // correctness check:
        assert(math.abs(slope - 1) < eps, "slope: " + slope)
        assert(math.abs(offset) < eps, "offset: " + offset)
        data = null
        System.gc()
    } in {
      case (size, bench) =>
        val (slope0, offset0) =
          bench match {
            // benchmarks:
            case "direct_____" => leastSquaresDirect(data)
            case "adrt_erased" => leastSquaresADRTGeneric(data)
            case "adrt_spec'd" => leastSquaresADRTSpecialized(data)
            case "blitz______" => leastSquaresBlitz(data)
            case "manual_trav" => leastSquaresManual1(data)
            case "manual_fuse" => leastSquaresManual2(data)
          }
      // sanity checks:
      assert(math.abs(slope0 - slope) < eps, slope0)
      assert(math.abs(offset0 - offset) < eps, offset0)
    }
  }

  def flags(interp: Boolean): String = interp match {
    case true =>  "-Xint"
    case false => ""
  }
}