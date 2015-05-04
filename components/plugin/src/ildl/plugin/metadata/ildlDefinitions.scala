package ildl.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap
import scala.reflect.internal.Phase

trait ildlDefinitions {
  this: ildlHelperComponent =>

  import global._
  import definitions._

  def ildlBridgePhase: Phase

  sealed abstract trait TransformationType
  object Rigid     extends TransformationType
  object Freestyle extends TransformationType

  def flag_passive: Boolean

  lazy val ildlPackageObjectSymbol = rootMirror.getPackageObject("ildl")
  lazy val idllAdrtSymbol = definitions.getMemberMethod(ildlPackageObjectSymbol, TermName("adrt"))

  lazy val ildlPackageSymbol = rootMirror.getPackage(TermName("ildl"))
  lazy val ildlTransformationDescrSym = definitions.getMemberClass(ildlPackageSymbol, TypeName("TransformationDescription"))
  lazy val ildlRigidTransformationDescrSym = definitions.getMemberClass(ildlPackageSymbol, TypeName("RigidTransformationDescription"))
  lazy val ildlHighClass =              definitions.getMemberClass(ildlPackageSymbol, TypeName("high"))

  // TODO: This should be defined programatically
  lazy val ildlInternalPackageSymbol = rootMirror.getPackage(TermName("ildl.internal"))
  lazy val reprClass = definitions.getMemberClass(ildlInternalPackageSymbol, TypeName("repr"))

  lazy val reprToHighName = TermName("toHigh")
  lazy val highToReprName = TermName("toRepr")
  lazy val highTpeName = TypeName("High")
  lazy val reprTpeName = TypeName("Repr")

  lazy val nobridgeClass = global.rootMirror.getRequiredClass("ildl.internal.nobridge")
  lazy val nobridgeTpe = nobridgeClass.tpe
}
