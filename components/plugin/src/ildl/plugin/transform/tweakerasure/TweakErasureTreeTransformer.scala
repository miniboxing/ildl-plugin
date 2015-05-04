package ildl.plugin
package transform
package tweakerasure

import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.transform.TypingTransformers
import scala.reflect.internal.Phase
import ildl.plugin.infrastructure._
import ildl.plugin.metadata.ildlDefinitions

trait TweakErasureTreeTransformer extends TreeRewriters {
  self: TweakErasureComponent =>

  import global._
  import helper._
  import definitions._

  class TweakErasurePhase(prev: Phase) extends StdPhase(prev) {
    override def name = TweakErasureTreeTransformer.this.phaseName
    def apply(unit: CompilationUnit): Unit = {
      val transformer = new TweakErasureTransformer(unit)
      transformer.transformUnit(unit)
    }
  }

  object CallNobridgeMethod {
    def unapply(tree: Tree): Boolean = tree match {
      case Apply(method, args) if method.symbol.hasAnnotation(nobridgeClass) =>
             true
      // boxing/unboxing operations can be introduced by erasure:
      case Apply(Select(sel, nme.box), List(Apply(method, args))) if
           method.symbol.hasAnnotation(nobridgeClass) && ScalaValueClasses.contains(sel.symbol.companionClass) =>
             true
      case Apply(Select(sel, nme.unbox), List(Apply(method, args))) if
           method.symbol.hasAnnotation(nobridgeClass) && ScalaValueClasses.contains(sel.symbol.companionClass) =>
             true
      case _ =>
             false
    }
  }

  class TweakErasureTransformer(unit: CompilationUnit) extends TreeRewriter(unit) {
    override def rewrite(tree: Tree) = tree match {
      case dd @ DefDef(mods, name, _, _, _, CallNobridgeMethod()) if dd.symbol.isBridge =>
        Nil
      case _ =>
        Descend
    }
  }
}
