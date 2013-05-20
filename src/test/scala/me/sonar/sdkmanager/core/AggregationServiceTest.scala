package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import collection.JavaConversions._

class AggregationServiceTest extends SpringComponentTest {
    @Inject
    var service: AggregationService = _
    @Inject
    var factualService: FactualService = _

    "the service" should "aggregate values" in {
        //        println(service.aggregateDwellTime("demo"))
        /*println(service.aggregateVisits("test", "walmart"))*/
    }

    "the service" should "return top places" in {
        val data = service.topPlaces("test", 1, 1361394777000L).take(4)
        println(data)

        val factualIds = data.map(d => d.term.substring(d.term.indexOf("factual-") + 8))
        val factualPlaces = factualService.getFactualPlacesById(factualIds)
        println(factualPlaces)
    }
}
