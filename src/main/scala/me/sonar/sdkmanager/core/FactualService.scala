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
    val ageTranslation = Map("20" -> "10-20",
        "21" -> "10-20",
        "under_5" -> "0-10",
        "5_to_9" -> "0-10",
        "10_to_14" -> "10-20",
        "15_to_17" -> "10-20",
        "18_and_19" -> "10-20",
        "22_to_24" -> "20-30",
        "25_to_29" -> "20-30",
        "30_to_34" -> "30-40",
        "35_to_39" -> "30-40",
        "40_to_44" -> "40-50",
        "45_to_49" -> "40-50",
        "50_to_54" -> "50-60",
        "55_to_59" -> "50-60",
        "60_and_61" -> "60-70",
        "62_to_64" -> "60-70",
        "65_and_66" -> "60-70",
        "67_to_69" -> "60-70",
        "70_to_74" -> "70-80",
        "75_to_79" -> "70-80",
        "80_to_84" -> "80+",
        "85_years_and_over" -> "80+")


    implicit class maxAttributeImplicit(js: JsonNode) {
        def fieldMap = js.fields().map(entry => entry.getKey -> (entry.getValue.asDouble() / 100.0)).toMap[String, Double]

        def maxAttribute = fieldMap.maxBy(_._2)
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
        val levelAttained = demographics.get("education").get("level_attained")
        val genderDistribution = Seq("male", "female").map {
            case e => e -> (demographics.get("age_and_sex").get(e).asDouble() / 100.0)
        }.toMap[String, Double]
        val (education, educationProbability) = genderDistribution flatMap {
            case (g, prob) => levelAttained.get(g).fieldMap.mapValues(_ / 100.0 * prob)
        } groupBy (_._1) mapValues (
                _.map(_._2).sum
                ) maxBy (_._2)
        val (age, ageProbability) = genderDistribution flatMap {
            case (g, prob) => demographics.get("age_and_sex").get("age_ranges_by_sex").get(g).fieldMap.map {
                case (nodeName, nodeProbability) => ageTranslation(nodeName) -> nodeProbability / 100.0 * prob
            }
        } groupBy (_._1) mapValues (
                x => x.map(_._2).sum / x.size
                ) maxBy (_._2)
        val (gender, genderProbability) = genderDistribution.maxBy(_._2)
        val medianIncome = demographics.get("income").get("median_income").get("amount").asInt()
        val incomeBucket = createSegments(medianIncome, incomeBuckets, None).head.name
        val (housingMax, housingMaxProbability) = demographics.get("housing").get("household_type").maxAttribute
        val (raceMax, raceMaxProbability) = demographics.get("race_and_ethnicity").get("race").maxAttribute
        Seq(
            ProfileAttribute("income", incomeBucket, 0.7),
            ProfileAttribute("household", housingMax, housingMaxProbability),
            ProfileAttribute("ethnicity", raceMax, raceMaxProbability),
            ProfileAttribute("gender", gender, genderProbability),
            ProfileAttribute("education", education, educationProbability),
            ProfileAttribute("age", age, ageProbability)
        )
    }

}

case class FactualData()
