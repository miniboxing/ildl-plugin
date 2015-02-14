package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap

trait ildlMetadata {
  this: ildlHelperComponent  =>

  import global._

  case class ildlAttachment(tree: Tree)

  object metadata {
    val descriptionObject = perRunCaches.newMap[Tree, Tree]()
  }
}
