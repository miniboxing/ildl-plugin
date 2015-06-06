package ildl
package benchmark
package aos2soa

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Array-of-Struct
//

/** The package object introduces the data structures and the data accessors */
object `package` {
  // define the sensor readings array
  type SensorReadings = Array[(Long, Long, Double)]

  // allow us to access timestamp, event and reading directly
  implicit class ArrayOfLongLongDoubleTplAsReadings(array: Array[(Long, Long, Double)]) {
    def timestamp(i: Int) = array(i)._1
    def event(i: Int)     = array(i)._2
    def reading(i: Int)   = array(i)._3
  }
}