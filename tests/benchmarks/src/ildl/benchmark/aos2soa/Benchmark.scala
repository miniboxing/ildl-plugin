package ildl
package benchmark
package aos2soa

import org.scalameter.CurveData
import org.scalameter.api._
import org.scalameter.Key
import org.scalameter.DSL._

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Array-of-Struct
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

  import AverageTemperature._

  var aosData: Array[(Long, Long, Double)] = null
  adrt(ArrayOfStructToStructOfArray) {
    var soaData: Array[(Long, Long, Double)] = null
  }

  val bench = Gen.enumeration("bench")("direct", "adrt__")
  val sizes = Gen.single("size")(5000000)// Gen.range("size")(1000000, 5000000, 1000000)
  val pred  = Gen.enumeration("predictable")(false, true)
  override def aggregator = Aggregator.average

  measure method "readingsAverage" in {
    using(Gen.tupled(sizes, pred, bench)) config (
        exec.independentSamples -> 1,
        exec.benchRuns -> 20,
        exec.jvmflags -> ("-Xmx2g -Xms2g " /* + "-verbose:gc " */)
    ) setUp {
        // Note: It is expected that "soaData" appears as "not found" in the IDE:
        case (size, pred, "direct") => aosData = createDataDirect(size, pred); soaData = null
        case (size, pred, "adrt__") => soaData = createDataSoA(size, pred); aosData = null
        case (_, _, _) => soaData = null; aosData = null
        System.gc()
    } tearDown {
      _ =>
        // Note: It is expected that "soaData" appears as "not found" in the IDE:
        aosData = null
        soaData = null
        System.gc()
    } in {
      case (size, pred, bench) =>
//        print("starting ")
//        print(bench)
//        print("  ")
//        println(pred)
        bench match {
          // Note: It is expected that "getAverageSoA" and "soaData" appear as "not found" in the IDE:
          case "direct" => getAverageDirect(aosData, 0)
          case "adrt__" => getAverageSoA(soaData, 0)
        }
//        print("stopping ")
//        print(bench)
//        print("  ")
//        println(pred)
    }
  }
}