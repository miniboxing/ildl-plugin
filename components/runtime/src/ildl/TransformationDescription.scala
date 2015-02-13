package ildl

/**
 *  A trait defining an iLDL transformation description object
 */
trait TransformationDescription {
  type High
  type Repr
  def toRepr(high: High): Repr @repr
  def fromRepr(lo: Repr @repr): High

  // Add your own transformations here, please
}