package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap

trait ildlMetadata {
  this: ildlHelperComponent  =>

  import global._

  case class ildlAttachment(descrs: List[Position])

  object metadata {

    /** Mapping from description object positions to their actual trees */
    val descriptionObject = perRunCaches.newMap[Position, Tree]()

    /** Recording the transformation description objects visible for every symbol */
    val synbolDescriptionObjects = perRunCaches.newMap[Symbol, List[Tree]]()
  }
}
