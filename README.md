#ildl-plugin
<img src="http://scala-miniboxing.org/images/ildl-logo.png" alt="ildl logo" width="150" align="right">

[![Build Status](https://travis-ci.org/miniboxing/ildl-plugin.svg)](https://travis-ci.org/miniboxing/ildl-plugin?branch=master)

This plugin enables data-centric metaprogramming, allowing library developers to implement domain-specific optimizations for their libraries. 
We used it in four sample transformations:
 * [avoiding heap allocation](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Data-Encoding)
 * [transforming collections of data](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Efficient-Collections)
 * [deforestation and retrofitting specialization](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Deforestation)
 * [array of struct to struct of array transformations](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Array-of-Struct)

If you're interested in:
 * trying it out, check out [the `Introduction` page of the wiki](https://github.com/miniboxing/ildl-plugin/wiki/Tutorial-~-Introduction);
 * understanding the theory, check out [this paper](http://infoscience.epfl.ch/record/207050?ln=en).

Thanks and enjoy!
