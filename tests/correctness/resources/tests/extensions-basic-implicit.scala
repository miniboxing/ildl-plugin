package tests

import ildl._

object Test {

  case class ZZZ(v: Long)

  implicit class ZZZOps(zzz: ZZZ) {
    def +(other: ZZZ) = ZZZ(zzz.v + other.v)
    def -(other: ZZZ) = ZZZ(zzz.v - other.v)
    def *(other: ZZZ) = ZZZ(zzz.v * other.v)
    def /(other: ZZZ) = ZZZ(zzz.v / other.v)
    def %(other: ZZZ) = ZZZ(zzz.v % other.v)
  
  }

  object ZZZAsLong extends TransformationDescription {
    def toRepr(zzz: ZZZ): Long @high = zzz.v
    def toHigh(v: Long @high): ZZZ = ZZZ(v)
    def implicit_ZZZOps_+(recv: Long @high, other: ZZZ): Long @high = ???        // okay, coercing
    def implicit_ZZZOps_-(recv: Long @high, other: Any): Long @high = ???        // okay, coercing
    def implicit_ZZZOps_*(recv: Long @high, other: Long @high): Long @high = ??? // okay, non-coercing
    def implicit_ZZZOps_/(recv: Long @high, other: String): Long @high = ???     // wrong, other: String
    def implicit_ZZZOps_%(recv: Long @high, other: Long @high): Long @high = ??? // overloaded
    def implicit_ZZZOps_%(recv: Long @high, other: Any): Long @high = ???
    def extension_toString(): String = ???
  }

  adrt(ZZZAsLong) { 
    val a = ZZZ(1)
    val b = a + a // yes, coercing
    val c = a - a // yes, coercing
    val d = a * a // yes
    val e = a / a // no
    val f = a % a // no
    f.toString    // no
  }
}
