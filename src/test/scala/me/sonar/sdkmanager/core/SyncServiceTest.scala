package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api.{SyncRequest, GeofenceEvent}
import org.scala_tools.time.Imports._
import me.sonar.sdkmanager.model.db.{ProfileAttributesDao, ProfileAttribute}
import me.sonar.sdkmanager.model.Platform
import collection.JavaConversions._

class SyncServiceTest extends SpringComponentTest {
    @Inject
    var service: SyncService = _
    @Inject
    var profileAttributesDao: ProfileAttributesDao = _

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
            service.save(Platform.android, "dev1", "testApp-SyncServiceTest", SyncRequest(-1, events = events, profileAttributes = Seq(ProfileAttribute(value = "testValue", probability = 1.0, `type` = "testType", lastModified = new java.util.Date))))
        }


        {
            val events = Seq(
                GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(now withHourOfDay (9)), exiting = Some(now withHourOfDay (11))))
            events.foreach {
                e =>
                    e.id = count.toString
                    count += 1
            }
            service.save(Platform.android, "dev2", "testApp-SyncServiceTest", SyncRequest(-1, events = events))
        }

        val aggregates = service.aggregateDwellTime("testApp-SyncServiceTest")
        val geofences = service.aggregateGeofenceData("testApp")
        assert(aggregates lenientEquals (Map("gf1" -> CountStats(1.hour.millis, 2.hours.millis, 100.minutes.millis))))
    }

    "the service" should "overwrite app categories" in {
        val syncRequest = new SyncRequest(-1, profileAttributes = Seq(ProfileAttribute(value = "me.sonar.android,org.wikipedia,ht.highlig", `type` = "installed_apps")))
        service.save(Platform.android, "dev1", "testApp-SyncServiceTest", syncRequest)
        Thread.sleep(5000)
        service.save(Platform.android, "dev1", "testApp-SyncServiceTest", syncRequest) //hack...do it twice incase we don't already have it in the db
        val profileAttribute = profileAttributesDao.findOne("testApp-SyncServiceTest-android-dev1").orNull

        assert(profileAttribute != null)
        assert(profileAttribute.attributes.filter(_.`type` == "category").size === 2)

        val syncRequest2 = new SyncRequest(-1, profileAttributes = Seq(ProfileAttribute(`type` = "installed_apps", value = "me.sonar.android")))
        service.save(Platform.android, "dev1", "testApp-SyncServiceTest", syncRequest2)
        val profileAttribute2 = profileAttributesDao.findOne("testApp-SyncServiceTest-android-dev1").orNull

        assert(profileAttribute2 != null)
        assert(profileAttribute2.attributes.filter(_.`type` == "category").size === 1)

    }
}
