package ildl
package benchmark

package object deforest {

  // allow lists to have a force method:
  implicit class ListOps[T](val list: List[T]) extends AnyVal {
    def force: List[T] = list
  }
}