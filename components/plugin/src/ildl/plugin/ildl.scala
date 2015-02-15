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
import bridge._
import coerce._
import commit._

/** Metadata and definitions */
trait ildlHelperComponent extends
  ildlDefinitions with
  ildlMetadata with
  ildlAddons {
  val global: Global
}

/** Post-parser component, which marks values and methods in the `adrt` scope */
trait PostParserComponent extends
    PluginComponent
    with TreeRewriters
    with PostParserTreeTransformer {

  val helper: ildlHelperComponent { val global: PostParserComponent.this.global.type }
}

/** The component that injects @repr annotations */
trait InjectComponent extends
    PluginComponent
    with InjectTreeTransformer
    with InjectInfoTransformer {

  val helper: ildlHelperComponent { val global: InjectComponent.this.global.type }

  def injectPhase: StdPhase

  def afterInject[T](op: => T): T = global.exitingPhase(injectPhase)(op)
  def beforeInject[T](op: => T): T = global.enteringPhase(injectPhase)(op)
}

/** The component that introduces coercions */
trait BridgeComponent extends
    PluginComponent
    with BridgeTreeTransformer {

  val helper: ildlHelperComponent { val global: BridgeComponent.this.global.type }

  def bridgePhase: StdPhase

  def afterBridge[T](op: => T): T = global.exitingPhase(bridgePhase)(op)
  def afterCoerce[T](op: => T): T
  def beforeBridge[T](op: => T): T = global.enteringPhase(bridgePhase)(op)
  def beforeCoerce[T](op: => T): T
}

/** The component that introduces coercions */
trait CoerceComponent extends
    PluginComponent
    with ReprAnnotationCheckers
    with CoerceTreeTransformer {

  val helper: ildlHelperComponent { val global: CoerceComponent.this.global.type }

  def coercePhase: StdPhase

  def afterCoerce[T](op: => T): T = global.exitingPhase(coercePhase)(op)
  def beforeCoerce[T](op: => T): T = global.enteringPhase(coercePhase)(op)
}

/** The component that introduces coercions */
trait CommitComponent extends
    PluginComponent
    with CommitInfoTransformer
    with CommitTreeTransformer {

  val helper: ildlHelperComponent { val global: CommitComponent.this.global.type }

  def commitPhase: StdPhase

  def afterCommit[T](op: => T): T = global.exitingPhase(commitPhase)(op)
  def beforeCommit[T](op: => T): T = global.enteringPhase(commitPhase)(op)
}

class ildl(val global: Global) extends Plugin {
  import global._

  val name = "ildl"
  val description = "Transforms your code"

  var flag_passive = false

  lazy val components = List[PluginComponent](PostParserPhase, InjectPhase, BridgePhase, CoercePhase, CommitPhase)

  // LDL ftw!
  global.addAnnotationChecker(CoercePhase.ReprAnnotationChecker)

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

    var injectPhase : StdPhase = _
    override def newPhase(prev: scala.tools.nsc.Phase): StdPhase = {
      injectPhase = new Phase(prev)
      injectPhase
    }
  }

  private object BridgePhase extends {
    val helper = helperComponent
  } with BridgeComponent {
    val global: ildl.this.global.type = ildl.this.global
    val runsAfter = List("uncurry")
    override val runsRightAfter = Some("uncurry")
    val phaseName = "ildl-bridge"

    var bridgePhase : StdPhase = _
    override def newPhase(prev: scala.tools.nsc.Phase): StdPhase = {
      bridgePhase = new BridgePhase(prev.asInstanceOf[BridgePhase.StdPhase])
      bridgePhase
    }

    def afterCoerce[T](op: => T): T = global.exitingPhase(CoercePhase.coercePhase)(op)
    def beforeCoerce[T](op: => T): T = global.enteringPhase(CoercePhase.coercePhase)(op)
  }

  private object CoercePhase extends {
    val helper = helperComponent
  } with CoerceComponent {
    val global: ildl.this.global.type = ildl.this.global
    val runsAfter = List(BridgePhase.phaseName)
    override val runsRightAfter = Some(BridgePhase.phaseName)
    val phaseName = "ildl-coerce"

    var coercePhase : StdPhase = _
    override def newPhase(prev: scala.tools.nsc.Phase): StdPhase = {
      coercePhase = new CoercePhase(prev.asInstanceOf[CoercePhase.StdPhase])
      coercePhase
    }
  }

  private object CommitPhase extends {
    val helper = helperComponent
  } with CommitComponent {
    val global: ildl.this.global.type = ildl.this.global
    val runsAfter = List(CoercePhase.phaseName)
    override val runsRightAfter = Some(CoercePhase.phaseName)
    val phaseName = "ildl-commit"

    var commitPhase : StdPhase = _
    override def newPhase(prev: scala.tools.nsc.Phase): StdPhase = {
      commitPhase = new Phase(prev)
      commitPhase
    }
  }
}
