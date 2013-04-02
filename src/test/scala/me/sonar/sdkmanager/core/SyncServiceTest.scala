package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api.GeofenceEvent
import org.scala_tools.time.Imports._
import me.sonar.sdkmanager.model.api.GeofenceEvent
import scala.Some

class SyncServiceTest extends SpringComponentTest {
    @Inject
    var service: SyncService = _
    "the service" should "aggregate values" in {
        val events = Seq(
            GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(DateTime.now - 2.hours), exiting = Some(DateTime.now - 1.hour)),
            GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(DateTime.now), exiting = None))
        var count = 0
        events.foreach {
            e =>
                e.id = count.toString
                count += 1
        }
        service.save("android", "dev1", events)
        val aggregates = service.computeAggregates()
        assert(aggregates lenientEquals Seq(Map("_id" -> "android", "visits" -> 2)))
    }

}
