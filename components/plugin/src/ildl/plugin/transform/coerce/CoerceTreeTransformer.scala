//
//     _____   .__         .__ ___.                    .__ scala-miniboxing.org
//    /     \  |__|  ____  |__|\_ |__    ____  ___  ___|__|  ____     ____
//   /  \ /  \ |  | /    \ |  | | __ \  /  _ \ \  \/  /|  | /    \   / ___\
//  /    Y    \|  ||   |  \|  | | \_\ \(  <_> ) >    < |  ||   |  \ / /_/  >
//  \____|__  /|__||___|  /|__| |___  / \____/ /__/\_ \|__||___|  / \___  /
//          \/          \/          \/               \/         \/ /_____/
// Copyright (c) 2011-2015 Scala Team, École polytechnique fédérale de Lausanne
//
// Authors:
//    * Vlad Ureche
//    * Eugene Burmako
//
package ildl.plugin
package transform
package coerce

import scala.tools.nsc._
import scala.tools.nsc.typechecker._
import scala.tools.nsc.transform.TypingTransformers
import scala.util.DynamicVariable
import scala.collection.immutable.ListMap

trait CoerceTreeTransformer extends TypingTransformers {
  self: CoerceComponent =>

  import global._
  import helper._

  class CoercePhase(prev: StdPhase) extends StdPhase(prev) {
    override def name = CoerceTreeTransformer.this.phaseName
    override def checkable = false
    def apply(unit: CompilationUnit): Unit = {
      val tree = afterCoerce(new TreeAdapters().adapt(unit))
      tree.foreach(node => assert(node.tpe != null, node))
    }
  }

  class TreeAdapters extends Analyzer {
    var indent = 0
    override lazy val global: self.global.type = self.global

    def adapt(unit: CompilationUnit): Tree = {
      val context = rootContext(unit)
      // turnOffErrorReporting(this)(context)
      val checker = new TreeAdapter(context)
      unit.body = checker.typed(unit.body)
      unit.body
    }

    override def newTyper(context: Context): Typer =
      new TreeAdapter(context)

    def adaptdbg(ind: Int, msg: => String): Unit = {
//       println("  " * ind + msg)
    }

    class TreeAdapter(context0: Context) extends Typer(context0) {

      override val infer = new Inferencer {
        def context = TreeAdapter.this.context
        // As explained in #132, the inferencer can refer to private
        // members and we don't want to crash in the retyper due to
        // this => we just replace the check. :)
        override def checkAccessible(tree: Tree, sym: Symbol, pre: Type, site: Tree): Tree =
          tree.setSymbol(sym).setType(pre.memberType(sym))
      }

      override protected def finishMethodSynthesis(templ: Template, clazz: Symbol, context: Context): Template =
        templ

      def supertyped(tree: Tree, mode: Mode, pt: Type): Tree =
        super.typed(tree, mode, pt)

      override protected def adapt(tree: Tree, mode: Mode, pt: Type, original: Tree = EmptyTree): Tree = {
        val oldTpe = tree.tpe
        val newTpe = pt
        def superAdapt =
          if (oldTpe <:< newTpe)
            tree
          else
            tree.setType(newTpe)

        if (tree.isTerm) {
          if ((oldTpe.hasReprAnnot ^ newTpe.hasReprAnnot) && (!pt.isWildcard)) {
            val descObject = if (oldTpe.hasReprAnnot) oldTpe.getDescrObject else newTpe.getDescrObject
            val conversion = if (oldTpe.hasReprAnnot) oldTpe.getDescrReprToHigh else newTpe.getDescrHighToRepr
            val (tpe, descr) =
              if (oldTpe.hasReprAnnot)
                (oldTpe.dealiasWiden.withoutReprAnnot, oldTpe.getDescrObject)
              else
                (newTpe.dealiasWiden.withoutReprAnnot, newTpe.getDescrObject)
            val convCall = gen.mkAttributedSelect(gen.mkAttributedRef(descObject), conversion)
            val tree1 = gen.mkMethodCall(convCall, List(tree.withTypedAnnot))
            val tree2 = super.typed(tree1, mode, pt)
            assert(tree2.tpe != ErrorType, tree2)
            // super.adapt is automatically executed when calling super.typed
            tree2
          } else if (oldTpe.hasReprAnnot && (oldTpe.hasReprAnnot == newTpe.hasReprAnnot) && !(oldTpe <:< newTpe)) {
            val descr1 = oldTpe.getDescrObject
            val descr2 = newTpe.getDescrObject
            if (descr1 != descr2) {
              // representation mismatch
              val convCall1 = gen.mkAttributedSelect(gen.mkAttributedRef(oldTpe.getDescrObject), oldTpe.getDescrReprToHigh)
              val convCall2 = gen.mkAttributedSelect(gen.mkAttributedRef(newTpe.getDescrObject), newTpe.getDescrHighToRepr)
              val tree1 = gen.mkMethodCall(convCall2, gen.mkMethodCall(convCall1, List(tree.withTypedAnnot)) :: Nil)
              super.typed(tree1, mode, pt)
            } else {
              // workaround the isSubType issue with singleton types
              // and annotated types (see mb_erasure_torture10.scala)
              tree.setType(newTpe)
              tree
            }
          } else
            superAdapt
        } else {
          superAdapt
        }
      }

      case object AlreadyTyped
      implicit class WithAlreadyTyped(val tree: Tree) {
        def withTypedAnnot: Tree = tree.updateAttachment[AlreadyTyped.type](AlreadyTyped)
      }

      override def typed(tree: Tree, mode: Mode, pt: Type): Tree = {
        val ind = indent
        indent += 1
        adaptdbg(ind, " <== " + tree + " now: " + tree.tpe + "  expected: " + pt)

        if (tree.hasAttachment[AlreadyTyped.type] && (pt == WildcardType) && (tree.tpe != null))
          return tree

        val res = tree match {
          case EmptyTree | TypeTree() =>
            super.typed(tree, mode, pt)

          // Don't retype transformation description objects
          case tpl: ClassDef =>
            val isDescrObject = tpl.symbol.isTransfDescriptionObject
            if (isDescrObject)
              tpl
            else {
              tpl.setType(null)
              super.typed(tpl, mode, pt)
            }

          case Select(qual, meth) if qual.isTerm && tree.symbol.isMethod =>
            val qual2 = super.typedQualifier(qual.setType(null), mode, WildcardType).withTypedAnnot

            import helper._
            if (qual2.hasReprAnnot) {
              val tpe2 = if (qual2.tpe.hasAnnotation(reprClass)) qual2.tpe else qual2.tpe.widen
              val tpe3 = tpe2.removeAnnotation(reprClass)
              //val qual3 = super.typedQualifier(qual.setType(null), mode, tpe3)
              val descObject = qual2.tpe.getDescrObject
              val conversion = qual2.tpe.getDescrReprToHigh
              val convCall = gen.mkAttributedSelect(gen.mkAttributedRef(descObject), conversion)
              val qual3 =  gen.mkMethodCall(convCall, List(qual2))
              super.typed(Select(qual3, meth) setSymbol tree.symbol, mode, pt)
            } else {
              tree.setType(null)
              super.typed(tree, mode, pt)
            }

          case _ =>
            tree.setType(null)
            super.typed(tree, mode, pt)
        }

        // Stupid hack to get rid of an error when typing the <outer>
        // reference - the typer set the Outer.type as type instead of
        // ()Outer.type. There, I fixed it:
        if (tree.hasSymbolField && tree.symbol.name.decoded == "<outer>" && !tree.isInstanceOf[Apply])
          tree.tpe match {
            case MethodType(Nil, _) => // ok
            case _ => tree.setType(MethodType(Nil, tree.tpe))
          }

        adaptdbg(ind, " ==> " + res + ": " + res.tpe)
//        if (res.tpe == ErrorType)
//          adaptdbg(ind, "ERRORS: " + context.errBuffer)
        indent -= 1
        res
      }
    }
  }
}

