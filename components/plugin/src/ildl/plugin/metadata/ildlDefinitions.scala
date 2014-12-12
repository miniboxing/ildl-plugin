package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap

trait ildlDefinitions {
  this: PluginComponent =>

  import global._
  import definitions._

}
