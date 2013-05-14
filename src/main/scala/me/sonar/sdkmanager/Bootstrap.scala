package me.sonar.sdkmanager

import org.springframework.context.support.ClassPathXmlApplicationContext
import javax.inject.Inject
import me.sonar.sdkmanager.core.{AggregationService, FactualService}
import ch.hsr.geohash.GeoHash
import me.sonar.sdkmanager.model.db.{ProfileAttribute, DB}
import scala.slick.session.Session

object Bootstrap extends App {

    val context = new ClassPathXmlApplicationContext("classpath:spring/root-context.xml")
    val packageName = Bootstrap.getClass.getPackage.getName
    val className = args.head
    val handler = Class.forName(s"$packageName.$className").newInstance.asInstanceOf[Handler]
    context.getAutowireCapableBeanFactory.autowireBean(handler)
    handler.execute(args.tail)
}


trait Handler {
    def execute(args: Array[String])
}

class FactualFetcher extends Handler with DB {
    @Inject
    var factualService: FactualService = _
    @Inject
    var attributeService: AggregationService = _

    import org.scala_tools.time.Imports._
    import profile.simple._

    def execute(args: Array[String]) {
        attributeService.getAttributesOfType("home") foreach {
            a =>
                db withSession {
                    implicit session: Session =>
                        factualService.getFactualData(a.value) foreach {
                            case ((value, probability, t)) =>
                                ProfileAttributes.insert(ProfileAttribute(id = a.appId + "-" + a.deviceId + "-" + t + "-" + value, appId = a.appId, deviceId = a.deviceId, `type` = t, value = value, probability = probability, lastModified = DateTime.now))
                        }
                }
        }
    }
}
