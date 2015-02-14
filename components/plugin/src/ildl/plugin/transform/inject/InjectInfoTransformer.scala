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
      case _ =>
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
              ntpe = tpe.withAnnotation(AnnotationInfo(reprClass.tpe, List(descr), Nil))
              crtd = descr
            } else {
              if (!wned && (pos != NoPosition)) {
                wned = true
                global.reporter.warning(pos, "Several `adrt` scopes can be applied here. The innermost will apply: " + crtd)
              }
            }
        }
        ntpe
    }
  }
}


