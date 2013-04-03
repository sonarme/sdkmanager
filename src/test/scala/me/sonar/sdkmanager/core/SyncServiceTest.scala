package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api.GeofenceEvent
import org.scala_tools.time.Imports._

class SyncServiceTest extends SpringComponentTest {
    @Inject
    var service: SyncService = _
    "the service" should "aggregate values" in {
        val now = DateTime.now
        var count = 0

        {
            val events = Seq(
                GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(now withHourOfDay (9)), exiting = Some(now withHourOfDay (11))),
                GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(now withHourOfDay (10)), exiting = Some(now withHourOfDay (11))),
                GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(now withHourOfDay (10)), exiting = None))
            events.foreach {
                e =>
                    e.id = count.toString
                    count += 1
            }
            events.foreach {
                e =>
                    e.appId = "testApp"
            }
            service.save("android", "dev1", events)
        }


        {
            val events = Seq(
                GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(now withHourOfDay (9)), exiting = Some(now withHourOfDay (11))))
            events.foreach {
                e =>
                    e.id = count.toString
                    count += 1
            }
            events.foreach {
                e =>
                    e.appId = "testApp"
            }
            service.save("android", "dev2", events)
        }

        val aggregates = service.aggregateDwellTime("testApp")
        assert(aggregates lenientEquals (Map("gf1" -> Seq(DurationStats(1.hour.toDuration, 2.hours.toDuration, 100.minutes.toDuration)))))
    }

}
