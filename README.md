ildl-plugin
===========

This is the public mirror of the ildl-plugin development repository.
The paper describing the repository is deliberately hidden while it's under review.

**You might want to have a look [at the wiki](https://github.com/miniboxing/ildl-plugin/wiki) for more details**

See the tests in [`tests/correctness/resources/tests`](https://github.com/miniboxing/ildl-plugin/tree/master/tests/correctness/resources/tests):

```
$ sbt test
...
[info] Test run started
[info] Test ildl.infrastructure.TestSuite.testCompileOutput started
Picking plugin from: /mnt/data1/Work/Workspace/dev/ildl-plugin/tests/correctness/../../components/plugin/target/scala-2.11
Picking runtime from: /mnt/data1/Work/Workspace/dev/ildl-plugin/tests/correctness/../../components/plugin/target/scala-2.11
Picking tests from: /mnt/data1/Work/Workspace/dev/ildl-plugin/tests/correctness/resources/tests
Compiling benchmark-lazylist.scala                                     ... [ OK ]
Compiling bridges-1-repr.scala                                         ... [ OK ]
Compiling bridges-2-reprs.scala                                        ... [ OK ]
Compiling coerce-complex-1.scala                                       ... [ OK ]
Compiling coerce-complex-2.scala                                       ... [ OK ]
Compiling coerce-simple.scala                                          ... [ OK ]
Compiling error-high-1.scala                                           ... [ OK ]
Compiling error-high-2.scala                                           ... [ OK ]
Compiling error-high-3.scala                                           ... [ OK ]
Compiling extensions-basic-implicit.scala                              ... [ OK ]
Compiling extensions-basic.scala                                       ... [ OK ]
Compiling extensions-generic-implicit.scala                            ... [ OK ]
Compiling extensions-generic.scala                                     ... [ OK ]
Compiling feature-high.scala                                           ... [ OK ]
Compiling freestyle-1.scala                                            ... [ OK ]
Compiling freestyle-2.scala                                            ... [ OK ]
Compiling freestyle-3.scala                                            ... [ OK ]
Compiling inject-accessors-2.scala                                     ... [ OK ]
Compiling inject-accessors.scala                                       ... [ OK ]
Compiling scopes-collaborating.scala                                   ... [ OK ]
Compiling scopes-conflicting-cascade.scala                             ... [ OK ]
Compiling scopes-conflicting-repr.scala                                ... [ OK ]
Compiling scopes-conflicting-same-high.scala                           ... [ OK ]
Compiling scopes-nested.scala                                          ... [ OK ]
Compiling scopes-pickling.scala                                        ... [ OK ]

  25 tests ran, all good :)

[info] Test run finished: 0 failed, 0 ignored, 1 total, 10.464s
```

For example, in the [`scopes-collaborating.scala`](https://github.com/miniboxing/ildl-plugin/blob/master/tests/correctness/resources/tests/scopes-collaborating.scala) file we have:

```
  adrt(IntPairAsLong) {
    val n1 = (1, 0)
  }  

  adrt(IntPairAsFloat) {
    val n2 = n1
  }
```

and if you look at the [`scopes-collaborating.check`](https://github.com/miniboxing/ildl-plugin/blob/master/tests/correctness/resources/tests/scopes-collaborating.check) file you'll see:

```scala
    private[this] val n1: Long = IntPairAsLong.toRepr(new (Int, Int)(1, 0));
    <stable> <accessor> def n1(): Long = n1;
    private[this] val n2: Float = IntPairAsFloat.toRepr(IntPairAsLong.fromRepr(n1()));
    <stable> <accessor> def n2(): Float = n2
```

You can see the double coercion for the `n1()` value :)
