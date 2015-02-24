package ildl
package benchmark
package deforest

import scala.collection.generic.CanBuildFrom


object ListAsLazyList extends FreestyleTransformationDescription {

  // conversions:
  def toRepr[@specialized T](list: List[T]): LazyList[T] @high = new LazyListWrapper(list)
  def fromRepr[@specialized T](lazylist: LazyList[T] @high): List[T] = lazylist.force

  // optimizing the length:
  def extension_length[@specialized T](lazylist: LazyList[T] @high) =
    lazylist.length

  // optimizing the map method:
  def extension_map[@specialized T, @specialized U, That]
                               (lazylist: LazyList[T] @high)
                               (f: T => U)(implicit bf: CanBuildFrom[List[T], U, That]): LazyList[U] @high = {

    // sanity check => we could accept random canBulildFrom objects,
    // but that makes the transformation slightly more complex
    assert(bf == List.ReusableCBF, "The LazyList transformation only supports " +
                                   "using the default `CanBuildFromObject`" +
                                   "from the Scala collections library.")
    lazylist.map(f)
  }

  // optimizing the foldLeft method:
  def extension_foldLeft[@specialized T, @specialized U]
                              (lazylist: LazyList[T] @high)
                              (z: U)(f: (U, T) => U): U =
    lazylist.foldLeft(z)(f)

  // optimizing the sum method:
  def extension_sum[@specialized T, @specialized U >: T]
                              (lazylist: LazyList[T] @high)
                              (implicit num: Numeric[U]): U =
    lazylist.foldLeft(num.zero)(num.plus)

  // optimizing the implicit force method:
  def implicitly_listForce_force[@specialized T](lazylist: LazyList[T] @high) =
    lazylist.force
}