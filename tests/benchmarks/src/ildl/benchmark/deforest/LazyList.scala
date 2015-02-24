package ildl
package benchmark
package deforest

/**
 *  This is the lazy list we're planning to use instead of
 *  scala.collection.immutable.List[T].
 *
 *  The list can be in two states:
 *   * it's just a wrapper over a List[T], with no accumulated maps
 *   * it accumulated maps, so it's a List[T] with a function that
 *     composes the accumulated maps
 */
abstract sealed trait LazyList[@specialized T] {
  /** Map */
  def map[@specialized U, That](f: T => U): LazyList[U]

  /** Fold */
  def foldLeft[@specialized U](z: U)(f: (U, T) => U): U

  /** Length */
  def length: Int

  /** Force: get a list */
  def force: List[T]
}


class LazyListWrapper[@specialized T](list: List[T]) extends LazyList[T] {

  def map[@specialized U, That](f: T => U) =
    new LazyListMapper(list, f)

  def foldLeft[@specialized U](z: U)(f: (U, T) => U): U = {
    var lst = list
    var acc  = z
    while(lst != Nil) {
      acc = f(acc, lst.head)
      lst = lst.tail
    }
    acc
  }

  def length: Int = list.length // since we don't support filter yet

  def force: List[T] = list
}


class LazyListMapper[@specialized T, @specialized To](list: List[To], fs: To => T) extends LazyList[T] {

  def map[@specialized U, That](f: T => U) =
    new LazyListMapper(list, fs andThen f)

  def foldLeft[@specialized U](z: U)(f: (U, T) => U): U = {
    var lst = list
    var acc  = z
    while(lst != Nil) {
      acc = f(acc, fs(lst.head))
      lst = lst.tail
    }
    acc
  }

  def length: Int = list.length // since we don't support filter yet

  def force: List[T] = list.map(fs)
}
