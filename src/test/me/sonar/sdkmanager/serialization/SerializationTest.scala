package me.sonar.sdkmanager.serialization

import me.sonar.sdkmanager.SimpleTest
import me.sonar.sdkmanager.model.StaticGeoFence
import me.sonar.sdkmanager.web.api.RestObjectMapper

class SerializationTest extends SimpleTest {
    val om = new RestObjectMapper

    "the rest object mapper" should "serialize scala classes" in {
        val fence = StaticGeoFence(lat = 40.7453940, lng = -73.9838360, radius = 800)
        fence.id = "testfence"
        fence.processRole = true
        fence.publish = true
        val json = om.writeValueAsString(fence)
        val back = om.readValue(json, classOf[StaticGeoFence])
        assert(back.id === fence.id)
    }

}
