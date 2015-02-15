package ildl.plugin
package transform
package inject

import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.Phase
import infrastructure.TreeRewriters
import scala.tools.nsc.transform.InfoTransform

trait InjectInfoTransformer extends InfoTransform {
  self: InjectComponent =>

  import global._
  import helper._

  override def transformInfo(sym: Symbol, tpe: Type): Type = {

    // make sure description objects do not nest
    if (currentRun.compiles(sym) && sym.isTransfDescriptionObject) {
      val enclosing = sym.ownerChain.find(s => (s != sym) && (s.isTransfDescriptionObject))
      if (enclosing.isDefined) {
        global.reporter.error(sym.pos, s"The ${sym.name} transformation description object is nested inside the " +
                                       s"${enclosing.get.name} transformation description object, a construction which " +
                                       s"is illegal (al least for now).")
      }
    }

    // types inside the `adrt` scope get annotated:
    if (metadata.synbolDescriptionObjects.isDefinedAt(sym)) {
      val descrs = metadata.synbolDescriptionObjects(sym)
      transformType(NoPosition, tpe, descrs)
    } else if (tpe.finalResultType.hasHighAnnot) {
      // Match exterior description object
      val enclosingDescr = sym.ownerChain.find(s => s.isTransfDescriptionObject)
      enclosingDescr match {
        case None =>
          global.reporter.error(sym.pos, s"The ${sym} contains the @high annotation despite not being enclosed in a " +
                                         s"transformation description object. This is an invalid use of the @high " +
                                         s"annotation.")
          tpe.withoutHighAnnot
        case Some(descr) =>
          transformHighAnnotation(sym, tpe, gen.mkAttributedRef(descr))
      }
    } else
      tpe
  }

  def transformType(pos: Position, tpe: Type, descrs: List[Tree]): Type = {
    tpe match {
      case PolyType(targs, tpe) => PolyType(targs, transformType(pos, tpe, descrs))
      case MethodType(args, tpe) => MethodType(args, transformType(pos, tpe, descrs))
      case _ if !tpe.hasAnnotation(reprClass) =>
        var ntpe = tpe
        var done = false
        var wned = false
        var crtd = EmptyTree: Tree

        for (descr <- descrs) {
          val highTpe = descr.tpe.member(TypeName("High")).alternatives.head.tpe
          val reprTpe = descr.tpe.member(TypeName("Repr")).alternatives.head.tpe
          if (tpe =:= highTpe)
            if (!done) {
              done = true
              ntpe = tpe.withReprAnnot(descr)
              crtd = descr
            } else {
              if (!wned && (pos != NoPosition)) {
                wned = true
                global.reporter.warning(pos, "Several `adrt` scopes can be applied here. The innermost will apply: " + crtd)
              }
            }
        }
        ntpe
      case _ if tpe.hasAnnotation(reprClass) =>
        global.reporter.error(pos, s"Unexpected annotation on type $tpe. This signals an internal error.")
        tpe
    }
  }

  def transformHighAnnotation(sym: Symbol, tpe: Type, descr: Tree): Type = {
    tpe match {
      case PolyType(targs, tpe) => PolyType(targs, transformHighAnnotation(sym, tpe, descr))
      case MethodType(args, tpe) => MethodType(args, transformHighAnnotation(sym, tpe, descr))
      case _ if tpe.hasHighAnnot =>
        val highTpe = descr.tpe.member(TypeName("High")).alternatives.head.tpe
        val reprTpe = descr.tpe.member(TypeName("Repr")).alternatives.head.tpe
        if (tpe.withoutHighAnnot =:= reprTpe) {
          highTpe.withReprAnnot(descr)
        } else {
          global.reporter.error(sym.pos, s"The ${descr.symbol.name} transformation description object contains a definition " +
                                         s"error: The @high annotation in $sym's type is applied to something that is " +
                                         s"not the representation type (which is $reprTpe). This is an error in the" +
                                         s"transformation description object definition.")
          tpe.withoutHighAnnot
        }
      case _ =>
        tpe
    }

  }
}


