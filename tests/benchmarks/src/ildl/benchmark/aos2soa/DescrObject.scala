package ildl
package benchmark
package aos2soa

import scala.reflect.ClassTag

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Array-of-Struct
//

/**
 *  This transformation targets data of type `SensorReadings` (an array of tuples) and transforms
 *  it into `SensorReadingsSoA` (a tuple of arrays). It's important to have this transformation
 *  across both data loading and reading, as we don't want to pay for the cost of transforming form
 *  one representation to another.
 *
 *  We restrict this by forcing the `toRepr` and `toHigh` coercions to only transform `null`, all
 *  other values triggering a run-time error.
 *
 *  @see the comment in [[ildl.benchmark.aos2soa.AverageTemperature]] for more information
 */
object ArrayOfStructToStructOfArray extends RigidTransformationDescription {

  case class SensorReadingsSoA(arrayOfTimestamps: Array[Long],
                               arrayOfEvents:     Array[Long],
                               arrayOfReadings:   Array[Double])

  type High = SensorReadings
  type Repr = SensorReadingsSoA

  // conversions:
  def toRepr(arrayOfStruct: SensorReadings): SensorReadingsSoA @high =
    if (arrayOfStruct == null)
      null
    else
      sys.error("You should instantiate the array inside the current scope!")

  def toHigh(structOfArray: SensorReadingsSoA @high): SensorReadings =
    if (structOfArray == null)
      null
    else
      // TODO: Add a safe way to extract the array, maybe hijacking clone?!?
      sys.error("The transformed array should not leak outside the transformed scope!")


  // Constructor:
  def ctor_Array(length: Int): SensorReadingsSoA @high = {
    val arrayOfT = new Array[Long](length)
    val arrayOfU = new Array[Long](length)
    val arrayOfV = new Array[Double](length)
    SensorReadingsSoA(arrayOfT, arrayOfU, arrayOfV)
  }


  // Operations
  def extension_length(soa: SensorReadingsSoA @high): Int = soa.arrayOfTimestamps.length

  def extension_apply(soa: SensorReadingsSoA @high, idx: Int): (Long, Long, Double) =
    (soa.arrayOfTimestamps(idx), soa.arrayOfEvents(idx), soa.arrayOfReadings(idx))

  def extension_update(soa: SensorReadingsSoA @high, idx: Int, value: (Long, Long, Double)): Unit = {
    soa.arrayOfTimestamps(idx) = value._1
    soa.arrayOfEvents(idx)     = value._2
    soa.arrayOfReadings(idx)   = value._3
  }

  def implicit_ArrayOfLongLongDoubleTplAsReadings_timestamp(soa: SensorReadingsSoA @high, idx: Int): Long =
    soa.arrayOfTimestamps(idx)

  def implicit_ArrayOfLongLongDoubleTplAsReadings_event(soa: SensorReadingsSoA @high, idx: Int): Long =
    soa.arrayOfEvents(idx)

  def implicit_ArrayOfLongLongDoubleTplAsReadings_reading(soa: SensorReadingsSoA @high, idx: Int): Double =
    soa.arrayOfReadings(idx)
}