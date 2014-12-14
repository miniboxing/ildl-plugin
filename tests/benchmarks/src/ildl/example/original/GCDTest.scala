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
    def approx_/(n2: (Int, Int)): (Int, Int) = {
      val denom = n2 * n2.c
      val numer = n1 * n2.c
      assert(denom.im == 0)
      (math.round(numer.re.toFloat / denom.re), math.round(numer.im.toFloat / denom.re))
    }
  }

  def gcd(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {
    if (n1.norm >= n2.norm)
      gcd_in(n1, n2)
    else
      gcd_in(n2, n1)
  }

  @annotation.tailrec
  private def gcd_in(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {
    val quotient  = n1 approx_/ n2
    val remainder = n1 - (quotient * n2)

    if (remainder == (0, 0)) n2 else gcd_in(n2, remainder)
  }

  val rnd = new util.Random(0)
  val start = System.currentTimeMillis
  var iter = 100000
  while (iter > 0) {
    gcd((rnd.nextInt(10000), rnd.nextInt(10000)), (rnd.nextInt(10000), rnd.nextInt(10000)))
    iter -= 1
  }
  val stop  = System.currentTimeMillis
  println("iterations took " + (stop - start) + " ms")
}