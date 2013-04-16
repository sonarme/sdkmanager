package me.sonar.sdkmanager.core

trait Segmentation {
    def createSegments[T <% Comparable[T], S](value: T, segments: Iterable[Segment[T, S]], wrapAroundPoint: Option[(T, T)] = None) = segments.filter(_.contains(value, wrapAroundPoint))

    case class Segment[T <% Comparable[T], S](from: T, to: T, name: S) {

        def compareValue(left: T, right: T) = left.asInstanceOf[Comparable[T]].compareTo(right)

        def containsValue(value: T, fromC: T, toC: T) = compareValue(fromC, value) <= 0 && compareValue(value, toC) < 0

        def contains(value: T, wrapAroundPointOpt: Option[(T, T)]) =
            wrapAroundPointOpt match {
                case Some(wrapAroundPoint) if (compareValue(from, to) > 0) =>
                    containsValue(value, from, wrapAroundPoint._1) || containsValue(value, wrapAroundPoint._2, to)
                case _ => containsValue(value, from, to)
            }
    }

}
