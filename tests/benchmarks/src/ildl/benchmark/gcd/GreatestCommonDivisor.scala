package ildl
package benchmark
package gcd

/**
 * The actual benchmark. The current benchmark is the Gaussian integer greatest common
 * divisor, which is explained on
 * [[http://math.stackexchange.com/questions/82350/gcd-of-gaussian-integers]].
 *
 * Several factors influence the overall speedup. In the following diagram, we added
 * intermediate steps to the transformation, in order to isolate the individual factors
 * influencing the overall speedup.
 *
 * The diagram follows the transformation of the main data type in the program: the pair
 * of integers. The top part explains the transformation, the middle shows the updated
 * types and the bottom part shows the exact transformation description objects used for
 * the `adrt` scopes:
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
 *     \___________/                    /                /
 *      \ step1.IntPairAsGaussianInt.../                /
 *       \                            /                /
 *        \                          /                /
 *         \________________________/                /
 *          \ step2.IntPairAsGaussianInteger        /
 *           \                                     /
 *            \                                   /
 *             \_________________________________/
 *               step3.IntPairAsGaussianInteger
 * }}}
 *
 * These are the numbers we obtain for the benchmarks:
 *
 *    +--------------------------------------------+--------------+---------+
 *    | Benchmark                                  | Running Time | Speedup |
 *    +--------------------------------------------+--------------+---------|
 *    | 10000 GCD runs, original code              |      28.1 ms |    none |
 *    | 10000 GCD runs, step 1 transformation      |      12.5 ms |    2.2x |
 *    | 10000 GCD runs, step 2 transformation      |      15.0 ms |    1.9x |
 *    | 10000 GCD runs, step 3 transformation      |       2.2 ms |   12.7x |
 *    +--------------------------------------------+--------------+---------+
 *
 * These numbers are roughly the same as the ones reported in the paper.
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