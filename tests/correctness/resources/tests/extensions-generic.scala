package tests

import ildl._

object Test {

  object ListAsArray extends TransformationDescription {
    def toRepr[T](list: List[T]): Array[T] @high = ???
    def toHigh[T](arr: Array[T] @high): List[T] = ???
    def extension_apply[T](recv: Array[T] @high, index: Int): T = ??? // okay
    def extension_toString[T](recv: Array[T] @high): Long = ???       // bad
    // TODO: This shouldn't work, but it does:
    //def extension_toString(recv: Array[String] @high): String = ???     // bad
  }

  adrt(ListAsArray) { 
    val a = List(1,2,3)
    val b = a(2)
    val c = a.toString
  }
}
