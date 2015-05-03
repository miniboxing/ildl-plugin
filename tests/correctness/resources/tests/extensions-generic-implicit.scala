package tests

import ildl._

object Test {

  case class MyList[T](arr: Array[T])

  implicit class MyListOps[T](myl: MyList[T]) {
    def foo(i: Int) = myl.arr(i)
    def bar(i: Int) = myl.arr(i)
  }

  object ListAsArray extends TransformationDescription {
    def toRepr[T](list: MyList[T]): Array[T] @high = ???
    def toHigh[T](arr: Array[T] @high): MyList[T] = ???
    def implicit_MyListOps_foo[T](arr: Array[T] @high, i: Int) = ???     // okay
    def implicit_MyListOps_foo[T](arr: Array[T] @high, i: String) = ???  // not okay, i: String
  }

  adrt(ListAsArray) { 
    val a = MyList(Array(1,2,3))
    val b = a.foo(2)  // okay
    val c = a.bar(3)  // not working
  }
}
