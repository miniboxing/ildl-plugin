package ildl
package benchmark

package object gcd {

  // http://mathforum.org/library/drmath/view/67068.html
  implicit class IntPairAsGaussianIntegerImplicit(n1: (Int, Int)) {
    def c = (n1.re, -n1.im)
    def re = n1._1
    def im = n1._2
    def norm = n1.re * n1.re + n1.im * n1.im
    def +(n2: (Int, Int)): (Int, Int) = (n1.re + n2.re, n1.im + n2.im)
    def -(n2: (Int, Int)): (Int, Int) = (n1.re - n2.re, n1.im - n2.im)
    def *(n2: (Int, Int)): (Int, Int) = (n1.re * n2.re - n1.im * n2.im, n1.re * n2.im + n1.im * n2.re)
    def *(n2: Int): (Int, Int) = (n1.re * n2, n1.re * n2)
    def /(n2: (Int, Int)): (Int, Int) = {
      val denom = n2 * n2.c
      val numer = n1 * n2.c
      assert(denom.im == 0)
      (math.round(numer.re.toFloat / denom.re), math.round(numer.im.toFloat / denom.re))
    }
    def %(n2: (Int, Int)): (Int, Int) =
      (n1 - (n1 / n2 * n2)).pos
    def pos: (Int, Int) =
      if (n1.re < 0) (-n1.re, -n1.im) else n1
  }
}
