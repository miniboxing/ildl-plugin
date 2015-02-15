package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap

trait ildlAddons {
  this: ildlHelperComponent =>

  import global._
  import definitions._

  implicit class RichType(tpe: Type) {
    def hasReprAnnot: Boolean =
      tpe.hasAnnotation(reprClass)
    def withReprAnnot(descr: Tree): Type =
      tpe.withAnnotation(AnnotationInfo(reprClass.tpe, List(descr), Nil))
    def withoutReprAnnot(descr: Tree): Type =
      tpe.filterAnnotations(_.tpe =:= reprClass.tpe)

    def hasHighAnnot: Boolean =
      tpe.hasAnnotation(ildlHighClass)
    def withoutHighAnnot: Type =
      tpe.filterAnnotations(x => !(x.tpe =:= ildlHighClass.tpe))
  }

  // TODO: RichTree, RichSym
}
