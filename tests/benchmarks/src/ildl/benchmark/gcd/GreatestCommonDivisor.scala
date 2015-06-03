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

  // See the comment in DescrObject for information on the transformation objects:

  adrt(IntPairTupleSpec) {
    def gcd_adrt_1(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {

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
  }

  adrt(IntPairAsBoxedLong) {
    def gcd_adrt_2(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {

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
  }

  adrt(IntPairAsGaussianInteger) {
    def gcd_adrt_3(n1: (Int, Int), n2: (Int, Int)): (Int, Int) = {

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
  }
}