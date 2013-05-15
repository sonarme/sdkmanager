package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject

class AggregationServiceTest extends SpringComponentTest {
    @Inject
    var service: AggregationService = _

    "the service" should "aggregate values" in {
        //        println(service.aggregateDwellTime("demo"))
        /*println(service.aggregateVisits("test", "walmart"))*/
    }
}
