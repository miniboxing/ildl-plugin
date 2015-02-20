package ildl
package benchmark
package deforest

import scala.collection.generic.CanBuildFrom

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
  /** Map */
  def map[U, That](f: T => U): LazyList[U]

  /** Fold */
  def foldLeft[U](z: U)(f: (U, T) => U): U

  /** Length */
  def length: Int

  /** Force: get a list */
  def force: List[T]
}





object ListAsLazyList extends FreestyleTransformationDescription {

  // conversions:
  def toRepr[T](list: List[T]): LazyList[T] @high = new LazyListWrapper(list)
  def fromRepr[T](lazylist: LazyList[T] @high): List[T] = lazylist.force

  // optimizing the length:
  def extension_length[T](lazylist: LazyList[T] @high) =
    lazylist.length

  // optimizing the map method:
  def extension_map[T, U, That](lazylist: LazyList[T] @high)
                               (f: T => U)(implicit bf: CanBuildFrom[List[T], U, That]): LazyList[U] @high = {

    // sanity check => we could accept random canBulildFrom objects,
    // but that makes the transformation slightly more complex
    assert(bf == List.ReusableCBF, "The LazyList transformation only supports " +
                                   "using the default `CanBuildFromObject`" +
                                   "from the Scala collections library.")
    lazylist.map(f)
  }

  // optimizing the foldLeft method:
  def extension_foldLeft[T, U](lazylist: LazyList[T] @high)
                              (z: U)(f: (U, T) => U): U =
    lazylist.foldLeft(z)(f)

  // optimizing the sum method:
  def extension_sum[T, U >: T](lazylist: LazyList[T] @high)
                              (implicit num: Numeric[U]): U =
    lazylist.foldLeft(num.zero)(num.plus)

  // optimizing the sum method:
  def extension_sum[T, U >: T](lazylist: LazyList[T] @high) =
    lazylist.length

  // optimizing the implicit force method:
  def implicitly_listForce_force[T](lazylist: LazyList[T] @high) =
    lazylist.force
}





class LazyListWrapper[T](list: List[T]) extends LazyList[T] {

  def map[U, That](f: T => U) =
    new LazyListMapper(list, f)

  def foldLeft[U](z: U)(f: (U, T) => U): U = {
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





class LazyListMapper[T, To](list: List[To], fs: To => T) extends LazyList[T] {

  def map[U, That](f: T => U) =
    new LazyListMapper(list, fs andThen f)

  def foldLeft[U](z: U)(f: (U, T) => U): U = {
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
