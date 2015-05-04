package ildl
package benchmark
package hamming

import org.scalameter.CurveData
import org.scalameter.api._
import org.scalameter.Key
import org.scalameter.DSL._

object BenchmarkRunner extends PerformanceTest.Microbenchmark {

  import HammingNumbers._

  val bench   = Gen.enumeration("bench")("direct", "adrt__")

  var data: List[(Double, Double)] = _
  var slope: Double = _
  var offset: Double = _
  val eps = 1E-6

  println((new HammingDirect().drop(2000)).next())

  for (interp <- Seq(true, false))
    measure method "gcd" in {
      using(Gen.tupled(bench, Gen.single("jvm_interpreter")(interp))) config (
          exec.independentSamples -> 5,
          exec.benchRuns -> 5,
          exec.jvmflags -> flags(interp)
      ) setUp {
        _ =>
          val r1 = (new HammingDirect().drop(10000)).next()
          val r2 = (new HammingADRT().drop(10000)).next()
          assert(r1 == r2, r1.toString + " vs " + r2.toString)
      } in {
        case (bench, _) =>
          bench match {
            case "direct" =>
              (new HammingDirect().drop(10000)).next()
            case "adrt__" =>
              (new HammingADRT().drop(10000)).next()
          }
      }
    }

  def flags(interp: Boolean): String = interp match {
    case true =>  "-Xint"
    case false => ""
  }
}