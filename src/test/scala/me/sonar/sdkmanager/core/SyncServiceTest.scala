package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api.GeofenceEvent
import scala.Some
import org.joda.time.DateTime

class SyncServiceTest extends SpringComponentTest {
    @Inject
    var service: SyncService = _
    "the service" should "aggregate values" in {
        val events = Seq(
            GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(new DateTime withHourOfDay (9)), exiting = Some(new DateTime withHourOfDay (11))),
            GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(new DateTime withHourOfDay (10)), exiting = Some(new DateTime withHourOfDay (11))),
            GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(new DateTime withHourOfDay (10)), exiting = None))
        var count = 0
        events.foreach {
            e =>
                e.id = count.toString
                count += 1
        }
        service.save("android", "dev1", events)
        val aggregates = service.computeAggregates()
        assert(aggregates lenientEquals Seq(Map("_id" -> Map("platform" -> "android", "deviceId" -> "dev1"), "visits" -> 2)))
    }

}
