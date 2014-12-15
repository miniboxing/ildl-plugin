package ildl.example.transformed

import scala.annotation.Annotation
import scala.annotation.TypeConstraint

object GCDTestTrans extends App {

  class arg extends Annotation with TypeConstraint
  class rec extends Annotation with TypeConstraint

  // http://mathforum.org/library/drmath/view/67068.html
  object IntPairAsGaussianInteger {
    type In = (Int, Int)
    type Out = Long
    private def pack(re: Int, im: Int) = (re.toLong << 32l) | (im.toLong & 0xFFFFFFFFl)
    def in2out(pair: (Int, Int)) = pack(pair._1, pair._2)
    def out2in(l: Long @arg): (Int, Int) = (re(l), im(l))
    def re(l: Long @rec): Int = (l >>> 32).toInt
    def im(l: Long @rec): Int = (l & 0xFFFFFFFF).toInt
    def norm(l: Long @rec) = re(l)^2 + im(l)^2
    def c(l: Long @rec): Long @rec = pack(re(l), -im(l))
    def +(n1: Long @rec, n2: Long @arg): Long = pack(re(n1) + re(n2), im(n1) + im(n2))
    def -(n1: Long @rec, n2: Long @arg): Long = pack(re(n1) - re(n2), im(n1) - im(n2))
    def *(n1: Long @rec, n2: Long @arg): Long = pack(re(n1) * re(n2) - im(n1) * im(n2), re(n1) * im(n2) + im(n1) * re(n2))
    def *(n1: Long @rec, n2: Int @arg): Long = pack(re(n1) * n2, im(n1) * n2)
    def /(n1: Long @rec, n2: Long @arg): Long = {
      val denom = *(n2, c(n2))
      val numer = *(n1, c(n2))
      assert(im(denom) == 0)
      pack(math.round(re(numer).toFloat / re(denom)), math.round(im(numer).toFloat / re(denom)))
    }
    def %(n1: Long, n2: Long): Long = pos(IntPairAsGaussianInteger.-(n1,*(/(n1, n2), n2)))
    def pos(n1: Long): Long = if (re(n1) < 0) pack(-re(n1), -im(n1)) else n1
    def bits(n1: Long) = f"$n1%016x"
  }

  import IntPairAsGaussianInteger._

  @annotation.tailrec
  private def gcd(n1: Long, n2: Long): Long = {
    val remainder = %(n1, n2)
    if (norm(remainder) == 0) n2 else gcd(n2, remainder)
  }

  println(out2in(timed(() => gcd(*(in2out((55, 2)),in2out((10, 4))), *(in2out((17, 13)), in2out((10, 4)))))))

  def timed[T](op: () => T): T = {
    val start = System.currentTimeMillis
    var iter = 1000
    while (iter > 0) {
      op()
      iter -= 1
    }
    val stop  = System.currentTimeMillis
    println("The operation took " + (stop - start) + " us.")
    op()
  }
}

