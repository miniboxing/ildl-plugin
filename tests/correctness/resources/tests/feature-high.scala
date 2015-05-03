package test

import ildl._

object Transf extends RigidTransformationDescription {
  type High = Int
  type Repr = Long
  def toRepr(high: Int): Long @high = high
  def toHigh(lo: Long @high): Int = lo.toInt
}
