package ildl
package benchmark
package aos2soa

import org.scalameter.CurveData
import org.scalameter.api._
import org.scalameter.Key
import org.scalameter.DSL._

object BenchmarkRunner extends PerformanceTest.Microbenchmark {

  var aosData: Array[(Long, Long, Double)] = null
  adrt(ArrayOfStructToStructOfArray) {
    var soaData: Array[(Long, Long, Double)] = null
  }

  val bench = Gen.enumeration("bench")("direct", "adrt__")
  val sizes = Gen.range("size")(1000000, 5000000, 1000000)

  measure method "leastSquares" in {
    using(Gen.tupled(sizes, bench)) config (
        exec.independentSamples -> 2,
        exec.benchRuns -> 2
    ) setUp {
       case (size, "direct") => aosData = ArrayOfStruct.createData(size)
      case (size, "adrt__") => soaData = StructOfArray.createData(size)
    } tearDown {
      _ =>
        aosData = null
        soaData = null
        System.gc()
    } in {
      case (size, bench) =>
        bench match {
          case "direct" => ArrayOfStruct.getAverage(aosData, 0)
          case "adrt__" => StructOfArray.getAverage(soaData, 0)
        }
    }
  }
}