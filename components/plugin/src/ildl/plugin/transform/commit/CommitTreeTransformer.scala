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
package commit

import scala.tools.nsc.transform.TypingTransformers
import scala.tools.nsc.typechecker._

trait CommitTreeTransformer extends TypingTransformers {
  self: CommitComponent =>

  import global._
  import definitions._
  import helper._
  import typer.{ typed, atOwner }

  override def newTransformer(unit: CompilationUnit): Transformer = new Transformer {

    override def transform(tree: Tree): Tree = {
      val specTrans = new TreeTransformer(unit)
      afterCommit(checkNoRepr(specTrans.transform(tree)))
    }
  }

  def checkNoRepr(tree: Tree) = {
    for (t <- tree)
      assert(noReprAnnot(t.tpe), t + ": " + t.tpe)
    tree
  }

  def noReprAnnot(t: Type): Boolean = {
    var hasStorage = false
    new TypeMap {
      def apply(tp: Type): Type = mapOver(tp)
      override def mapOver(tp: Type): Type = tp match {
        case _ if tp hasAnnotation(reprClass) =>
          hasStorage = true
          tp
        case _ =>
          super.mapOver(tp)
      }
    }.apply(t)

    !hasStorage
  }

  class TreeTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {

    override def transform(tree0: Tree): Tree = {
      val oldTpe = tree0.tpe
      val newTpe = deepTransformation.transform(tree0.pos, oldTpe)

      // force new info on the symbol
      if (tree0.hasSymbolField)
        tree0.symbol.info

      val tree1 = super.transform(tree0)

      tree1.setType(newTpe)
    }
  }
}
