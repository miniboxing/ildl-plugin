package ildl
package benchmark

object Platform {

  def checkCompatibility() = {
    // x86 architecture
    assert(sys.props.getOrElse("os.arch", "") == "amd64" ||
           sys.props.getOrElse("os.arch", "") == "i386", "Unsupported architecture:" + sys.props("os.arch"))

    if (sys.props.getOrElse("os.arch", "") == "i386") {
      println()
      println()
      println("WARNING!")
      println("The benchmarks may be affected by the fact that we are running on a 32-bit operating system!")
      println()
      println()
    }

    // Java version
    assert(sys.props.getOrElse("java.version", "").startsWith("1.7"), "Incorrect Java version:" + sys.props("java.version"))

    // Disallow environment setup
    assert(!sys.env.isDefinedAt("_JAVA_OPTIONS"), "You should disable _JAVA_OPTIONS before running the benchmarks!")
    assert(!sys.env.isDefinedAt("JAVA_OPTS"),     "You should disable JAVA_OPTS before running the benchmarks!")
  }
}