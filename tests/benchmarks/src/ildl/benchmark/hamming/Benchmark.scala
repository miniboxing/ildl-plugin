package ildl
package benchmark
package hamming

import org.scalameter.CurveData
import org.scalameter.api._
import org.scalameter.Key
import org.scalameter.DSL._

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

  // make sure we're running on the correct setup:
  Platform.checkCompatibility()

  import HammingNumbers._

  val bench   = Gen.enumeration("bench")("direct", "adrt_1", "adrt_2", "adrt_3")
  override def aggregator = Aggregator.average

  var data: List[(Double, Double)] = _
  var slope: Double = _
  var offset: Double = _
  val eps = 1E-6

  for (interp <- Seq(false))
    measure method "gcd" in {
      using(Gen.tupled(bench, Gen.single("jvm_interpreter")(interp))) config (
          exec.independentSamples -> 1,
          exec.benchRuns -> 20,
          exec.jvmflags -> ("-Xmx5m -Xms5m " + flags(interp))
      ) setUp {
        _ =>
          val r1 = (new HammingDirect().drop(10000)).next()
          // Note: It is expected that the following types appear as "not found" in the IDE:
          val r2 = (new HammingADRT_1().drop(10000)).next()
          val r3 = (new HammingADRT_2().drop(10000)).next()
          val r4 = (new HammingADRT_3().drop(10000)).next()
          assert(r1 == r2, r1.toString + " vs " + r2.toString)
          assert(r1 == r3, r1.toString + " vs " + r3.toString)
          assert(r1 == r4, r1.toString + " vs " + r4.toString)
          System.gc()
      } in {
        case (bench, _) =>
          bench match {
            case "direct" => (new HammingDirect().drop(10000)).next()
            // Note: It is expected that the following types appear as "not found" in the IDE:
            case "adrt_1" => (new HammingADRT_1().drop(10000)).next()
            case "adrt_2" => (new HammingADRT_2().drop(10000)).next()
            case "adrt_3" => (new HammingADRT_3().drop(10000)).next()
          }
      }
    }

  def flags(interp: Boolean): String = (interp match {
    case true =>  "-Xint"
    case false => ""
  }) + "-Xmx2g -Xms2g"
}