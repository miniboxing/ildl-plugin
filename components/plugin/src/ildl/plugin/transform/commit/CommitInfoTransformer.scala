// NOTE: This file was adapted form the miniboxing repository:
//
//     _____   .__         .__ ___.                    .__ scala-miniboxing.org
//    /     \  |__|  ____  |__|\_ |__    ____  ___  ___|__|  ____     ____
//   /  \ /  \ |  | /    \ |  | | __ \  /  _ \ \  \/  /|  | /    \   / ___\
//  /    Y    \|  ||   |  \|  | | \_\ \(  <_> ) >    < |  ||   |  \ / /_/  >
//  \____|__  /|__||___|  /|__| |___  / \____/ /__/\_ \|__||___|  / \___  /
//          \/          \/          \/               \/         \/ /_____/
// Copyright (c) 2011-2015 Scala Team, École polytechnique fédérale de Lausanne
//
// Authors:
//    * Vlad Ureche
//
package ildl.plugin
package transform
package commit


import scala.tools.nsc.transform.InfoTransform

trait CommitInfoTransformer extends InfoTransform {
  this: CommitComponent =>

  import global._
  import definitions._
  import helper._

  override def transformInfo(sym: Symbol, tpe: Type): Type = {
    val tpe2 = deepTransformation.transform(sym, tpe)
//    if (!(tpe =:= tpe2))
//      println(sym + "  old: " + tpe + "  new: " + tpe2)
    tpe2
  }

  object deepTransformation extends TypeMap {

    var symbol: Symbol = NoSymbol
    var position: Position = NoPosition

    def apply(tpe: Type) = mapOver(tpe)

    def transform(sym: Symbol, tpe: Type): Type = {
      symbol = sym
      val res = mapOver(tpe)
      symbol = NoSymbol
      res
    }

    def transform(pos: Position, tpe: Type): Type = {
      position = pos
      val res = mapOver(tpe)
      position = NoPosition
      res
    }

    def getPosition: Position =
      if (symbol == NoSymbol)
        position
      else
        symbol.pos

    override def mapOver(tpe: Type): Type = tpe match {
      case tpe if tpe.annotations.exists(ann => ann.tpe.typeSymbol == reprClass) =>
        val annots = tpe.annotations.filter(ann => ann.tpe.typeSymbol == reprClass)
        if (annots.length != 1)
          global.reporter.error(getPosition, s"Multiple annotations found for $symbol: ${beforeCommit(symbol.tpe)}")
        val descr = tpe.getAnnotDescrObject
        val tpe2 = getDescrReprType(descr, tpe.withoutReprAnnot)
        if (tpe2 == ErrorType)
          global.reporter.error(getPosition, "There is no corresponding representation type for high-level type " +
                                             tpe.withoutReprAnnot + " set in the `toRepr` method. The `toRepr` and `toHigh`" +
                                             "methods should define a bijection between the high and repr types! " +
                                             "The concerned transformation description " +
                                             "object is: " + descr.fullName + ".")
        tpe2.withoutHighAnnot
      case _ =>
        super.mapOver(tpe)
    }
  }
}
