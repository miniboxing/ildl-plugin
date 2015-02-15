package test

import ildl._

object Transf extends TransformationDescription {
  type High = Int
  type Repr = Long
  def toRepr(high: Int): Long @high = high
  def fromRepr(lo: Long @high): Int = lo.toInt

  // @high used to annotate something that 
  // is not the high type:
  val hi: String @high = "xxx"
}
