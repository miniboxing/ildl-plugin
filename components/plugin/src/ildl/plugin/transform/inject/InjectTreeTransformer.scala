package ildl.plugin
package transform
package inject

import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.Phase
import infrastructure.TreeRewriters
import scala.tools.nsc.transform.TypingTransformers
import ildl.plugin.infrastructure.TreeRewriters

trait InjectTreeTransformer extends TreeRewriters {
  self: InjectComponent =>

  import global._
  import helper._

  override def newTransformer(unit: CompilationUnit): Transformer =
    afterInject(new InjectTransformer(unit))

  def getTransformDescriptors(tree: Tree): List[Tree] = {
    val attach = tree.attachments.get[ildlAttachment].get
    val descrs = attach.descrs.map(metadata.descriptionObject(_))
    descrs
  }

  class InjectTransformer(unit: CompilationUnit) extends TreeRewriter(unit) {

    import global._

    def rewrite(tree: Tree) = tree match {

      // adrt scopes
      case Apply(Apply(adrt, List(descr)), List(_)) if adrt.symbol == idllAdrtSymbol =>
        metadata.descriptionObject(descr.pos) = descr
        Multi(Nil) // then wipe the reference to adrt :)

      // valDefs
      case vd @ ValDef(mods, name, tpt, rhs) if vd.hasAttachment[ildlAttachment] =>
//        val descr = metadata.descriptionObject(vd)
//        val ntpt = TypeTree(transformType(tpt.tpe, descr))
//        val nrhs = transform(rhs)
//        val vd2 = treeCopy.ValDef(tree, mods, name, ntpt, nrhs)
//        localTyper.typed(vd2)
        val descrs = getTransformDescriptors(tree)
        println("transforming " + vd + "  :  " + descrs)
        Descend

      // defdefs
      case dd : DefDef if dd.hasAttachment[ildlAttachment] =>
//        println("transforming " + vd + "  :  " + System.identityHashCode(tree) + "    " + vd.attachments)
        val descrs = getTransformDescriptors(tree)
        println("transforming " + dd + "  :  " + descrs)
        Descend
      case _ =>
        Descend
    }
  }
}


