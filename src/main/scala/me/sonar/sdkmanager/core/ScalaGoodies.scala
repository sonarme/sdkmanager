package me.sonar.sdkmanager.core

import collection.IterableLike
import java.util.UUID
import java.nio.ByteBuffer
import java.security.SecureRandom
import reflect.NameTransformer
import org.apache.commons.codec.binary.{StringUtils, Base64}
import org.joda.time.{DateTimeZone, DateTime}
import collection.generic.CanBuildFrom

object ScalaGoodies {
    def ?[A >: Null <: AnyRef](block: => A): A =
        try {
            block
        } catch {
            case e: NullPointerException if e.getStackTrace().isEmpty || e.getStackTrace().length >= 3 && e.getStackTrace()(2).getMethodName == "$qmark" => null
            case e: Throwable => throw e
        }

    def ??[A >: Null <: AnyRef](block: => A)(implicit manifest: Manifest[A]): Option[A] = ?(block) match {
        case a: A => Some(a)
        case _ => None
    }

    def optionDouble(doubleVal: java.lang.Double): Option[Double] = if (doubleVal == null) None else Some(doubleVal.doubleValue())

    def optionLong(longVal: java.lang.Long): Option[Long] = if (longVal == null) None else Some(longVal.longValue())

    def optionInteger(value: java.lang.Integer): Option[Int] = if (value == null) None else Some(value.intValue())

    def optionBoolean(value: java.lang.Boolean): Option[Boolean] = if (value == null) None else Some(value.booleanValue())

    private val urlSafeBase64 = new Base64(0, Array[Byte](), true)
    private val secureRandom = new SecureRandom

    def utcDate(datetime: DateTime) = datetime.withZone(DateTimeZone.UTC).toDateMidnight

    def compactGUID() = {
        // not UUID format, but based on UUID V4
        val bytes = new Array[Byte](16)
        secureRandom.nextBytes(bytes)
        urlSafeBase64Encode(bytes)
    }

    def urlSafeBase64Encode(bytes: Array[Byte]) = urlSafeBase64.encodeToString(bytes)

    def urlSafeBase64Decode(str: String) = urlSafeBase64.decode(str)

    @annotation.tailrec
    def retry[T](n: Int)(fn: => T): T = {
        val r = try {
            Some(fn)
        } catch {
            case e: Exception if n > 1 => None
        }
        r match {
            case Some(x) => x
            case None => retry(n - 1)(fn)
        }
    }


    def getField(f: java.lang.reflect.Field, instance: AnyRef) = {
        f.setAccessible(true)
        f.get(instance)
    }

    def showReflect(instance: AnyRef): String = {
        val fields = instance.getClass.getDeclaredFields.filterNot(_.isSynthetic)
        fields.map(f => NameTransformer.decode(f.getName) + "=" + getField(f, instance)).mkString(", ")
    }

    implicit class RichCollection[A, Repr](xs: IterableLike[A, Repr]) {
        def distinctBy[B, That](f: A => B)(implicit cbf: CanBuildFrom[Repr, A, That]) = {
            val builder = cbf(xs.repr)
            val i = xs.iterator
            var set = Set[B]()
            while (i.hasNext) {
                val o = i.next
                val b = f(o)
                if (!set(b)) {
                    set += b
                    builder += o
                }
            }
            builder.result
        }

    }

}





