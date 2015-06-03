package ildl
package benchmark
package gcd

import org.scalameter.CurveData
import org.scalameter.api._
import org.scalameter.Key
import org.scalameter.DSL._

/**
 * The benchmark object. This object is the entry point into the current
 * benchmark and customizes the ScalaMeter configuration.
 *
 * **Note:** In the ScalaIDE, some of the benchmarked methods will appear
 * as not found. This is expected, and occurs since the presentation compiler
 *  (the fast one which performs syntax highlighting and quick error checking)
 *  is a stripped-down version of the Scala compiler and does not allow the
 * ildl-plugin to transform the program before the typer phase (in the
 * `post-parser` phase)
 */
object BenchmarkRunner extends PerformanceTest.Microbenchmark {

  import GreatestCommonDivisor._

  // for the meanings of the 4 labels please see [[ildl.benchmark.gcd.GreatestCommonDividsor]]
  val bench   = Gen.enumeration("bench")("direct", "adrt_1", "adrt_2", "adrt_3")

  var data: List[(Double, Double)] = _
  var slope: Double = _
  var offset: Double = _
  val eps = 1E-6

  for (interp <- Seq(true))
    measure method "gcd" in {
      using(Gen.tupled(bench, Gen.single("jvm_interpreter")(interp))) config (
          exec.independentSamples -> 5,
          // you will notice that increasing the number of benchmark runs tends to
          // make the numbers very uniform:
          // ```
          //  Parameters(bench -> direct, jvm_interpreter -> false): 2.352974
          //  Parameters(bench -> adrt_1, jvm_interpreter -> false): 1.299271
          //  Parameters(bench -> adrt_2, jvm_interpreter -> false): 2.214313
          //  Parameters(bench -> adrt_3, jvm_interpreter -> false): 1.259047
          // ```
          // We traced this to the fact that, after many runs, the HotSpot
          // virtual machine is able to inline all the code away and perform
          // escape analysis in order to remove all allocations, effectively
          // flattening the code completely. On the other hand, you can see
          // both in the interpreter benchmarks (and on larger benchmarks)
          // that several code patterns prevent aggresive inlining, thus not
          // allowing this flattening. This is where the adrt transformation
          // can make an important difference. These are the numbers for the
          // interpreter run:
          // ```
          //  Parameters(bench -> direct, jvm_interpreter -> true): 1462.901959
          //  Parameters(bench -> adrt_1, jvm_interpreter -> true): 541.569494
          //  Parameters(bench -> adrt_2, jvm_interpreter -> true): 811.463098
          //  Parameters(bench -> adrt_3, jvm_interpreter -> true): 539.818765
          // ```
          exec.benchRuns -> 5,
          exec.jvmflags -> flags(interp)
      ) setUp {
        _ =>
          // Result correctness checks:
          val r1 = (10, 3)
          val r2 = gcd_direct((544,185), (131,181))
          val r3 = gcd_adrt_1((544,185), (131,181))
          val r4 = gcd_adrt_2((544,185), (131,181))
          val r5 = gcd_adrt_3((544,185), (131,181))
          assert(r2 == r1, r2.toString)
          assert(r3 == r1, r3.toString)
          assert(r4 == r1, r4.toString)
          assert(r5 == r1, r5.toString)
      } in {
        case (bench, _) =>
          bench match {
            case "direct" =>
              var i = 10000
              while (i > 0) {
                gcd_direct((544,185), (131,181))
                i -= 1
              }
            case "adrt_1" =>
              var i = 10000
              while (i > 0) {
                gcd_adrt_1((544,185), (131,181))
                i -= 1
              }
            case "adrt_2" =>
              var i = 10000
              while (i > 0) {
                gcd_adrt_2((544,185), (131,181))
                i -= 1
              }
            case "adrt_3" =>
              var i = 10000
              while (i > 0) {
                gcd_adrt_3((544,185), (131,181))
                i -= 1
              }
          }
      }
    }

  def flags(interp: Boolean): String = interp match {
    case true =>  "-Xint"
    case false => ""
  }
}