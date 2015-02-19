package tests

import ildl._

object Test {

  object IntAsLong extends FreestyleTransformationDescription {
    def toRepr(i: Int): Long @high = i.toLong
    def fromRepr(l: Long @high): Int = l.toInt
    def extension_+(recv: Long @high, other: Int): Long @high = ???        // okay, coercing
    def extension_-(recv: Long @high, other: Any): Long @high = ???        // okay, coercing
    def extension_*(recv: Long @high, other: Long @high): Long @high = ??? // okay, non-coercing
    def extension_/(recv: Long @high, other: String): Long @high = ???     // wrong, other: String
    def extension_%(recv: Long @high, other: Long @high): Long @high = ??? // overloaded
    def extension_%(recv: Long @high, other: Any): Long @high = ???
    def extension_toString(): String = ???
  }

  adrt(IntAsLong) { 
    val a = 1
    val b = a + a // yes, coercing
    val c = a - a // yes, coercing
    val d = a * a // yes
    val e = a / a // no
    val f = a % a // no
    f.toString    // no
  }
}
