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

  class InjectTransformer(unit: CompilationUnit) extends TreeRewriter(unit) {

    println("InjectTransformer initialized")
    println(metadata.descriptionObject.keySet.map(_.tpe).mkString("\n"))

    import global._

    def rewrite(tree: Tree) = tree match {
      // ValDefs and DefDefs
      case vd @ ValDef(mods, name, tpt, rhs) => //if vd.hasAttachment[ildlAttachment] => //metadata.descriptionObject.isDefinedAt(vd) =>
//        val descr = metadata.descriptionObject(vd)
//        val ntpt = TypeTree(transformType(tpt.tpe, descr))
//        val nrhs = transform(rhs)
//        val vd2 = treeCopy.ValDef(tree, mods, name, ntpt, nrhs)
//        localTyper.typed(vd2)
        println("transforming " + vd + "  :  " + System.identityHashCode(tree) + "    " + vd.attachments)
        vd
      case _ =>
        Descend
    }
  }
}


