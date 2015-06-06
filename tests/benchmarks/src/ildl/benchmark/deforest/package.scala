package ildl
package benchmark

//
// You can read about this benchmark on the following wiki page:
// https://github.com/miniboxing/ildl-plugin/wiki/Sample-%7E-Deforestation
//

package object deforest {

  /**
   * This implicit class adds a ".force" method to all lists,
   * allowing the transformed code to force the
   *  allow lists to have a force method:
   */
  implicit class ListOps[T](val list: List[T]) extends AnyVal {
    def force: List[T] = list
  }
}