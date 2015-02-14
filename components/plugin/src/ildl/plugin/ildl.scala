package ildl.plugin

import scala.tools.nsc.Global
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins._
import scala.tools.nsc.transform._
import scala.tools.nsc.settings.ScalaVersion
import infrastructure._
import metadata._
import transform._
import postParser._
import inject._

/** Metadata and definitions */
trait ildlHelperComponent extends
  ildlDefinitions with
  ildlMetadata {
  val global: Global
}

/** Post-parser component, which marks values and methods in the `adrt` scope */
trait PostParserComponent extends
    PluginComponent
    with TreeRewriters
    with PostParserTreeTransformer {

  val helper: ildlHelperComponent { val global: PostParserComponent.this.global.type }
}

/** The component that inject @repr annotations */
trait InjectComponent extends
    PluginComponent
    with InjectTreeTransformer
    with InjectInfoTransformer {

  val helper: ildlHelperComponent { val global: InjectComponent.this.global.type }

  def injectPhase: StdPhase

  def afterInject[T](op: => T): T = global.enteringPhase(injectPhase)(op)
  def beforeInject[T](op: => T): T = global.exitingPhase(injectPhase)(op)
}

class ildl(val global: Global) extends Plugin {
  import global._

  val name = "ildl"
  val description = "Transforms your code"

  var flag_passive = false

  lazy val components = List[PluginComponent](PostParserPhase, InjectPhase)

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

  private object helperComponent extends ildlHelperComponent {
    val global: ildl.this.global.type = ildl.this.global

    def flag_passive = ildl.this.flag_passive
  }

  private object PostParserPhase extends {
    val helper = helperComponent
  } with PostParserComponent {
    val global: ildl.this.global.type = ildl.this.global
    val runsAfter = List("parser")
    override val runsRightAfter = Some("parser")
    val phaseName = "ildl-postparser"
  }

  private object InjectPhase extends {
    val helper = helperComponent
  } with InjectComponent {
    val global: ildl.this.global.type = ildl.this.global
    val runsAfter = List("typer")
    override val runsRightAfter = Some("typer")
    val phaseName = "ildl-inject"

    def flag_passive = ildl.this.flag_passive

    var injectPhase : StdPhase = _
    override def newPhase(prev: scala.tools.nsc.Phase): StdPhase = {
      injectPhase = new Phase(prev)
      injectPhase
    }
  }
}
