package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.model.db._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.factual.driver.{Geopulse, Point, Factual}
import ch.hsr.geohash.{WGS84Point, GeoHash}
import collection.JavaConversions._

@Service
class FactualService extends Segmentation {
    @Inject
    var factual: Factual = _
    @Inject
    var factualDao: FactualGeopulseDao = _
    val om = new ObjectMapper
    val incomeBuckets = Seq(0 -> 10, 10 -> 15, 15 -> 25, 25 -> 35, 35 -> 50, 50 -> 75, 75 -> 100, 100 -> 150, 150 -> 200, 200 -> Int.MaxValue) map {
        case (fromIncome, toIncome) =>
            Segment(from = fromIncome * 1000, to = toIncome * 1000, name = fromIncome + "k" + (if (toIncome == Int.MaxValue) "+" else "-" + toIncome + "k"))
    }


    implicit class maxAttributeImplicit(js: JsonNode) {
        def maxAttribute = js.fields().map(entry => entry.getKey -> entry.getValue.asDouble()).maxBy(_._2)
    }

    def getFactualData(geohash: String) = {
        val response = factualDao.findOne(geohash).map(_.response).getOrElse {
            val point: WGS84Point = GeoHash.fromGeohashString(geohash).getBoundingBoxCenterPoint
            val geopulse = factual.get("places/geopulse", new Geopulse(new Point(point.getLatitude, point.getLongitude)) {
                def params = toUrlParams
            }.params)
            factualDao.save(FactualGeopulse(id = geohash, response = geopulse))
            geopulse
        }

        val json = om.readValue(response, classOf[JsonNode])
        val demographics = json.get("response").get("data").get("demographics")
        val medianIncome = demographics.get("income").get("median_income").get("amount").asInt()
        val incomeBucket = createSegments(medianIncome, incomeBuckets, None).head.name
        val (housingMax, housingMaxProbability) = demographics.get("housing").get("household_type").maxAttribute
        val (raceMax, raceMaxProbability) = demographics.get("race_and_ethnicity").get("race").maxAttribute
        Seq(ProfileAttribute("income", incomeBucket, 0.7), ProfileAttribute("household", housingMax, housingMaxProbability), ProfileAttribute("ethnicity", raceMax, raceMaxProbability))
    }

}

case class FactualData()
