package ildl
package benchmark
package deforest
package miniboxed

/**
 *  This is the lazy list we're planning to use instead of
 *  scala.collection.immutable.List[T].
 *
 *  The list can be in two states:
 *   * it's just a wrapper over a List[T], with no accumulated maps
 *   * it accumulated maps, so it's a List[T] with a function that
 *     composes the accumulated maps
 */
abstract sealed trait LazyList[@miniboxed T] {
  
  //
  // Along with optimizing generics, the miniboxing plugin
  // also transforms the function representation and performs
  // other code transformations. We won't go into the list
  // here, but we're preparing a paper on this:
  // https://infoscience.epfl.ch/record/208797 
  //
  
  /** Map */
  def map[@miniboxed U, That](f: T => U): LazyList[U]

  /** Fold */
  def foldLeft[@miniboxed U](z: U)(f: (U, T) => U): U

  /** Length */
  def length: Int

  /** Force: get a list */
  def force: List[T]
}

/**
 * This class corresponds to a wrapped list, with no functions
 * collected so far. It is one of the two cases of [[LazyList]].
 */
class LazyListWrapper[@miniboxed T](list: List[T]) extends LazyList[T] {

  def map[@miniboxed U, That](f: T => U): LazyList[U] =
    new LazyListMapper(list, f)

  def foldLeft[@miniboxed U](z: U)(f: (U, T) => U): U = {
    var lst = list
    var acc  = z
    while(!lst.isEmpty) {
      acc = f(acc, lst.head)
      lst = lst.tail
    }
    acc
  }

  def length: Int = list.length // since we don't support filter yet

  def force: List[T] = list
}

/**
 * This class corresponds to a wrapped list, with one or more
 * functions delayed. It is one of the two cases of [[LazyList]].
 */
class LazyListMapper[@miniboxed T, @miniboxed To](list: List[To], fs: To => T) extends LazyList[T] {

  def map[@miniboxed U, That](f: T => U): LazyList[U] =
    new LazyListMapper(list, fs andThen f)

  def foldLeft[@miniboxed U](z: U)(f: (U, T) => U): U = {
    var lst = list
    var acc  = z
    while(!lst.isEmpty) {
      acc = f(acc, fs(lst.head))
      lst = lst.tail
    }
    acc
  }

  def length: Int = list.length // since we don't support filter yet

  def force: List[T] = list.map(fs)
}
