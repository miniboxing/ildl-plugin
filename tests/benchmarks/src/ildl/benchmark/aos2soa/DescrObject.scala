package ildl
package benchmark
package aos2soa

import scala.reflect.ClassTag

object ArrayOfStructToStructOfArray extends FreestyleTransformationDescription {

  case class StructOfArray[T, U, V](arrayOfT: Array[AnyRef],
                                    arrayOfU: Array[AnyRef],
                                    arrayOfV: Array[AnyRef])

  // conversions:
  def toRepr[T, U, V](arrayOfStruct: Array[(T, U, V)]): StructOfArray[T, U, V] @high =
    if (arrayOfStruct == null)
      null
    else {
      val length = arrayOfStruct.length
      val arrayOfT = new Array[AnyRef](length)
      val arrayOfU = new Array[AnyRef](length)
      val arrayOfV = new Array[AnyRef](length)

      var i = 0
      while (i < length) {
        val tup = arrayOfStruct(i)
        if (tup != null) {
          arrayOfT(i) = tup._1.asInstanceOf[AnyRef]
          arrayOfU(i) = tup._2.asInstanceOf[AnyRef]
          arrayOfV(i) = tup._3.asInstanceOf[AnyRef]
        }
        i += 1
      }

      StructOfArray(arrayOfT, arrayOfU, arrayOfV)
    }

  def fromRepr[T, U, V](structOfArray: StructOfArray[T, U, V] @high): Array[(T, U, V)] =
    if (structOfArray == null)
      null
    else
      sys.error("There's no going back!"): Array[(T, U, V)]

  // Operations
  def extension_length[T, U, V](soa: StructOfArray[T, U, V] @high): Int = soa.arrayOfT.length

  def extension_apply[T, U, V](soa: StructOfArray[T, U, V] @high, idx: Int): (T, U, V) =
    (soa.arrayOfT(idx).asInstanceOf[T],
     soa.arrayOfU(idx).asInstanceOf[U],
     soa.arrayOfV(idx).asInstanceOf[V])

  def extension_update[T, U, V](soa: StructOfArray[T, U, V] @high, idx: Int, value: (T, U, V)): Unit = {
    soa.arrayOfT(idx) = value._1.asInstanceOf[AnyRef]
    soa.arrayOfU(idx) = value._2.asInstanceOf[AnyRef]
    soa.arrayOfV(idx) = value._3.asInstanceOf[AnyRef]
  }
}