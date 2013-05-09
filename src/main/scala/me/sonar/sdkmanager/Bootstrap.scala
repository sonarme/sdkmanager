package me.sonar.sdkmanager

import org.springframework.context.support.ClassPathXmlApplicationContext
import javax.inject.Inject
import me.sonar.sdkmanager.core.{AggregationService, FactualService}
import ch.hsr.geohash.GeoHash

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

class FactualFetcher extends Handler {
    @Inject
    var factualService: FactualService = _
    @Inject
    var attributeService: AggregationService = _

    def execute(args: Array[String]) {
        attributeService.getAttributesOfType("home") foreach {
            a =>
                factualService.getFactualData(a.value)
        }
    }
}
