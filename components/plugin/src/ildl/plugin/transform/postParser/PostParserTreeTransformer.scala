package ildl.plugin
package transform
package postParser

import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.Phase
import infrastructure.TreeRewriters

trait PostParserTreeTransformer {
  self: PostParserComponent =>

  import global._

  def newPhase(prev: Phase): StdPhase =
    new PostParserPhase(prev)

  class PostParserPhase(prev: Phase) extends StdPhase(prev) {
    override def name = PostParserTreeTransformer.this.phaseName
    def apply(unit: CompilationUnit): Unit =
      new PostParserTransformer(unit).transformUnit(unit)
  }

  class PostParserTransformer(unit: CompilationUnit) extends TreeRewriter(unit) {

    import global._
    import helper._

    class ildlAttachementTraverser(descr: Position) extends Traverser {

      override def traverse(tree: Tree) = {
        val newatt =
          if (tree.hasAttachment[ildlAttachment])
            ildlAttachment(descr :: tree.attachments.get[ildlAttachment].get.descrs)
          else
            ildlAttachment(descr :: Nil)
        tree.updateAttachment[ildlAttachment](newatt)
        super.traverse(tree)
      }
    }

    protected def rewrite(tree: Tree): Result = {
      tree match {
        case Apply(Apply(Ident(TermName("adrt")), descr :: Nil), Block(stmts, expr) :: Nil) =>

          // keep the empty tree around, to force type-checking of the description object
          // the ildl-inject phase will remove it from the tree
          val empty = Apply(Apply(Ident(TermName("adrt")), descr :: Nil), Literal(Constant(())) :: Nil)

          // the actual trees that are transformed => flattened in-place
          val trees =
            expr match {
              case Literal(Constant(())) => empty :: stmts
              case _                     => empty :: stmts ::: List(expr)
            }

          // add the transformation to all trees
          val trav = new ildlAttachementTraverser(descr.pos)

          for (tree <- trees)
            trav traverse tree

          Multi(trees)
        case _ =>
          Descend
      }
    }
  }
}


