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
    } else
      tpe
  }

  def transformType(pos: Position, tpe: Type, descrs: List[Tree]): Type = {
    tpe match {
      case PolyType(targs, tpe) => PolyType(targs, transformType(pos, tpe, descrs))
      case MethodType(args, tpe) => MethodType(args, transformType(pos, tpe, descrs))
      case NullaryMethodType(tpe) => NullaryMethodType(transformType(pos, tpe, descrs))
      case _ if !tpe.hasAnnotation(reprClass) =>

        if (tpe.hasHighAnnot)
          global.reporter.error(pos, "The `adrt` scope cannot be used insde transformtion description objects.")

        var ntpe = tpe
        var done = false
        var wned = false
        var crtd = EmptyTree: Tree

        for (descr <- descrs.reverse) {
          if (matchesDescrHighType(descr.getDescrObject, tpe))
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
}


