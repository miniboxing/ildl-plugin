package ildl.plugin

import scala.tools.nsc.Global
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins._
import scala.tools.nsc.transform._
import scala.tools.nsc.settings.ScalaVersion

class ildl(val global: Global) extends Plugin {
  import global._

  val name = "ildl"
  val description = "Transforms your code"

  var flag_passive = false

  lazy val components = List[PluginComponent]()

  override def processOptions(options: List[String], error: String => Unit) {
    for (option <- options) {
      option.toLowerCase() match {
        case "passive" => // do not honor annotations => do not transform the code
          flag_passive = true
        case _ =>
          error("ildl: Option not understood: " + option)
      }
    }
  }
}
