package ildl.plugin
package transform
package inject

import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.Phase
import infrastructure.TreeRewriters
import scala.tools.nsc.transform.InfoTransform

trait InjectInfoTransformer extends InfoTransform {
  self: InjectComponent =>

  import global._

  override def transformInfo(sym: Symbol, tpe: Type): Type = tpe

  def transformType(tpe: Type, descr: Tree) = tpe
}


