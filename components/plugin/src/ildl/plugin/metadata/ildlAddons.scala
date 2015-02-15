package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap

trait ildlAddons {
  this: ildlHelperComponent =>

  import global._
  import definitions._

  implicit class RichType(tpe: Type) {

    // @repr annotation tools:
    def hasReprAnnot: Boolean =
      tpe.dealiasWiden.hasAnnotation(reprClass)
    def withReprAnnot(descr: Tree): Type = {
//      assert(descr.symbol != ildlTransformationDescrSym, descr)
      tpe.withAnnotation(AnnotationInfo(reprClass.tpe, List(descr), Nil))
    }
    def withReprAnnot(descr: Symbol): Type = {
//      assert(descr != ildlTransformationDescrSym, descr)
//      assert(descr.isModuleClass)
      tpe.withAnnotation(AnnotationInfo(reprClass.tpe, List(gen.mkAttributedRef(descr)), Nil))
    }
    def withoutReprAnnot: Type =
      tpe.filterAnnotations(_.tpe =:= reprClass.tpe)

    def getDescrObject: Symbol = tpe.dealiasWiden.annotations.filter(_.tpe.typeSymbol == reprClass) match {
      case Nil         => assert(false, "Internal error: No @repr annotation detected."); ???
      case List(annot) =>
        val sym = annot.argAtIndex(0).get.symbol
//        assert(sym != ildlTransformationDescrSym, annot.argAtIndex(0))
        if (sym == ildlTransformationDescrSym)
          sym
        else if (sym.isModule)
          sym
        else
          sym.sourceModule
      case _           => assert(false, "Internal error: Multiple @repr annotations detected."); ???
    }
    def getDescrHighToRepr: Symbol = tpe.getDescrObject.tpe.member(highToReprName).filter(!_.isDeferred)
    def getDescrReprToHigh: Symbol = tpe.getDescrObject.tpe.member(reprToHighName).filter(!_.isDeferred)

    def withoutReprDeep: Type = (new TypeMap {
      def apply(tpe: Type): Type = mapOver(tpe)
      override def mapOver(tpe: Type): Type = tpe match {
        case ann: AnnotatedType if ann.hasAnnotation(reprClass) =>
          tpe.filterAnnotations(ann => !(ann.tpe =:= reprClass.tpe))
        case _ =>
          super.mapOver(tpe)
      }}).apply(tpe)


    // @high annotation tools:
    def hasHighAnnot: Boolean =
      tpe.hasAnnotation(ildlHighClass)
    def withoutHighAnnot: Type =
      tpe.filterAnnotations(x => !(x.tpe =:= ildlHighClass.tpe))
  }

  implicit class RichSym(sym: Symbol) {
    def isTransfDescriptionObject: Boolean =
      sym.isModuleOrModuleClass && (sym.tpe <:< ildlTransformationDescrSym.tpe) ||
      sym == ildlTransformationDescrSym
  }

  implicit class RichTree(tree: Tree) {
    def hasReprAnnot: Boolean =
      tree.tpe.hasReprAnnot

  }
}
