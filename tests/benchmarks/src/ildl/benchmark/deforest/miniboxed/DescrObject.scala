package ildl
package benchmark
package deforest
package miniboxed

import scala.collection.generic.CanBuildFrom
import miniboxing.runtime.math.MiniboxedNumeric

/**
 * Transform the List[T] into LazyList[T] with specialized versions.
 * @see See the [[ildl.benchmark.deforest.LeastSquares]] for transformation details.
 */
object ListAsLazyList extends TransformationDescription {

  //
  // Along with optimizing generics, the miniboxing plugin
  // also transforms the function representation and performs
  // other code transformations. We won't go into the list
  // here, but we're preparing a paper on this:
  // https://infoscience.epfl.ch/record/208797
  //

  // conversions:
  def toRepr[@miniboxed T](list: List[T]): LazyList[T] @high = new LazyListWrapper(list)
  def toHigh[@miniboxed T](lazylist: LazyList[T] @high): List[T] = lazylist.force

  // optimizing the length:
  def extension_length[@miniboxed T](lazylist: LazyList[T] @high) =
    lazylist.length

  // optimizing the map method:
  def extension_map[@miniboxed T, @miniboxed U, @miniboxed That]
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
  def extension_foldLeft[@miniboxed T, @miniboxed U]
                              (lazylist: LazyList[T] @high)
                              (z: U)(f: (U, T) => U): U =
    lazylist.foldLeft(z)(f)

  // optimizing the sum method:
  def extension_sum[@miniboxed T, @miniboxed U >: T]
                              (lazylist: LazyList[T] @high)
                              (implicit num: Numeric[U]): U = {
    // while we wait for https://github.com/miniboxing/miniboxing-plugin/issues/227:
    val mnum = MiniboxedNumeric.DoubleAsIfMbIntegral.asInstanceOf[MiniboxedNumeric[U]]
    lazylist.foldLeft(mnum.zero)(mnum.plus)
  }

  // optimizing the implicit force method:
  def implicitly_listForce_force[@miniboxed T](lazylist: LazyList[T] @high) =
    lazylist.force
}