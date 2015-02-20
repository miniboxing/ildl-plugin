package tests

import ildl._
import scala.collection.generic.CanBuildFrom
import scala.language.postfixOps

abstract sealed trait LazyList[T] {
  /** Map */
  def map[U, That](f: T => U)(implicit bf: CanBuildFrom[List[T], U, That]): LazyList[U]

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
                               (f: T => U)(implicit bf: CanBuildFrom[List[T], U, That]): LazyList[U] @high =
    lazylist.map(f)(bf)

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

  println("LazyListWrapper created")

  def map[U, That](f: T => U)(implicit bf: CanBuildFrom[List[T], U, That]) = {
    LazyList.checkCBF(bf)
    new LazyListMapper(list, f)
  }

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

  println("LazyListMapper created")

  def map[U, That](f: T => U)(implicit bf: CanBuildFrom[List[T], U, That]) = {
    LazyList.checkCBF(bf)
    new LazyListMapper(list, fs andThen f)
  }

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

object LazyList {
  def checkCBF(bf: CanBuildFrom[_,_,_]): Unit = {
    // sanity check => we could accept random canBulildFrom objects,
    // but that makes the transformation slightly more complex
    assert(bf == List.ReusableCBF, "The LazyList transformation only supports " +
                                   "using the default `CanBuildFromObject`" +
                                   "from the Scala collections library.")
  }
}


object DeforestTest {
  adrt(ListAsLazyList){
    def leastSquaresADRT(data: List[(Double, Double)]): (Double, Double) = {
      val size = data.length
      val sumx = data.map(_._1).sum
      val sumy = data.map(_._2).sum
      val sumxy = data.map(p => p._1 * p._2).sum
      val sumxx = data.map(p => p._1 * p._1).sum

      val slope  = (size * sumxy - sumx * sumy) / (size * sumxx - sumx * sumx)
      val offset = (sumy * sumxx - sumx * sumxy) / (size * sumxx - sumx * sumx)

      (slope, offset)
    }
  }

  def main(args: Array[String]): Unit = {
    val data1 = (1 to 100) map(_.toDouble) toList
    val data2 = data1
    val (slope, offset) = leastSquaresADRT(data1 zip data2)
    val eps = 1E-6
    assert(math.abs(slope - 1) < eps, "slope: " + slope)
    assert(math.abs(offset) < eps, "offset: " + offset)
    println("Băi, chestia asta chiar merge, pe bune!")
  }
}
