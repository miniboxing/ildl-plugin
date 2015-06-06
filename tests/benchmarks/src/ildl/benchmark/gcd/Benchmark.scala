package ildl
package benchmark
package gcd

import org.scalameter.api._
import org.scalameter.DSL._

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-%7E-Data-Encoding
//

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

  // make sure we're running on the correct setup:
  Platform.checkCompatibility()

  import GreatestCommonDivisor._

  // for the meanings of the 4 labels please see [[ildl.benchmark.gcd.GreatestCommonDividsor]]
  val bench   = Gen.enumeration("bench")("direct", "adrt_1", "adrt_2", "adrt_3")
  override def aggregator = Aggregator.average

  var data: List[(Double, Double)] = _
  var slope: Double = _
  var offset: Double = _
  val eps = 1E-6

  for (interp <- Seq(false))
    measure method "gcd" in {
      using(Gen.tupled(bench, Gen.single("jvm_interpreter")(interp))) config (
          exec.independentSamples -> 5,
          // you may notice that increasing the number of benchmark runs tends
          // to even out the numbers (for 100 runs):
          // ```
          //  Parameters(bench -> direct, jvm_interpreter -> false): 8.921889749999998
          //  Parameters(bench -> adrt_1, jvm_interpreter -> false): 2.9439812500000007
          //  Parameters(bench -> adrt_2, jvm_interpreter -> false): 5.823887280000001
          //  Parameters(bench -> adrt_3, jvm_interpreter -> false): 2.01347449
          // ```
          // We traced this to the fact that scalameter drops the runs where garbage
          // collection occurs:
          // ```
          //  Some GC time recorded, accepted: 20, ignored: 33
          // ```
          // This gives the non-transformed code an unfair advantage over the transformed
          // code and thus skews the benchmark.
          exec.benchRuns -> 20,
          exec.jvmflags -> ("-Xmx100m -Xms100m " + flags(interp))
      ) setUp {
        _ =>
          // Result correctness checks:
          val r1 = (10, 3)
          val r2 = gcd_direct((544,185), (131,181))
          // Note: It is expected that the method appears as "not found" in the IDE:
          val r3 = gcd_adrt_1((544,185), (131,181))
          val r4 = gcd_adrt_2((544,185), (131,181))
          val r5 = gcd_adrt_3((544,185), (131,181))
          assert(r2 == r1, r2.toString)
          assert(r3 == r1, r3.toString)
          assert(r4 == r1, r4.toString)
          assert(r5 == r1, r5.toString)
          System.gc()
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
                // Note: It is expected that the method appears as "not found" in the IDE:
                gcd_adrt_1((544,185), (131,181))
                i -= 1
              }
            case "adrt_2" =>
              var i = 10000
              while (i > 0) {
                // Note: It is expected that the method appears as "not found" in the IDE:
                gcd_adrt_2((544,185), (131,181))
                i -= 1
              }
            case "adrt_3" =>
              var i = 10000
              while (i > 0) {
                // Note: It is expected that the method appears as "not found" in the IDE:
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