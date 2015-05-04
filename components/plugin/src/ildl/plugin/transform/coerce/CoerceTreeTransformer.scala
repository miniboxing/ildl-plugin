// NOTE: This file was adapted form the miniboxing repository:
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


    object MaybeImplicit {
      abstract sealed trait ImplicitMetadata
      case class NonEmptyMetadata(method: Tree, targs: List[Type]) extends ImplicitMetadata
      case object EmptyMetadata extends ImplicitMetadata

      def unapply(tree: Tree): Option[(Tree, ImplicitMetadata, String)] = tree match {
        case Apply(impl, List(qual))                   if impl.symbol.isImplicit => Some((qual, NonEmptyMetadata(impl, Nil), "implicit_" + impl.symbol.name.decode))
        case Apply(TypeApply(impl, targs), List(qual)) if impl.symbol.isImplicit => Some((qual, NonEmptyMetadata(impl, targs.map(_.tpe)), "implicit_" + impl.symbol.name.decode))
        case _ => Some((tree, EmptyMetadata, "extension"))
      }

      def apply(qual: Tree, meta: ImplicitMetadata) = meta match {
        case meta: NonEmptyMetadata => gen.mkMethodCall(meta.method, /* meta.targs, */ List(qual))
        case EmptyMetadata => qual
      }
    }

    object FullApply {
      def unapply(tree: Tree): Option[(Tree, List[Tree], List[Tree])] =
        tree match {
          case Apply(sel@Select(_, _), args)                   if !sel.symbol.isImplicit => Some((sel, Nil, args))
          case Apply(TypeApply(sel@Select(_, _), targs), args) if !sel.symbol.isImplicit => Some((sel, targs, args))
          case _ => None
        }
      def apply(qual: Tree, targs: List[Tree], args: List[Tree]): Tree =
        if (targs.isEmpty)
          Apply(qual, args)
        else
          Apply(TypeApply(qual, targs), args)
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
            super.adapt(tree, mode, pt, original)

        if ((tree.tpe.isInstanceOf[PolyType]) && (mode.inFunMode))
          super.adapt(tree, mode, pt, original)
        else if (tree.isTerm) {
          if ((oldTpe.hasReprAnnot ^ newTpe.hasReprAnnot) && (!pt.isWildcard)) {
            val descObject = if (oldTpe.hasReprAnnot) oldTpe.getAnnotDescrObject else newTpe.getAnnotDescrObject
            val conversion = if (oldTpe.hasReprAnnot) oldTpe.getAnnotDescrReprToHigh else newTpe.getAnnotDescrHighToRepr
            val (tpe, descr) =
              if (oldTpe.hasReprAnnot)
                (oldTpe.dealiasWiden.withoutReprAnnot, oldTpe.getAnnotDescrObject)
              else
                (newTpe.dealiasWiden.withoutReprAnnot, newTpe.getAnnotDescrObject)
            val convCall = gen.mkAttributedSelect(gen.mkAttributedRef(descObject), conversion)
            val tree1 = gen.mkMethodCall(convCall, List(tree.withTypedAnnot))
            val tree2 = super.typed(tree1, mode, pt)
            //assert(tree2.tpe != ErrorType, tree2)
            // super.adapt is automatically executed when calling super.typed
            tree2
          } else if (oldTpe.hasReprAnnot && (oldTpe.hasReprAnnot == newTpe.hasReprAnnot) && !(oldTpe <:< newTpe)) {
            val descr1 = oldTpe.getAnnotDescrObject
            val descr2 = newTpe.getAnnotDescrObject
            if (descr1 != descr2) {
              // representation mismatch -- TODO: WARN HERE
              val convCall1 = gen.mkAttributedSelect(gen.mkAttributedRef(oldTpe.getAnnotDescrObject), oldTpe.getAnnotDescrReprToHigh)
              val convCall2 = gen.mkAttributedSelect(gen.mkAttributedRef(newTpe.getAnnotDescrObject), newTpe.getAnnotDescrHighToRepr)
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

      def typechecks(candidate: Symbol, descObject: Symbol, tree: Tree, qual2: Tree, targs: List[Tree], args: List[Tree], mode: Mode, pt: Type): Boolean = {
        val newQual = gen.mkAttributedRef(descObject)
        val extMeth = gen.mkAttributedSelect(newQual, candidate)
        val candTree = FullApply(extMeth, Nil, qual2.duplicate :: args.map(_.duplicate))

        // cleaned up typer
        val unit = global.currentUnit
        val contextZ = rootContext(unit, throwing = false, checking = false)
        contextZ.implicitsEnabled = false
        contextZ.macrosEnabled = false
        contextZ.enrichmentEnabled = false
        val localTyper = newTyper(contextZ)

        val result: Boolean =
          localTyper.silent(_.typed(candTree, mode, pt), reportAmbiguousErrors = false) match {
            case SilentResultValue(t: Tree) => t.tpe.withoutReprAnnotAggresive <:< pt.withoutReprAnnotAggresive
            case SilentTypeError(err) => false
          }

        result
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

          case FullApply(sel@Select(MaybeImplicit(qual, implData, prefix), meth), targs, args) if qual.isTerm && tree.symbol.isMethod =>
            val qual2 = super.typedQualifier(qual.setType(null), mode, WildcardType).withTypedAnnot

            import helper._
            val global = CoerceTreeTransformer.this.global
            if (qual2.hasReprAnnot) {
              val tpe2 = if (qual2.tpe.hasAnnotation(reprClass)) qual2.tpe else qual2.tpe.widen
              val tpe3 = tpe2.removeAnnotation(reprClass)
              val descObject = qual2.tpe.getAnnotDescrObject

              val extName = TermName(prefix + "_" + meth)
              val publicCandidates = descObject.info.member(extName).alternatives.filter(mb => mb.isPublic && !mb.isDeferred)
              val matchingCandidates = publicCandidates.filter(typechecks(_, descObject, tree, qual2, Nil, args, mode, pt))

              matchingCandidates match {
                case List(candidate) =>
                  val newQual = gen.mkAttributedRef(descObject)
                  val extMeth = gen.mkAttributedSelect(newQual, candidate)
                  super.typed(FullApply(extMeth, Nil, qual2 :: args), mode, pt)
                case _ =>
                  CoerceTreeTransformer.this.global.reporter.warning(tree.pos,
                    "The " + sel.symbol + " can be optimized if you define a public, non-overloaded " +
                    "and matching exension method for it in " + descObject + ", with the name " + extName.decoded +
                    (if (matchingCandidates.isEmpty) "." else " (the method is overloaded).\n"))
                  val conversion = qual2.tpe.getAnnotDescrReprToHigh
                  val convCall = gen.mkAttributedSelect(gen.mkAttributedRef(descObject), conversion)
                  val qual3 =  gen.mkMethodCall(convCall, List(qual2))
                  super.typed(FullApply(Select(MaybeImplicit(qual3, implData), meth) setSymbol tree.symbol, targs, args).withTypedAnnot, mode, pt)
              }
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

