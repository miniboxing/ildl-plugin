package ildl

import scala.annotation.StaticAnnotation
import scala.annotation.TypeConstraint

/**
 *  Used to distinguish between a `Repr` value that corresponds
 *  to the high-level type and one that corresponds to the actual
 *  `Repr` type. For example, when transforming BigInt to Long,
 *  you have two methods:
 *
 *  class BigIng ... {
 *    def *(other: BigInt) = ...
 *    def *(other: Long) = ...
 *  }
 *
 *  When you're writing the code for the transformation you will
 *  have:
 *
 *  ```
 *  object BigIntAsLong ... {
 *    ...
 *    def extension_*(recv: Long, other: Long): Long = wtf?!?
 *    def extension_*(recv: Long, other: Long): Long = wtf?!?
 *  }
 *  ```
 *
 *  The @high annotation helps here:
 *
 *  ```
 *  object BigIntAsLong ... {
 *    ...
 *    // corresponds to the multiplication between 2 bigInts:
 *    def extension_*(recv: Long @high, other: Long @high): Long = ...
 *    // corresponds to the multiplication between a bigInt and a long:
 *    def extension_*(recv: Long @high, other: Long): Long = ...
 *  }
 *  ```
 */
class high extends StaticAnnotation with TypeConstraint {
  /**
   *  TODO: Document this
   */
  def this(transf: TransformationDescription with Singleton) =
    this()
}