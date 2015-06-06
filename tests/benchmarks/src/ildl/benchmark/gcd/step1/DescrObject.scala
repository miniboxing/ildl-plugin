package ildl
package benchmark
package gcd
package step1

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-%7E-Data-Encoding
//

/**
 *  A simpler transformation, which uses a manually specialized tuple class.
 *  @see the comment in [[ildl.benchmark.gcd.GreatestCommonDivisor]] for more information
 */
object IntPairAsGaussianInteger extends TransformationDescription {

  // Here we're un-importing scala.Long and switching to Complex
  // but it's ultimately the same code
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

  // extension methods:
  def extension_==(receiver: Complex @high, other: Complex @high): Boolean = receiver == other
  def extension_toString(receiver: Complex @high): String = toHigh(receiver).toString

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