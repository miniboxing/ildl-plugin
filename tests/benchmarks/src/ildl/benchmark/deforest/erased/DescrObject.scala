package ildl
package benchmark
package deforest
package erased

import scala.collection.generic.CanBuildFrom
import ildl.TransformationDescription

/**
 * Transform the List[T] into LazyList[T] with erased generics.
 * @see See the [[ildl.benchmark.deforest.LeastSquares]] for transformation details.
 */
object ListAsLazyList extends TransformationDescription {

  // conversions:
  def toRepr[T](list: List[T]): LazyList[T] @high = new LazyListWrapper(list)
  def toHigh[T](lazylist: LazyList[T] @high): List[T] = lazylist.force

  // optimizing the length:
  def extension_length[T](lazylist: LazyList[T] @high) =
    lazylist.length

  // optimizing the map method:
  def extension_map[T, U, That]
                               (lazylist: LazyList[T] @high)
                               (f: (T => U) @api)(implicit bf: CanBuildFrom[List[T], U, That]): LazyList[U] @high = {

    // sanity check => we could accept random canBulildFrom objects,
    // but that makes the transformation slightly more complex
    assert(bf == List.ReusableCBF, "The LazyList transformation only supports " +
                                   "using the default `CanBuildFromObject`" +
                                   "from the Scala collections library.")
    lazylist.map(f)
  }

  // optimizing the foldLeft method:
  def extension_foldLeft[T, U]
                              (lazylist: LazyList[T] @high)
                              (z: U)(f: ((U, T) => U) @api): U =
    lazylist.foldLeft(z)(f)

  // optimizing the sum method:
  def extension_sum[T, U >: T]
                              (lazylist: LazyList[T] @high)
                              (implicit num: Numeric[U]): U =
    lazylist.foldLeft(num.zero)(num.plus)

  // optimizing the implicit force method:
  def implicitly_listForce_force[T](lazylist: LazyList[T] @high) =
    lazylist.force
}