package ildl
package benchmark
package aos2soa

object Implicit {
  implicit class ArrayOfLongLongDoubleTplAsReadings(array: Array[(Long, Long, Double)]) {
    def timestamp(i: Int) = array(i)._1
    def event(i: Int)     = array(i)._2
    def reading(i: Int)   = array(i)._3
  }
}