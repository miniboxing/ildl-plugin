package ildl
package benchmark
package gcd

object GreatestCommonDivisor {

  /** Greatest common divisor computation */
  def gcd_direct(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {

    // inner loop:
    @annotation.tailrec
    def gcd_inner(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {
      val remainder = n1 % n2
      if (remainder.norm == 0) n2 else gcd_inner(n2, remainder)
    }

    if (n1.norm >= n2.norm)
      gcd_inner(n1, n2)
    else
      gcd_inner(n2, n1)
  }

  /** Greatest common divisor computation */
  adrt(IntPairAsGaussianInteger) {

    // inner loop:
    def gcd_adrt(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {
      @annotation.tailrec
      def gcd_inner(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {
        val remainder = n1 % n2
        if (remainder.norm == 0) n2 else gcd_inner(n2, remainder)
      }

      if (n1.norm >= n2.norm)
        gcd_inner(n1, n2)
      else
        gcd_inner(n2, n1)
    }
  }
}