package ildl
package benchmark
package aos2soa

import scala.util.Random
import Implicit._

object StructOfArray {

   adrt(ArrayOfStructToStructOfArray) {
    def createData(size: Int): SensorReadings = {
      Random.setSeed(0)
      var timestamp = System.currentTimeMillis()
      val array = new Array[(Long, Long, Double)](size)

      for (i <- 0 until size) {
        timestamp += Random.nextInt(1000)
        val event = if (Random.nextBoolean) 0 else 1
        val reading = Random.nextDouble()

        array(i) = (timestamp, event, reading)
      }

      array
    }

    def getAverage(data: SensorReadings, event: Long): Double = {
      var i = 0
      var acc = 0.0
      var cnt = 0
      val size = data.length

      while (i < size) {
        if (data.event(i) == event) {
          acc += data.reading(i)
          cnt = 0
        }
        i += 1
      }
      if (cnt != 0)
        acc / cnt
      else
        Float.NaN
    }
  }
}