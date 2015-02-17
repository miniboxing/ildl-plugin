package test

import ildl._

object Transf extends RigidTransformationDescription {
  type High = Int
  type Repr = Long
  def toRepr(high: Int): Long @high = high
  def fromRepr(lo: Long @high): Int = lo.toInt

  // illegal: nested transformation description objects
  object Illegal extends RigidTransformationDescription {
    type High = Int
    type Repr = Long
    def toRepr(high: Int): Long @high = high
    def fromRepr(lo: Long @high): Int = lo.toInt
  }
}
