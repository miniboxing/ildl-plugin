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
    import helper._

    class ildlAttachementTraverser(att: ildlAttachment) extends Traverser {

      println()
      println("Attachment Traverser initialized")

      override def traverse(tree: Tree) = {
        tree match {
          case _: ValDef | _: DefDef =>
            tree.updateAttachment[ildlAttachment](att)
            println("marked " + tree + "  :  " + tree.attachments)
          case _ =>
        }
        super.traverse(tree)
      }
    }

    class ildlMetadataTraverser(descr: Tree) extends Traverser {

      println()
      println("Attachment Metadata initialized")

      override def traverse(tree: Tree) = {
        tree match {
          case _: ValDef | _: DefDef =>
            metadata.descriptionObject(tree) = descr
            println("added to metadata " + tree + "    " + System.identityHashCode(tree))
          case _ =>
        }
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

          val att = ildlAttachment(descr)
          val trav = new ildlAttachementTraverser(att)
          val trav2 = new ildlMetadataTraverser(descr)

          for (tree <- trees) {
            trav traverse tree
            trav2 traverse tree
          }

          Multi(trees)
        case _ =>
          Descend
      }
    }
  }
}


