package ildl
package benchmark
package aos2soa

import scala.reflect.ClassTag

object ArrayOfStructToStructOfArray extends RigidTransformationDescription {

  case class StructOfArray(arrayOfTimestamps: Array[Long],
                           arrayOfEvents:     Array[Long],
                           arrayOfReadings:   Array[Double])

  type High = SensorReadings
  type Repr = StructOfArray

  // conversions:
  def toRepr(arrayOfStruct: SensorReadings): StructOfArray @high =
    if (arrayOfStruct == null)
      null
    else {
      val length = arrayOfStruct.length
      val arrayOfT = new Array[Long](length)
      val arrayOfU = new Array[Long](length)
      val arrayOfV = new Array[Double](length)

      var i = 0
      while (i < length) {
        val tup = arrayOfStruct(i)
        if (tup != null) {
          arrayOfT(i) = tup._1
          arrayOfU(i) = tup._2
          arrayOfV(i) = tup._3
        }
        i += 1
      }

      StructOfArray(arrayOfT, arrayOfU, arrayOfV)
    }

  def toHigh(structOfArray: StructOfArray @high): SensorReadings =
    if (structOfArray == null)
      null
    else
      sys.error("There's no going back!")

  // Operations
  def extension_length(soa: StructOfArray @high): Int = soa.arrayOfTimestamps.length

  def extension_apply(soa: StructOfArray @high, idx: Int): (Long, Long, Double) =
    (soa.arrayOfTimestamps(idx), soa.arrayOfEvents(idx), soa.arrayOfReadings(idx))

  def extension_update(soa: StructOfArray @high, idx: Int, value: (Long, Long, Double)): Unit = {
    soa.arrayOfTimestamps(idx) = value._1
    soa.arrayOfEvents(idx)     = value._2
    soa.arrayOfReadings(idx)   = value._3
  }

  def implicit_ArrayOfLongLongDoubleTplAsReadings_timestamp(soa: StructOfArray @high, idx: Int): Long =
    soa.arrayOfTimestamps(idx)

  def implicit_ArrayOfLongLongDoubleTplAsReadings_event(soa: StructOfArray @high, idx: Int): Long =
    soa.arrayOfEvents(idx)

  def implicit_ArrayOfLongLongDoubleTplAsReadings_reading(soa: StructOfArray @high, idx: Int): Double =
    soa.arrayOfReadings(idx)
}