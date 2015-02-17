package ildl

/**
 *  A trait defining an iLDL transformation description object
 */
abstract sealed trait TransformationDescription

trait RigidTransformationDescription extends TransformationDescription {
  type High
  type Repr
  def toRepr(high: High): Repr @high
  def fromRepr(lo: Repr @high): High

  // Add your own transformations here, please
}

trait FreestyleTransformationDescription extends TransformationDescription {
  // this is how your transformation should look:
  // def toRepr[T1, T2, ...](high: High): Repr @high
  // def fromRepr[T1, T2, ...](repr: Repr @high): High

  // Add your own transformations here, please
}