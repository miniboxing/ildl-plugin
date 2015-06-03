package ildl
package benchmark
package gcd

/**
 * The benchmark object. The current benchmark is the Gaussian
 * integer greatest common divisor, which is explained on
 * [[http://math.stackexchange.com/questions/82350/gcd-of-gaussian-integers]].
 *
 * The transformation performs several optimizations at once:
 *  * introducing a specialized tuple
 *  * encoding the tuple in a long integer
 *  * stack-allocating the long integer
 *
 *  {{{
 *
 *         +--- class specialization: the tuple class is already specialized in the Scala backend,
 *         |                          so the two integers are stored in the unboxed format. However,
 *         |                          a limitation in the Scala specialization (fixed by the
 *         |                          miniboxing transformation) dictates that even the specialized
 *         |                          class has a pair of generic fields. By adding our
 *         |                          hand-specialized alternative to tuples, we eliminate those
 *         |                          fields, saving on the heap memory footprint and thus on the
 *         |                          GC cycles.
 *         |
 *         |                  +--> encoding: instead of having a class with two fields we have the
 *         |                  |              java.lang.Long which encodes the two fields in one. We
 *         |                  |              expect to get a small performance hit, as the real and
 *         |                  |              imaginary components have to be encoded.
 *         |                  |
 *         |                  |                   +--> unboxing: we need to convert j.l.Long to
 *         |                  |                   |              scala.Long and the backend unboxes it
 *         |                  |                   |              without our intervention.
 *         |                  |                   |
 * (3, 4) ===> Complex(3, 4) ===> java.lang.Long ===> scala.Long (compiled to Java's unboxed long)
 *    \             ^                    ^                ^
 *     \___step1___/                    /                /
 *      \ adrt(IntPairTupleSpec)       /                /
 *       \                            /                /
 *        \                          /                /
 *         \___step2________________/                /
 *          \ adrt(IntPairAsBoxedLong)              /
 *           \                                     /
 *            \                                   /
 *             \___step3_________________________/
 *               adrt(IntPairAsGaussianInteger)
 * }}}
 *
 */
object GreatestCommonDivisor {

  /** Greatest common divisor computation, no optimization. */
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

  /** Greatest common divisor computation, manual tuple specialization. */
  adrt(step1.IntPairAsGaussianInteger) {
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

  /** Greatest common divisor computation, encoding into a [[java.lang.Long]]. */
  adrt(step2.IntPairAsGaussianInteger) {
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

  /** Greatest common divisor computation, encoding as an unboxed [[scala.Long]]. */
  adrt(step3.IntPairAsGaussianInteger) {
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