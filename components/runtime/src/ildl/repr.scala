package ildl

import scala.annotation.StaticAnnotation
import scala.annotation.TypeConstraint

/**
 *  Used to distinguish between an intentional `Repr` type
 *  and accidental occurrence of an unrelated `Repr` type,
 *  without the High => Repr transformation semantics.
 */
class high extends StaticAnnotation with TypeConstraint