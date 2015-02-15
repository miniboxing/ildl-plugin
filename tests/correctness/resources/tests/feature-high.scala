package test

import ildl._

object Transf extends TransformationDescription {
  type High = Int
  type Repr = Long
  def toRepr(high: Int): Long @high = high
  def fromRepr(lo: Long @high): Int = lo.toInt
}
