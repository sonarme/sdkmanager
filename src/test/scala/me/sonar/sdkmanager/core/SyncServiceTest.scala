package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api.{SyncRequest, GeofenceEvent}
import org.scala_tools.time.Imports._
import me.sonar.sdkmanager.model.db.ProfileAttribute

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
            service.save("android", "dev1", "testApp-SyncServiceTest", SyncRequest(-1, events = events, profileAttributes = Seq(ProfileAttribute(key = "testAttribute", value = "testValue", probability = 1.0, lastModified = new java.util.Date))))
        }


        {
            val events = Seq(
                GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(now withHourOfDay (9)), exiting = Some(now withHourOfDay (11))))
            events.foreach {
                e =>
                    e.id = count.toString
                    count += 1
            }
            service.save("android", "dev2", "testApp-SyncServiceTest", SyncRequest(-1, events = events))
        }

        val aggregates = service.aggregateDwellTime("testApp-SyncServiceTest")
        assert(aggregates lenientEquals (Map("gf1" -> CountStats(1.hour.millis, 2.hours.millis, 100.minutes.millis))))
    }

}
