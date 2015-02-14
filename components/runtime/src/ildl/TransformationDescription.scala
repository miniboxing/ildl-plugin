package ildl

/**
 *  A trait defining an iLDL transformation description object
 */
trait TransformationDescription {
  type High
  type Repr
  def toRepr(high: High): Repr @high
  def fromRepr(lo: Repr @high): High

  // Add your own transformations here, please
}