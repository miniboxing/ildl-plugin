package ildl
package benchmark

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