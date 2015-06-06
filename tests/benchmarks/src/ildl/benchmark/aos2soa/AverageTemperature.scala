package ildl
package benchmark
package aos2soa

import scala.util.Random

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Array-of-Struct
//

/**
 * The actual benchmark. The current benchmark is inspired by a presentation given by Lee Mighdoll during
 * ScalaDays 2015 in San Francisco on fast visualization tools for sensor data. Here's the presentation
 * he gave: [[https://www.parleys.com/tutorial/visualize-things]]
 *
 * Let's imagine you have a sensor and it records different parameters over the day. It could measure
 * temperature, humidity, light, sound, vibration or other parameters. Each recording is stored on a
 * local server in the form of entries:
 *
 * {{{
 *   +-----------+------------------------+----------------+
 *   | timestamp | event/measurement type | sensor reading |
 *   +-----------+------------------------+----------------+
 * }}}
 *
 * While some readings occur regularly, like temperature and humidity, others only occur when some
 * external trigger event occurs, such as, for example, a strong noise or vibration. In this case, the
 * readings are not necessarily uniform (we can't say that every 5th reading is a temperature reading).
 * Instead, we have a log of measurements, stored as an array:
 *
 * {{{
 *   +-----+-----+-----+-----
 *   |  o  |  o  |  o  | ...
 *   +- | -+- | -+- | -+-----
 *      |     |     |
 *      |     |     v    ...
 *      |     |   entry3
 *      |     |
 *      |     v
 *      |   entry2
 *      |
 *      v
 *    entry1
 * }}}
 *
 * Now let's imagine we ask the question: "What was the average temperature last night?"
 * To answer this question, we have to traverse the array of entries and, for each one, check
 * if the measurement is a temperature reading (and not humidity, ...) and, if it is, add the
 * measurement to our computed sum and count.
 *
 * Now, a much better way to do this is using the struct of array format:
 *
 * {{{
 *   +--------+--------+--------+-----
 *   | time1  | time2  | time3  | ...
 *   +--------+--------+--------+-----
 *
 *   +--------+--------+--------+-----
 *   | event1 | event2 | event3 | ...
 *   +--------+--------+--------+-----
 *
 *   +----------+----------+----------+-----
 *   | reading1 | reading2 | reading3 | ...
 *   +----------+----------+----------+-----
 * }}}
 *
 * Now, we only need to traverse the types array, one element after another. When we encounter a
 * temperature event, only then we access the reading array and add the sensor value to our sum
 * and count one more entry (so we can output the average temperature, not the sum of temperatures).
 *
 * Of course, we can change the existing code to transform the arrays and the processing code to
 * improve the performance by hand. But can we do it using the `ildl-plugin`? Sure we can.
 *
 * We automated the translation:
 *
 * {{{
 *
 *   SensorReadings ===> SensorReadingsSoA
 *    \                        ^
 *     \______________________/
 *       ArrayOfStructToStructOfArray
 * }}}
 *
 * We use the `ArrayOfStructToStructOfArray` transformation both when loading the data (well, in
 * our case randomly producing it) and when traversing the data structure. With this, we can obtain
 * the speed benefits without rewriting any of our code. You will notice that aside from the `adrt`
 * annotation, there are no differences between the `createDataDirect` and `createDataSoA`, and,
 * respectively, the `getAverageDirect` and `getAverageSoA` methods.
 *
 * We analyzed this transformation in two scenarios:
 * 1) If the temperature readings are randomly spaced, assuming there are many triggered readings
 * 2) If the temperature readings are evenly spaced, assuming a calm residential setting
 *
 * These are the results we obtained:
 *
 *    +--------------------------------------+----------+--------------+---------|
 *    | Benchmark                            | Spacing  | Running Time | Speedup |
 *    +--------------------------------------+----------+--------------+---------|
 *    | Average temperature, direct          | random   |     55.51 ms |    none |
 *    | Average temperature, struct of array | random   |     30.41 ms |    1.8x |
 *    | Average temperature, direct          | even     |     32.50 ms |    none |
 *    | Average temperature, struct of array | even     |      5.67 ms |    5.7x |
 *    +--------------------------------------+----------+--------------+---------|
 *
 * As expected, the transformation paid off better if the temperature readings are regular, but
 * even if they are not, we can still obtain a good speedup of 1.8x.
 *
 * Note: In the paper we only included the random distribution, but now we plan to include both
 * the random and even readings.
 */
object AverageTemperature {

  def createDataDirect(size: Int, predictable: Boolean): SensorReadings = {
    Random.setSeed(0)
    var timestamp = System.currentTimeMillis()
    val array = new Array[(Long, Long, Double)](size)
    val rand = new Random(0)

    for (i <- 0 until size) {
      timestamp += Random.nextInt(1000)
      val event = if (predictable) i % 3 else rand.nextInt(2)
      val reading = Random.nextDouble()

      array(i) = (timestamp, event, reading)
    }

    array
  }

  adrt(ArrayOfStructToStructOfArray) {
    // same code as createDataDirect:
    def createDataSoA(size: Int, predictable: Boolean): SensorReadings = {
      Random.setSeed(0)
      var timestamp = System.currentTimeMillis()
      val array = new Array[(Long, Long, Double)](size)
      val rand = new Random(0)

      for (i <- 0 until size) {
        timestamp += Random.nextInt(1000)
      val event = if (predictable) i % 3 else rand.nextInt(2)
        val reading = Random.nextDouble()

        array(i) = (timestamp, event, reading)
      }

      array
    }
  }

  def getAverageDirect(data: SensorReadings, event: Long): Double = {
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

  adrt(ArrayOfStructToStructOfArray) {
    // same code as getAverageDirect:
    def getAverageSoA(data: SensorReadings, event: Long): Double = {
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