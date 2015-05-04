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
    else
      sys.error("You should instantiate the array inside the current scope!")

  def toHigh(structOfArray: StructOfArray @high): SensorReadings =
    if (structOfArray == null)
      null
    else
      // TODO: Add a safe way to extract the array, maybe hijacking clone?!?
      sys.error("The transformed array should not leak outside the transformed scope!")


  // Constructor:
  def ctor_Array(length: Int): StructOfArray @high = {
    val arrayOfT = new Array[Long](length)
    val arrayOfU = new Array[Long](length)
    val arrayOfV = new Array[Double](length)
    StructOfArray(arrayOfT, arrayOfU, arrayOfV)
  }


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