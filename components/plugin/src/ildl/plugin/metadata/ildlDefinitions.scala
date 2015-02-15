package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap

trait ildlDefinitions {
  this: ildlHelperComponent =>

  import global._
  import definitions._

  lazy val ildlPackageObjectSymbol = rootMirror.getPackageObject("ildl")
  lazy val idllAdrtSymbol = definitions.getMemberMethod(ildlPackageObjectSymbol, TermName("adrt"))

  lazy val ildlPackageSymbol = rootMirror.getPackage(TermName("ildl"))
  lazy val ildlTransformationDescrSym = definitions.getMemberClass(ildlPackageSymbol, TypeName("TransformationDescription"))
  lazy val ildlHighClass =              definitions.getMemberClass(ildlPackageSymbol, TypeName("high"))

  // TODO: This should be defined programatically
  lazy val ildlInternalPackageSymbol = rootMirror.getPackage(TermName("ildl.internal"))
  lazy val reprClass = definitions.getMemberClass(ildlInternalPackageSymbol, TypeName("repr"))
}
