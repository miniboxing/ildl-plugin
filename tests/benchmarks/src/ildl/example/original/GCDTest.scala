package ildl.example.original

object GCDTest extends App {

  // http://mathforum.org/library/drmath/view/67068.html
  implicit class PairOfIntAsGaussianInteger(n1: (Int, Int)) {
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

  def gcd_reorder(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {
    if (n1.norm >= n2.norm)
      gcd(n1, n2)
    else
      gcd(n2, n1)
  }

  @annotation.tailrec
  private def gcd(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {
    val remainder = n1 % n2
    if (remainder == (0, 0)) n2 else gcd(n2, remainder)
  }


  println(timed(() => gcd((544,185), (131,181))))


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

