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

    protected def rewrite(tree: Tree): Result = {
      tree match {
        case Apply(Apply(Ident(TermName("adrt")), transf :: Nil), Block(stmts, expr) :: Nil) =>
          expr match {
            case Literal(Constant(())) => Multi(stmts)
            case _                     => Multi(stmts ::: List(expr))
          }
        case _ =>
          Descend
      }
    }
  }
}


