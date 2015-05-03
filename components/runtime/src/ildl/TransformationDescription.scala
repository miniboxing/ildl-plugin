package ildl

/**
 *  A trait defining an iLDL transformation description object.
 *  It must be extended by any transformation description object.
 *  The minimum description contains two elements:
 *
 *  ```
 *    def toRepr[T1, T2, ...](high: (Int, Int)): Long @high
 *    def toHigh[T1, T2, ...](repr: Long @high): (Int, Int)
 *  ```
 *
 *  Aside from the coercions, there are other things you can
 *  specify in the description object. These are:
 *
 *   * Extension methods
 *   * Extension methods for implicitly added methods
 *   * Constructor interception methods
 *
 *  For more information, please see the examples included in the project.
 *
 *  @see [[RigidTransformationDescription]] for a more rigid specification.
 */
trait TransformationDescription {
  // Add your own transformations here, please
}

/**
 * A more rigid transformation descritption object, where the
 * high-level and representation types are specified explicitly:
 * ```
 *   type High = (Int, Int)
 *   type Repr = Long
 *   def toRepr(high: (Int, Int)): Long @high = ...
 *   def toHigh(repr: Long @high): (Int, Int) = ...
 * ```
 *
 * @see [[TransformationDescription]] for the members you can add
 * to the objects extending this trait
 */
trait RigidTransformationDescription extends TransformationDescription {
  type High
  type Repr
  def toRepr(high: High): Repr @high
  def toHigh(lo: Repr @high): High

  // Add your own transformations here, please
}
