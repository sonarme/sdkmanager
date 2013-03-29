package com.sonar.dossier.testing

import ognl.DefaultMemberAccess
import ognl.Ognl
import ognl.OgnlContext
import org.unitils.reflectionassert.difference.Difference
import org.unitils.reflectionassert.report.impl.DefaultDifferenceReport
import ReflectionComparatorFactory.createRefectionComparator
import org.unitils.reflectionassert.ReflectionComparatorMode._
import org.unitils.reflectionassert.ReflectionComparator
import org.unitils.reflectionassert.ReflectionComparatorMode
import org.unitils.reflectionassert.{ReflectionComparator, ReflectionComparatorMode}
import scala.collection.JavaConversions._
import org.unitils.reflectionassert.comparator.Comparator
import org.unitils.reflectionassert.comparator.impl._
import collection.mutable.ListBuffer
import org.joda.time.DateTime
import org.unitils.reflectionassert.ReflectionComparatorMode._
import org.unitils.reflectionassert.difference.Difference
import org.unitils.reflectionassert.report.impl.DefaultDifferenceReport
import org.unitils.reflectionassert.comparator.Comparator
import org.unitils.reflectionassert.comparator.impl.LenientDatesComparator
import org.unitils.reflectionassert.comparator.impl.IgnoreDefaultsComparator
import org.unitils.reflectionassert.comparator.impl.LenientNumberComparator
import org.unitils.reflectionassert.comparator.impl.SimpleCasesComparator
import org.unitils.reflectionassert.comparator.impl.LenientOrderCollectionComparator
import org.unitils.reflectionassert.comparator.impl.CollectionComparator
import org.unitils.reflectionassert.comparator.impl.MapComparator
import org.unitils.reflectionassert.comparator.impl.HibernateProxyComparator
import org.unitils.reflectionassert.comparator.impl.ObjectComparator

trait ReflectionDiff {
    implicit def convertToEqualizer(left: AnyRef) = new ReflectionEqualizer(left)

    implicit def convertToEqualizer(left: Iterable[_]) = new ReflectionIterableEqualizer(left)
}

/**
 * Based on unitils ReflectionAssert, but supports Scala classes and works as implicit with assertions from any test framework
 * @param actual the actual object to be tested against the expected value
 * @author bkempe
 */
class ReflectionEqualizer(val actual: AnyRef) {

    /**
     * Asserts that two objects are equal. Reflection is used to compare all fields of these values.
     * If they are not equal an AssertionFailedError is thrown.
     * <p/>
     * This is identical to assertReflectionEquals with
     * lenient order and ignore defaults set as comparator modes.
     *
     * @param expected the expected object
     * @return difference
     */
    def lenientEquals(expected: AnyRef) = reflectionEquals(expected, LENIENT_ORDER, IGNORE_DEFAULTS)

    /**
     * Asserts that two objects are equal. Reflection is used to compare all fields of these values.
     * If they are not equal an AssertionFailedError is thrown.
     * <p/>
     * The comparator modes determine how strict to compare the values.
     *
     * @param expected the expected object
     * @param modes    the comparator modes
     * @return difference
     */
    def reflectionEquals(expected: AnyRef, modes: ReflectionComparatorMode*) = {
        val reflectionComparator = createRefectionComparator(modes: _*)
        val message = for (difference <- Option(reflectionComparator.getDifference(expected, actual))) yield getFailureMessage(difference)
        message
    }

    /**
     * @param difference the difference, not null
     * @return a failure message describing the difference found
     */
    protected def getFailureMessage(difference: Difference): String = new DefaultDifferenceReport().createReport(difference)


    /**
     * Asserts that the value of a property of an object is equal to the given value.
     * <p/>
     * Bean notation can be used to specify inner properties. Eg myArray[2].innerValue.
     * assertReflectionEquals is used to check whether both values are equal.
     * <p/>
     * The comparator modes determine how strict to compare the values.
     *
     * @param propertyName          the property, not null
     * @param expectedPropertyValue the expected value
     * @param modes                 the comparator modes
     * @return difference
     */
    def propertyReflectionEquals(propertyName: String, expectedPropertyValue: AnyRef, modes: ReflectionComparatorMode*) = {
        val propertyValue = ReflectionComparatorFactory.getProperty(actual, propertyName)
        new ReflectionEqualizer(propertyValue).reflectionEquals(expectedPropertyValue, modes: _*)
    }
}

class ReflectionIterableEqualizer(val actualObjects: Iterable[_]) {


    /**
     * Asserts that a property of all objects in the collection are equal to the given values.
     * <p/>
     * Example:  assertPropertyEquals("id", myIdCollection, myObjectCollection) checks whether all values of the
     * id field of the myObjectCollection elements matches the values in the myIdCollection
     * <p/>
     * Bean notation can be used to specify inner properties. Eg myArray[2].innerValue.
     * assertReflectionEquals is used to check whether both values are equal.
     * <p/>
     * The comparator modes determine how strict to compare the values.
     *
     * @param propertyName           the property, not null
     * @param expectedPropertyValues the expected values, not null
     * @param modes                  the comparator modes
     * @return difference
     */
    def propertyReflectionEquals(propertyName: String, expectedPropertyValues: Iterable[_], modes: ReflectionComparatorMode*) = {
        val actualPropertyValues = actualObjects.collect {
            case ref: AnyRef if ref != null => ReflectionComparatorFactory.getProperty(ref, propertyName)
        }
        new ReflectionEqualizer(actualPropertyValues).reflectionEquals(expectedPropertyValues, modes: _*)
    }


}

/**
 * Scala version of Unitils ReflectionComparatorFactory
 * @see org.unitils.reflectionassert.ReflectionComparatorFactory
 */
object ReflectionComparatorFactory {
    /**
     * Evaluates the given OGNL expression, and returns the corresponding property value from the given object.
     *
     * @param object         The object on which the expression is evaluated
     * @param ognlExpression The OGNL expression that is evaluated
     * @return The value for the given OGNL expression
     */
    def getProperty(`object`: AnyRef, ognlExpression: String) = {
        val ognlContext = new OgnlContext
        ognlContext.setMemberAccess(new DefaultMemberAccess(true))
        val ognlExprObj = Ognl.parseExpression(ognlExpression)
        Ognl.getValue(ognlExprObj, ognlContext, `object`)
    }

    /**
     * Creates a reflection comparator for the given modes.
     * If no mode is given, a strict comparator will be created.
     *
     * @param modes The modes, null for strict comparison
     * @return The reflection comparator, not null
     */
    def createRefectionComparator(modes: ReflectionComparatorMode*): ReflectionComparator = {
        val comparators = getComparatorChain(modes.toSet[ReflectionComparatorMode])
        new ReflectionComparator(comparators)
    }

    /**
     * Creates a comparator chain for the given modes.
     * If no mode is given, a strict comparator will be created.
     *
     * @param modes The modes, null for strict comparison
     * @return The comparator chain, not null
     */
    protected def getComparatorChain(modes: Set[ReflectionComparatorMode]) = {
        val comparatorChain = new ListBuffer[Comparator]
        if (modes(LENIENT_DATES)) {
            comparatorChain += LenientDatesComparator
        }
        if (modes(IGNORE_DEFAULTS)) {
            comparatorChain += IgnoreDefaultsComparator
        }
        comparatorChain += LenientNumberComparator
        comparatorChain += SimpleCasesComparator
        if (modes(LENIENT_ORDER)) {
            comparatorChain += LenientOrderComparator
        }
        else {
            comparatorChain += CollectionComparator
        }
        comparatorChain += MapComparator
        comparatorChain += HibernateProxyComparator
        comparatorChain += ObjectComparator
        comparatorChain.toList
    }

    /**
     * The LenientDatesComparator singleton insance
     */
    protected final val LenientDatesComparator = new LenientDatesComparator {
        override def canCompare(left: Any, right: Any) =
            super.canCompare(convert(left), convert(right))

        def convert(anyDate: Any) = anyDate match {
            case dateTime: DateTime => dateTime.toDate
            case date => date
        }

        override def compare(left: Any, right: Any, onlyFirstDifference: Boolean, reflectionComparator: ReflectionComparator) =
            super.compare(convert(left), convert(right), onlyFirstDifference, reflectionComparator)
    }
    /**
     * The IgnoreDefaultsComparator singleton insance
     */
    protected final val IgnoreDefaultsComparator = new IgnoreDefaultsComparator
    /**
     * The LenientNumberComparator singleton insance
     */
    protected final val LenientNumberComparator = new LenientNumberComparator
    /**
     * The SimpleCasesComparatorsingleton insance
     */
    protected final val SimpleCasesComparator = new SimpleCasesComparator
    /**
     * The LenientOrderCollectionComparator singleton insance
     */
    protected final val LenientOrderComparator = new LenientOrderCollectionComparator {
        override def canCompare(left: Any, right: Any) =
            super.canCompare(convert(left), convert(right))


        def convert(anyIterable: Any) = anyIterable match {
            case iterable: Iterable[_] => iterable: java.util.Collection[_]
            case iterable => iterable
        }

        override def compare(left: Any, right: Any, onlyFirstDifference: Boolean, reflectionComparator: ReflectionComparator) =
            super.compare(convert(left), convert(right), onlyFirstDifference, reflectionComparator)

    }
    /**
     * The CollectionComparator singleton insance
     */
    protected final val CollectionComparator = new CollectionComparator {
        override def canCompare(left: Any, right: Any) =
            super.canCompare(convert(left), convert(right))


        def convert(anyIterable: Any) = anyIterable match {
            case iterable: Iterable[_] => iterable: java.util.Collection[_]
            case iterable => iterable
        }

        override def compare(left: Any, right: Any, onlyFirstDifference: Boolean, reflectionComparator: ReflectionComparator) =
            super.compare(convert(left), convert(right), onlyFirstDifference, reflectionComparator)

    }
    /**
     * The MapComparator singleton insance
     */
    protected final val MapComparator = new MapComparator {
        override def canCompare(left: Any, right: Any) =
            super.canCompare(convert(left), convert(right))

        def convert(anyMap: Any) = anyMap match {
            case map: Map[_, _] => map: java.util.Map[_, _]
            case map => map
        }

        override def compare(left: Any, right: Any, onlyFirstDifference: Boolean, reflectionComparator: ReflectionComparator) = {

            super.compare(convert(left), convert(right), onlyFirstDifference, reflectionComparator)
        }
    }
    /**
     * The HibernateProxyComparator singleton insance
     */
    protected final val HibernateProxyComparator = new HibernateProxyComparator
    /**
     * The ObjectComparator singleton insance
     */
    protected final val ObjectComparator = new ObjectComparator
}


