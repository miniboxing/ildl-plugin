package ildl
package benchmark
package deforest
package erased


/**
 *  This is the lazy list we're planning to use instead of
 *  scala.collection.immutable.List[T].
 *
 *  The list can be in two states:
 *   * it's just a wrapper over a List[T], with no accumulated maps
 *   * it accumulated maps, so it's a List[T] with a function that
 *     composes the accumulated maps
 */
abstract sealed trait LazyList[T] {

  //
  // NOTE: We add the @api annotation to functions to notify the miniboxing plugin
  // is should keep the Scala function representation. This prevents the miniboxing
  // from artificially improving the performance of the erased benchmark.
  //

  /** Map */
  def map[U, That](f: (T => U) @api): LazyList[U]

  /** Fold */
  def foldLeft[U](z: U)(f: ((U, T) => U) @api): U

  /** Length */
  def length: Int

  /** Force: get a list */
  def force: List[T]
}

/**
 * This class corresponds to a wrapped list, with no functions
 * collected so far. It is one of the two cases of [[LazyList]].
 */
class LazyListWrapper[T](list: List[T]) extends LazyList[T] {

  def map[U, That](f: (T => U) @api): LazyList[U] =
    new LazyListMapper(list, f)

  def foldLeft[U](z: U)(f: ((U, T) => U) @api): U = {
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
class LazyListMapper[T, To](list: List[To], fs: (To => T) @api) extends LazyList[T] {

  def map[U, That](f: (T => U) @api): LazyList[U] =
    new LazyListMapper(list, fs andThen f)

  def foldLeft[U](z: U)(f: ((U, T) => U) @api): U = {
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
