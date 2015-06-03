package ildl
package benchmark
package gcd

//
// The transformation performs several optimizations at once:
//  - introducing a specialized tuple
//  - encoding the tuple in a long integer
//  - stack-allocating the long integer
//
//         +--- class specialization: the tuple class is already specialized in the Scala backend,
//         |                          so the two integers are stored in the unboxed format. However,
//         |                          a limitation in the Scala specialization (fixed by the
//         |                          miniboxing transformation) dictates that even the specialized
//         |                          class has a pair of generic fields. By adding our
//         |                          hand-specialized alternative to tuples, we eliminate those
//         |                          fields, saving on the heap memory footprint and thus on the
//         |                          GC cycles.
//         |
//         |                  +--> encoding: instead of having a class with two fields we have the
//         |                  |              java.lang.Long which encodes the two fields in one. We
//         |                  |              expect to get a small performance hit, as the real and
//         |                  |              imaginary components have to be encoded.
//         |                  |
//         |                  |                   +--> unboxing: we need to convert j.l.Long to
//         |                  |                   |              scala.Long and the backend unboxes it
//         |                  |                   |              without our intervention.
//         |                  |                   |
// (3, 4) ===> Complex(3, 4) ===> java.lang.Long ===> scala.Long (compiled to Java's unboxed long)
//    \             ^                    ^                ^
//     \___________/                    /                /
//      \ adrt(IntPairTupleSpec)       /                /
//       \                            /                /
//        \                          /                /
//         \________________________/                /
//          \ adrt(IntPairAsBoxedLong)              /
//           \                                     /
//            \                                   /
//             \_________________________________/
//               adrt(IntPairAsGaussianInteger)
//
//

/**
 *  A transformation description object which can encode Gaussian integers,
 *  modelled as pairs of integers (`(Int, Int)`) as Long integers (`Long`)
 *  which are automatically unboxed by the Scala backend.
 *
 *  @see [[http://mathforum.org/library/drmath/view/67068.html]]
 */
object IntPairAsGaussianInteger extends TransformationDescription {

  // coercions:
  def toRepr(pair: (Int, Int)): Long @high = pack(pair._1, pair._2)
  def toHigh(l: Long @high): (Int, Int) = (re(l), im(l))

  // constructor:
  def ctor_Tuple2(_1: Int, _2: Int): Long @high = pack(_1, _2)

  // interface: (no need to expose everything)
  def implicit_IntPairAsGaussianIntegerImplicit_%(n1: Long @high, n2: Long @high): Long @high = %(n1, n2)
  def implicit_IntPairAsGaussianIntegerImplicit_norm(n: Long @high): Int = norm(n)
  // TODO: Add the interface methods here, to avoid converting
  // `Long` back into `(Int, Int)` to execute operatiors...

  // implementation:
  private def pack(re: Int, im: Int) = (re.toLong << 32l) | (im.toLong & 0xFFFFFFFFl)
  private def re(l: Long @high): Int = (l >>> 32).toInt
  private def im(l: Long @high): Int = (l & 0xFFFFFFFF).toInt
  private def norm(l: Long @high): Int = re(l)^2 + im(l)^2
  private def c(l: Long @high): Long @high = pack(re(l), -im(l))
  private def +(n1: Long @high, n2: Long @high): Long @high = pack(re(n1) + re(n2), im(n1) + im(n2))
  private def -(n1: Long @high, n2: Long @high): Long @high = pack(re(n1) - re(n2), im(n1) - im(n2))
  private def *(n1: Long @high, n2: Long @high): Long @high = pack(re(n1) * re(n2) - im(n1) * im(n2), re(n1) * im(n2) + im(n1) * re(n2))
  private def *(n1: Long @high, n2: Int): Long @high = pack(re(n1) * n2, im(n1) * n2)
  private def /(n1: Long @high, n2: Long @high): Long @high = {
    val denom = *(n2, c(n2))
    val numer = *(n1, c(n2))
    assert(im(denom) == 0)
    pack(math.round(re(numer).toFloat / re(denom)), math.round(im(numer).toFloat / re(denom)))
  }
  private def %(n1: Long @high, n2: Long @high): Long @high = pos(this.-(n1,*(/(n1, n2), n2)))
  private def pos(n1: Long @high): Long @high = if (re(n1) < 0) pack(-re(n1), -im(n1)) else n1
  private def bits(n1: Long @high): String = f"$n1%016x"
}

/**
 *  A simpler transformation, which uses java.lang.Long instead of scala.Long
 *  as the transformation target
 */
object IntPairAsBoxedLong extends TransformationDescription {

  // Here we're un-importing scala.Long and importing java.lang.Long
  // so we can keep the same code
  import scala.{ Long => _ }
  import java.lang.Long

  // coercions:
  def toRepr(pair: (Int, Int)): Long @high = pack(pair._1, pair._2)
  def toHigh(l: Long @high): (Int, Int) = (re(l), im(l))

  // constructor:
  def ctor_Tuple2(_1: Int, _2: Int): Long @high = pack(_1, _2)

  // interface: (no need to expose everything)
  def implicit_IntPairAsGaussianIntegerImplicit_%(n1: Long @high, n2: Long @high): Long @high = %(n1, n2)
  def implicit_IntPairAsGaussianIntegerImplicit_norm(n: Long @high): Int = norm(n)
  // TODO: Add the interface methods here, to avoid converting
  // `Long` back into `(Int, Int)` to execute operatiors...

  // implementation:
  private def pack(re: Int, im: Int) = (re.toLong << 32l) | (im.toLong & 0xFFFFFFFFl)
  private def re(l: Long @high): Int = (l >>> 32).toInt
  private def im(l: Long @high): Int = (l & 0xFFFFFFFF).toInt
  private def norm(l: Long @high): Int = re(l)^2 + im(l)^2
  private def c(l: Long @high): Long @high = pack(re(l), -im(l))
  private def +(n1: Long @high, n2: Long @high): Long @high = pack(re(n1) + re(n2), im(n1) + im(n2))
  private def -(n1: Long @high, n2: Long @high): Long @high = pack(re(n1) - re(n2), im(n1) - im(n2))
  private def *(n1: Long @high, n2: Long @high): Long @high = pack(re(n1) * re(n2) - im(n1) * im(n2), re(n1) * im(n2) + im(n1) * re(n2))
  private def *(n1: Long @high, n2: Int): Long @high = pack(re(n1) * n2, im(n1) * n2)
  private def /(n1: Long @high, n2: Long @high): Long @high = {
    val denom = *(n2, c(n2))
    val numer = *(n1, c(n2))
    assert(im(denom) == 0)
    pack(math.round(re(numer).toFloat / re(denom)), math.round(im(numer).toFloat / re(denom)))
  }
  private def %(n1: Long @high, n2: Long @high): Long @high = pos(this.-(n1,*(/(n1, n2), n2)))
  private def pos(n1: Long @high): Long @high = if (re(n1) < 0) pack(-re(n1), -im(n1)) else n1
  private def bits(n1: Long @high): String = f"$n1%016x"
}


/**
 *  A simpler transformation, which uses a manually specialized tuple class
 */
object IntPairTupleSpec extends TransformationDescription {

  // Here we're un-importing scala.Long and importing java.lang.Long
  // so we can keep the same code
  import scala.{ Long => _ }

  case class Complex(_1: Int, _2: Int)

  // coercions:
  def toRepr(pair: (Int, Int)): Complex @high = pack(pair._1, pair._2)
  def toHigh(l: Complex @high): (Int, Int) = (re(l), im(l))

  // constructor:
  def ctor_Tuple2(_1: Int, _2: Int): Complex @high = pack(_1, _2)

  // interface: (no need to expose everything)
  def implicit_IntPairAsGaussianIntegerImplicit_%(n1: Complex @high, n2: Complex @high): Complex @high = %(n1, n2)
  def implicit_IntPairAsGaussianIntegerImplicit_norm(n: Complex @high): Int = norm(n)
  // TODO: Add the interface methods here, to avoid converting
  // `Complex` back into `(Int, Int)` to execute operatiors...

  // implementation:
  private def pack(re: Int, im: Int): Complex @high = Complex(re, im)
  private def re(l: Complex @high): Int = l._1
  private def im(l: Complex @high): Int = l._2
  private def norm(l: Complex @high): Int = re(l)^2 + im(l)^2
  private def c(l: Complex @high): Complex @high = pack(re(l), -im(l))
  private def +(n1: Complex @high, n2: Complex @high): Complex @high = pack(re(n1) + re(n2), im(n1) + im(n2))
  private def -(n1: Complex @high, n2: Complex @high): Complex @high = pack(re(n1) - re(n2), im(n1) - im(n2))
  private def *(n1: Complex @high, n2: Complex @high): Complex @high = pack(re(n1) * re(n2) - im(n1) * im(n2), re(n1) * im(n2) + im(n1) * re(n2))
  private def *(n1: Complex @high, n2: Int): Complex @high = pack(re(n1) * n2, im(n1) * n2)
  private def /(n1: Complex @high, n2: Complex @high): Complex @high = {
    val denom = *(n2, c(n2))
    val numer = *(n1, c(n2))
    assert(im(denom) == 0)
    pack(math.round(re(numer).toFloat / re(denom)), math.round(im(numer).toFloat / re(denom)))
  }
  private def %(n1: Complex @high, n2: Complex @high): Complex @high = pos(this.-(n1,*(/(n1, n2), n2)))
  private def pos(n1: Complex @high): Complex @high = if (re(n1) < 0) pack(-re(n1), -im(n1)) else n1
  private def bits(n1: Complex @high): String = "<not applicable>"
}
