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
    beforeInject(new InjectTransformer(unit))

  def getTransformDescriptors(tree: Tree): List[Tree] =
    if (tree.hasAttachment[ildlAttachment]) {
      val attach = tree.attachments.get[ildlAttachment].get
      val descrs = attach.descrs.map(metadata.descriptionObject(_))
      descrs
    } else {
      assert(tree.symbol.isAccessor && metadata.synbolDescriptionObjects.isDefinedAt(tree.symbol.accessed))
      metadata.synbolDescriptionObjects(tree.symbol.accessed)
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
        val descrs = getTransformDescriptors(tree)
        val ntpt = atOwner(vd.symbol)(localTyper.typed(TypeTree(transformType(vd.pos, tpt.tpe, descrs))))
        val nrhs = transform(rhs)
        val vd2 = treeCopy.ValDef(vd, mods, name, ntpt, nrhs)
        metadata.synbolDescriptionObjects(vd.symbol) = descrs
        afterInject(vd.symbol.info)
        localTyper.typed(vd2)

      // defdefs
      case dd @ DefDef(mods, name, tpars, vparamss, tpt, rhs) if shouldInject(dd) =>
        val descrs = getTransformDescriptors(tree)
        val ntpt = TypeTree(transformType(dd.pos, tpt.tpe, descrs))
        val nrhs = transform(rhs)
        val nvss = vparamss.map(_.map(transformValDef))
        val dd2 = treeCopy.DefDef(dd, mods, name, tpars, nvss, ntpt, nrhs)
        metadata.synbolDescriptionObjects(dd.symbol) = descrs
        localTyper.typed(dd2)

      case _ =>
        Descend
    }

    def shouldInject(dd: DefDef): Boolean =
      dd.hasAttachment[ildlAttachment] ||
      dd.symbol.isAccessor && metadata.synbolDescriptionObjects.isDefinedAt(dd.symbol.accessed)
  }
}


