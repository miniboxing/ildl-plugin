package ildl.plugin
package transform
package bridge

import scala.tools.nsc.transform.InfoTransform

trait BridgeInfoTransformer extends InfoTransform {
  self: BridgeComponent =>

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

    if (tpe.finalResultType.hasHighAnnot) {
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

  def transformHighAnnotation(sym: Symbol, tpe: Type, descr: Tree): Type = {
    tpe match {
      case PolyType(targs, tpe) => PolyType(targs, transformHighAnnotation(sym, tpe, descr))
      case MethodType(args, tpe) => MethodType(args, transformHighAnnotation(sym, tpe, descr))
      case NullaryMethodType(tpe) => NullaryMethodType(transformHighAnnotation(sym, tpe, descr))
      case _ if tpe.hasHighAnnot =>

        val highTpe = getDescrHighType(descr.getDescrObject, tpe.withoutHighAnnot)
        if (highTpe != ErrorType) {
          highTpe.withReprAnnot(descr)
        } else {
          global.reporter.error(sym.pos, s"The ${descr.symbol.name} transformation description object contains a " +
                                         s"definition error: The @high annotation in $sym's type is applied to " +
                                         s"something that does not match the representation type. This is an error " +
                                         s"in the transformation description object definition.")
          tpe.withoutHighAnnot
        }

      case _ =>
        tpe
    }

  }
}


