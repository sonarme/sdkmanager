package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.web.api.RestObjectMapper
import me.sonar.sdkmanager.model.api.Campaign
import me.sonar.sdkmanager.model.db._
import com.fasterxml.jackson.databind.ObjectMapper
import com.factual.driver.{Geopulse, Point, Factual}
import ch.hsr.geohash.{WGS84Point, GeoHash}
import com.fasterxml.jackson.core.`type`.TypeReference
import me.sonar.sdkmanager.model.db.AppCampaign
import me.sonar.sdkmanager.model.api.Campaign

@Service
class FactualService {
    @Inject
    var factual: Factual = _
    @Inject
    var factualDao: FactualGeopulseDao = _
    val om = new ObjectMapper

    def getFactualData(geohash: String) = {
        factualDao.findOne(geohash).map(_.demographics).getOrElse {
            val point: WGS84Point = GeoHash.fromGeohashString(geohash).getBoundingBoxCenterPoint
            val geopulse = factual.get("places/geopulse", new Geopulse(new Point(point.getLatitude, point.getLongitude)) {
                def params = toUrlParams
            }.params)
            val map: java.util.Map[String, Object] = om.readValue(geopulse, new TypeReference[java.util.Map[String, Object]] {})
            val demographics = map.get("response").asInstanceOf[java.util.Map[String, Object]].get("data").asInstanceOf[java.util.Map[String, Object]].get("demographics").asInstanceOf[java.util.Map[String, Object]]
            factualDao.save(FactualGeopulse(id = geohash, demographics = demographics))
            demographics
        }
    }

}
