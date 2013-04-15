package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api.{SyncRequest, GeofenceEvent}
import org.scala_tools.time.Imports._
import com.factual.driver.{ReadResponse, Point, Geopulse, Factual}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.`type`.TypeReference

class FactualTest extends SpringComponentTest {
    @Inject
    var factualService: FactualService = _

    "factual" should "return data" in {

        val data = factualService.getFactualData("dr5x")

        assert(data != null)
    }

}
