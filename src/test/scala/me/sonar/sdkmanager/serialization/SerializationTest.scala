package me.sonar.sdkmanager.serialization

import me.sonar.sdkmanager.SimpleTest
import me.sonar.sdkmanager.web.api.RestObjectMapper
import org.scala_tools.time.Imports._
import me.sonar.sdkmanager.model.api._
import me.sonar.sdkmanager.model.api.GeofenceEvent
import me.sonar.sdkmanager.model.api.SyncRequest
import me.sonar.sdkmanager.model.api.StaticGeoFence
import scala.Some
import me.sonar.sdkmanager.model.api.DynamicGeoFence

class SerializationTest extends SimpleTest {
    val om = new RestObjectMapper

    "the rest object mapper" should "(de)serialize StaticGeofences" in {
        val fence = StaticGeoFence(lat = 40.7453940, lng = -73.9838360, radius = 800, entering = true)
        fence.id = "testfence"
        fence.processRole = true
        fence.publish = true
        val json = om.writeValueAsString(fence)
        val back = om.readValue(json, classOf[StaticGeoFence])
        assert(back lenientEquals fence)
    }
    "the rest object mapper" should "(de)serialize SyncRequests" in {
        val syncRequest = SyncRequest(1, Seq(GeofenceEvent("gf1", lat = 40.7453940, lng = -73.9838360, entering = Some(DateTime.now), exiting = None)))

        val json = om.writeValueAsString(syncRequest)
        val back = om.readValue(json, classOf[SyncRequest])
        assert(back lenientEquals syncRequest)
    }
    "the rest object mapper" should "(de)serialize DynamicGeoFences" in {
        val fence = DynamicGeoFence("home", radius = 150)
        fence.id = "testfence"
        fence.processRole = true
        fence.publish = true
        val json = om.writeValueAsString(fence)
        val back = om.readValue(json, classOf[DynamicGeoFence])
        assert(back lenientEquals fence)
    }
}
