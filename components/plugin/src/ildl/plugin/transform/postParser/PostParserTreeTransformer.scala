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
    def apply(unit: CompilationUnit): Unit = {
      //afterInteropBridge(new BridgeTransformer(unit).transformUnit(unit))
      new PostParserTransformer(unit).transformUnit(unit)
    }
  }

  class PostParserTransformer(unit: CompilationUnit) extends TreeRewriter(unit) {

    import global._

    case class ILDLAttachment(tree: Tree)
    class ILDLAttachementTraverser(att: ILDLAttachment) extends Traverser {
      override def traverse(tree: Tree) = tree match {
        case vd: ValDef if !vd.hasAttachment[ILDLAttachment] => vd.updateAttachment[ILDLAttachment](att)
        case _ => super.traverse(tree)
      }
    }
//    implicit class WithAlreadyTyped(val tree: Tree) {
//      def withTypedAnnot: Tree = tree.updateAttachment[AlreadyTyped.type](AlreadyTyped)
//    }

    protected def rewrite(tree: Tree): Result = {
      tree match {
        case Apply(Apply(Ident(TermName("adrt")), transf :: Nil), Block(stmts, expr) :: Nil) =>

          val empty = Apply(Apply(Ident(TermName("adrt")), transf :: Nil), Literal(Constant(())) :: Nil)
          val trees =
            expr match {
              case Literal(Constant(())) => empty :: stmts
              case _                     => empty :: stmts ::: List(expr)
            }

          val att = ILDLAttachment(transf)
          val trav = new ILDLAttachementTraverser(att)

          for (tree <- trees)
            trav traverse tree

          Multi(trees)
        case _ =>
          Descend
      }
    }
  }
}


