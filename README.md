#ildl-plugin

[![Join the chat at https://gitter.im/miniboxing/ildl-plugin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/miniboxing/ildl-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
<img src="http://scala-miniboxing.org/images/ildl-logo.png" alt="ildl logo" width="150" align="right">

[![Build Status](https://travis-ci.org/miniboxing/ildl-plugin.svg)](https://travis-ci.org/miniboxing/ildl-plugin?branch=master)

The ildl-plugin is a meta-programming technique aimed at allowing safe, custom transformations across library boundaries. Using ildl-based transformations, we were able to obtain speedups in excess of 20x and have optimized code across a wide range of use-cases:

 * [avoiding heap allocation](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Data-Encoding)
 * [transforming collections of data](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Efficient-Collections)
 * [deforestation and retrofitting specialization](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Deforestation)
 * [array of struct to struct of array transformations](https://github.com/miniboxing/ildl-plugin/wiki/Sample-~-Array-of-Struct)

If you're interested in:
 * trying it out, check out [the `Introduction` page of the wiki](https://github.com/miniboxing/ildl-plugin/wiki/Tutorial-~-Introduction);
 * understanding the theory, check out [this paper](http://infoscience.epfl.ch/record/207050?ln=en).

Thanks and enjoy!
