import scala.reflect._
import scala.reflect.api._
import scala.reflect.macros.blackbox._
import scala.language.experimental.macros

package object ildl {

  private type Descr = TransformationDescription with Singleton

  /**
   *  The iLDL ad-hoc data representation transformation
   */
  def adrt[T](descr: Descr)(f: T): T = macro adrt_impl[T]

  /** iLDL macro implementation */
  def adrt_impl[T](c: Context)(descr: c.Expr[Descr])(f: T): c.Expr[T] =
    ???
}

