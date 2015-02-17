package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap
import scala.reflect.internal.util.SourceFile

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
      tpe.filterAnnotations(ann => !(ann.tpe =:= reprClass.tpe))

    def getAnnotDescrObject: Symbol = tpe.dealiasWiden.annotations.filter(_.tpe.typeSymbol == reprClass) match {
      case Nil         => assert(false, "Internal error: No @repr annotation detected."); ???
      case List(annot) => annot.argAtIndex(0).get.getDescrObject
      case _           => assert(false, "Internal error: Multiple @repr annotations detected."); ???
   }
    def getAnnotDescrHighToRepr: Symbol = tpe.getAnnotDescrObject.getDescrHighToRepr
    def getAnnotDescrReprToHigh: Symbol = tpe.getAnnotDescrObject.getDescrReprToHigh
    def getAnnotDescrHighTpe: Type = tpe.getAnnotDescrObject.getDescrHighTpe
    def getAnnotDescrReprTpe: Type = tpe.getAnnotDescrObject.getDescrReprTpe

    // @high annotation tools:
    def hasHighAnnot: Boolean =
      tpe.hasAnnotation(ildlHighClass)
    def withoutHighAnnot: Type =
      tpe.filterAnnotations(x => !(x.tpe =:= ildlHighClass.tpe))
  }

  implicit class RichSym(sym: Symbol) {
    def isTransfDescriptionObject: Boolean =
      sym.isModuleOrModuleClass && (sym.tpe <:< ildlTransformationDescrSym.tpe) ||
      sym == ildlTransformationDescrSym ||
      sym == ildlRigidTransformationDescrSym ||
      sym == ildlFreestyleTransformationDescrSym

    def getTransfType: TransformationType = {
      assert(isTransfDescriptionObject, sym)
      if (sym.tpe <:< ildlRigidTransformationDescrSym.tpe)
        Rigid
      else if (sym.tpe <:< ildlFreestyleTransformationDescrSym.tpe)
        Freestyle
      else
        ???
    }
    def getDescrHighToRepr: Symbol =
      sym.tpe.member(highToReprName).filter(!_.isDeferred)
    def getDescrReprToHigh: Symbol =
      sym.tpe.member(reprToHighName).filter(!_.isDeferred)
    def getDescrHighTpe: Type = {
      val res = sym.tpe.member(highTpeName).tpe.dealias
      assert(res != NoType, sym.tpe.member(highTpeName) + "  " + sym)
      res
    }
    def getDescrReprTpe: Type = {
      val res = sym.tpe.member(reprTpeName).tpe.dealias
      assert(res != NoType, sym.tpe.member(reprTpeName) + "  " + sym)
      res
    }
  }

  implicit class RichTree(tree: Tree) {
    def hasReprAnnot: Boolean =
      tree.tpe.hasReprAnnot
    def getTransfType: TransformationType =
      tree.symbol.getTransfType
    def getDescrHighTpe: Type =
      getDescrObject.getDescrHighTpe
    def getDescrReprTpe: Type =
      getDescrObject.getDescrReprTpe
    def getDescrObject: Symbol =
      nomalizeDescriptorSymbol(tree.symbol)
  }

  def nomalizeDescriptorSymbol(sym: Symbol) = {
    assert(sym != NoSymbol)
    if ((sym == ildlTransformationDescrSym) || (sym == ildlRigidTransformationDescrSym) || (sym == ildlFreestyleTransformationDescrSym))
      sym
    else if (sym.isModule)
      sym
    else
      sym.sourceModule
  }

  def matchesDescrHighType(descr: Symbol, high: Type) =
    getDescrReprType(descr, high) != ErrorType

  def matchesDescrReprType(descr: Symbol, repr: Type) =
    getDescrHighType(descr, repr) != ErrorType

  def getDescrHighType(descr: Symbol, repr: Type): Type = getDescrMatchingType(descr, repr, isHigh = false)
  def getDescrReprType(descr: Symbol, high: Type): Type = getDescrMatchingType(descr, high, isHigh = true)

  def getDescrMatchingType(descr: Symbol, tpe: Type, isHigh: Boolean): Type =
    descr.getTransfType match {

    case Rigid =>
        val highTpe = descr.getDescrHighTpe
        val reprTpe = descr.getDescrReprTpe
        val (cpTpe, resTpe) =
          if (isHigh)
            (highTpe, reprTpe)
          else
            (reprTpe, highTpe)
        if (tpe =:= cpTpe) {
          resTpe
        } else
          ErrorType

      case Freestyle =>
        val coercion =
          if (isHigh)
            descr.getDescrHighToRepr
          else
            descr.getDescrReprToHigh

        val unit = global.currentUnit
        val context = global.analyzer.rootContext(unit, throwing = false, checking = false)
        context.implicitsEnabled = false
        context.macrosEnabled = false
        context.enrichmentEnabled = false

        val coercionTree = Ident("<coercion>").setType(global.enteringPhase(ildlInjectPhase)(coercion.tpe))
        val appliedTree  = Apply(coercionTree, List(Ident("<argument>") setType tpe))
        val localTyper = global.analyzer.newTyper(context)
        val result: Type = {
          localTyper.silent(_.typed(appliedTree), reportAmbiguousErrors = false) match {
            case global.analyzer.SilentResultValue(t: Tree) => t.tpe
            case global.analyzer.SilentTypeError(err) =>
              ErrorType
          }
        }

//        println(descr + " with: " + tpe + " isHigh= " + isHigh + "  ==> " + result)

        result
    }
}
