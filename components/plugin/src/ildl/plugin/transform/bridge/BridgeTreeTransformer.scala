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
//
package ildl.plugin
package transform
package bridge

import scala.tools.nsc.transform.TypingTransformers
import infrastructure.TreeRewriters

trait BridgeTreeTransformer extends TreeRewriters {
  self: BridgeComponent =>

  import global._
  import helper._

  class BridgePhase(prev: StdPhase) extends StdPhase(prev) {
    override def name = BridgeTreeTransformer.this.phaseName
    override def checkable = true
    def apply(unit: CompilationUnit): Unit = {
      afterBridge(new BridgeTransformer(unit).transformUnit(unit))
    }
  }

  class BridgeTransformer(unit: CompilationUnit) extends TreeRewriter(unit) {

    import global._
    import definitions.BridgeClass

    protected def rewrite(tree: Tree): Result = {
      tree match {
        case defdef: DefDef =>

          val sameResultEncoding = (reference: Symbol) => (s: Symbol) => {
            val res1 = s.tpe.finalResultType.hasReprAnnot == reference.info.finalResultType.hasReprAnnot
            val res1a = res1 && !(s.tpe.finalResultType.hasReprAnnot)
            val res2 = res1 && (s.tpe.finalResultType.hasReprAnnot)
            val res2a = res2 && (s.tpe.finalResultType.getAnnotDescrObject == reference.info.finalResultType.getAnnotDescrObject)

            res1a || res2a
          }

          val preOverrides = beforeCoerce(defdef.symbol.allOverriddenSymbols).flatMap(_.alternatives)
          val postOverrides = afterCoerce(defdef.symbol.allOverriddenSymbols).flatMap(_.alternatives).filter(sameResultEncoding(defdef.symbol))

          val bridgeSyms = preOverrides.filterNot(postOverrides.contains)

          def filterBridges(bridges: List[Symbol]): List[Symbol] = bridges match {
            case Nil => Nil
            case sym :: tail =>
              val overs = afterCoerce(sym.allOverriddenSymbols).flatMap(_.alternatives).filter(sameResultEncoding(sym))
              val others = tail filterNot (overs.contains)
              sym :: filterBridges(others)
          }
          val bridgeSymsFiltered = filterBridges(bridgeSyms)

          var warned = false

          val bridges: List[Tree] =
            for (sym <- bridgeSymsFiltered) yield {
              val local = defdef.symbol
              val decls = local.owner.info.decls

              // bridge symbol:
              val bridge = local.cloneSymbol

              bridge.setInfo(local.owner.info.memberInfo(sym).cloneInfo(bridge))
              bridge.addAnnotation(BridgeClass)
              if (decls != EmptyScope) decls enter bridge

              // TODO: Restore the warning when going across representations!
              val tpes1 =  local.tpe.finalResultType ::  local.tpe.params.map(_.tpe)
              val tpes2 = bridge.tpe.finalResultType :: bridge.tpe.params.map(_.tpe)
              for ((tpe1, tpe2) <- tpes1 zip tpes2) {
                if (!warned && tpe1.hasReprAnnot && tpe2.hasReprAnnot && (tpe1.getAnnotDescrObject != tpe2.getAnnotDescrObject)) {
                  global.reporter.warning(local.pos, "The " + local + " in " + local.owner + " (transitively) " +
                                                     "overrides " + sym + " in " + sym.owner + ". This requires " +
                                                     "creating a bridge method, which is okay. But calling this bridge " +
                                                     "method goes through two data representation conversions, making " +
                                                     "it very slow. So please make sure objects of type " + local.owner.name +
                                                     " are not passed as objects of type " + sym.owner.name + ", otherwise " +
                                                     "you will experience significant slowdowns.")
                }
              }

              // bridge tree:
              val bridgeRhs0 = gen.mkMethodCall(gen.mkAttributedRef(local), bridge.typeParams.map(_.tpeHK), bridge.info.params.map(Ident))
              val bridgeRhs1 = atOwner(bridge)(localTyper.typed(bridgeRhs0))
              val bridgeDef = newDefDef(bridge, bridgeRhs1)() setType NoType
              mmap(bridgeDef.vparamss)(_ setType NoType)

              // transform RHS of the defdef + typecheck
              val bridgeDef2 = localTyper.typed(bridgeDef)
              bridgeDef2
            }
          val defdef2 = localTyper.typed(deriveDefDef(defdef){rhs => super.atOwner(defdef.symbol)(super.transform(rhs))})

          Multi(defdef2 :: bridges)

        // Don't change the transformation description objects
        case tpl: ClassDef =>
          val isDescrObject = tpl.symbol.isTransfDescriptionObject
          if (isDescrObject)
            tpl
          else
            Descend

        case _ =>
          Descend
      }
    }
  }
}