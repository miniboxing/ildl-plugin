ildl-plugin
===========

Vision:

```scala

  import FloatPairAsComplexAPI._

  withAdhocRepresentation(FloatPairToDouble) {
    val x  = (0f, 1f)
    val xx = x * x
    println(xx) 
  }
```

should produce:

```scala
  val x: Long = newFloatPair(0f, 1f)
  val xx: Long = operator_*(x, x)
  println(longToFloatPair(xx))
```

And should be able to handle classes, methods, pretty much any Scala expression (`Expr`). That's where I hope we'll get, and I trust it's doable after seeing how reliable the miniboxing function transformation is in practice (http://scala-miniboxing.org/example_functions.html).
