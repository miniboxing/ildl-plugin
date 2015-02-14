package ildl.plugin

import scala.tools.nsc.Global
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins._
import scala.tools.nsc.transform._
import scala.tools.nsc.settings.ScalaVersion

import infrastructure._
import transform._
import postParser._

trait PostParserComponent extends
    PluginComponent
    with TreeRewriters
    with PostParserTreeTransformer {

  def flag_passive: Boolean
}

class ILDL(val global: Global) extends Plugin {
  import global._

  val name = "ildl"
  val description = "Transforms your code"

  var flag_passive = false

  lazy val components = List[PluginComponent](PostParserPhase)

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

  private object PostParserPhase extends PostParserComponent {
    val global: ILDL.this.global.type = ILDL.this.global
    val runsAfter = List("parser")
    override val runsRightAfter = Some("parser")
    val phaseName = "ildl-postparser"

    def flag_passive = ILDL.this.flag_passive
  }


}
