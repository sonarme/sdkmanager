package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.model.db._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import ch.hsr.geohash.{BoundingBox, WGS84Point, GeoHash}
import collection.JavaConversions._
import me.sonar.sdkmanager.core.ScalaGoodies._
import scala.slick.session.Database
import Database.threadLocalSession
import com.factual.driver._
import com.factual.driver.{Query => FactualQuery}
import me.sonar.sdkmanager.model.db.ProfileAttribute
import me.sonar.sdkmanager.model.api.FactualPlaceResponse
import me.sonar.sdkmanager.model.api.FactualPlaceRequest

@Service
class FactualService extends Segmentation with DB {

    import profile.simple._

    @Inject
    var factual: Factual = _
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

    implicit class ProfileAttributeMapper(map: Map[String, Double]) {
        def toProfileAttributes(`type`: String) = map.map {
            case (v, p) => (v, p, `type`)
        }.toSeq
    }

    def getFactualData(geohash: String) = db withSession {
        val response = FactualGeopulseResponses.findById(geohash).map(_.response).getOrElse {
            val point: WGS84Point = GeoHash.fromGeohashString(geohash).getBoundingBoxCenterPoint
            // US only
            if (new BoundingBox(new WGS84Point(24.544245, -124.733253), new WGS84Point(49.388611, -66.954811)).contains(point)) {
                val geopulse = factual.get("places/geopulse", new Geopulse(new Point(point.getLatitude, point.getLongitude)) {
                    def params = toUrlParams
                }.params)
                FactualGeopulseResponses.insert(FactualGeopulse(id = geohash, response = geopulse))
                geopulse
            } else "{}"
        }

        val json = om.readValue(response, classOf[JsonNode])
        if (json.get("response") != null && json.get("response").get("data") != null && json.get("response").get("data").get("demographics") != null && json.get("response").get("data").get("education") != null) try {
            val demographics = json.get("response").get("data").get("demographics")
            val levelAttained = demographics.get("education").get("level_attained")
            val genderDistribution = Seq("male", "female").map {
                case e => e -> (demographics.get("age_and_sex").get(e).asDouble() / 100.0)
            }.toMap[String, Double]
            val educationMap = genderDistribution.keys flatMap {
                g => levelAttained.get(g).fieldMap
            } groupBy (_._1) mapValues (
                    _.map(_._2).sum
                    )
            val educationTotal = educationMap.values.sum
            val eds = educationMap mapValues (_ / educationTotal)
            val educationAttributes = eds.toProfileAttributes("education")
            val ageTranslated = genderDistribution.keys flatMap {
                g => demographics.get("age_and_sex").get("age_ranges_by_sex").get(g).fieldMap.map {
                    case (nodeName, nodeProbability) => ageTranslation(nodeName) -> nodeProbability
                }
            }
            val ageMap = ageTranslated.groupBy(_._1).mapValues(
                _.map(_._2).sum
            )
            val ageAttributes = ageMap.toProfileAttributes("age")
            val genderAttributes = genderDistribution.toProfileAttributes("gender")
            val medianIncome = demographics.get("income").get("median_income").get("amount").asInt()
            val incomeBucket = createSegments(medianIncome, incomeBuckets, None).head.name
            val housingAttributes = demographics.get("housing").get("household_type").fieldMap.toProfileAttributes("household")
            val ethnicityAttributes = demographics.get("race_and_ethnicity").get("race").fieldMap.toProfileAttributes("ethnicity")
            val ret = Seq(
                (incomeBucket, 0.7, "income")
            ) ++ ageAttributes ++ genderAttributes ++ housingAttributes ++ ethnicityAttributes ++ educationAttributes

            //        ret.filterNot(_.probability == 0)
            ret
        } catch {
            case e: Exception => throw new RuntimeException("" + json, e)
        } else Seq.empty[(String, Double, String)]
    }

    def getFactualPlaces(factualRequest: FactualPlaceRequest, includeFacets: Boolean = true) = {
        val multiReq = new MultiRequest
        val query = buildQuery(factualRequest)

        multiReq.addQuery("places", "places", query)
        if (includeFacets)
            multiReq.addQuery("facets", "places/facets", buildFacetQuery(factualRequest))

        val res = factual.sendRequests(multiReq)
        val places = res.getData.get("places").asInstanceOf[ReadResponse]
        val facets = ?(res.getData.get("facets").asInstanceOf[FacetResponse])
        val categories = ?(facets.getData.get("category_ids").map {
            case (k, v) => getCategoryFromId(k) -> v
        })
        ?(facets.getData.put("category", categories))
        val placesResponse = FactualPlaceResponse(places, facets)
        placesResponse
    }

    def getFactualPlacesById(factualIds: Seq[String]) = {
        val idGroups = factualIds.grouped(3).toList
        idGroups.map{ idGroup =>
            val multiReq = new MultiRequest
            idGroup.foreach {
                factualId =>
                    val query = new FactualQuery
                    query.field("factual_id").isEqual(factualId)
                    multiReq.addQuery(factualId, "places", query)
            }
            factual.sendRequests(multiReq).getData
        }.flatten.toMap
    }

    def getFactualPlaceById(factualId: String) = {
        val query = new FactualQuery
        query.field("factual_id").isEqual(factualId)
        factual.fetch("places", query)
    }

    private def buildQuery(factualRequest: FactualPlaceRequest) = {
        val query = new FactualQuery()
                .includeRowCount()
        factualRequest.query.map(query.search(_))
        factualRequest.geo.map(geo => query.within(new Circle(geo.lat, geo.lng, geo.radius)))
        factualRequest.filter.map {
            filter =>
                filter.region.map(query.field("region").inList(_))
                filter.locality.map(query.field("locality").inList(_))
                filter.country.map(query.field("country").inList(_))
                filter.category.map(category => {
                    val categoryIds = category.map(getFactualCategoryIds(_)).toSet.toList.flatten
                    query.field("category_ids").inList(categoryIds)
                })
        }
        factualRequest.limit.map(query.limit(_))
        factualRequest.offset.map(query.offset(_))
        query
    }

    private def buildFacetQuery(factualRequest: FactualPlaceRequest) = {
        val query = new FacetQuery("country", "region", "locality", "category_ids")
                .includeRowCount()
                .minCountPerFacetValue(1)
                .maxValuesPerFacet(250)
        factualRequest.query.map(query.search(_))
        factualRequest.geo.map(geo => query.within(new Circle(geo.lat, geo.lng, geo.radius)))
        factualRequest.filter.map {
            filter =>
                filter.region.map(query.field("region").inList(_))
                filter.locality.map(query.field("locality").inList(_))
                filter.country.map(query.field("country").inList(_))
                filter.category.map(category => {
                    val categoryIds = category.map(getFactualCategoryIds(_)).toSet.toList.flatten
                    query.field("category_ids").inList(categoryIds)
                })
        }
        query
    }

    def getFactualCategoryIds(term: String) = {
        val query = new FactualQuery()
                .search(term)
        factual.fetch("places-categories", query).getData.map(_.get("category_id")).toList
    }

    def getCategoryFromId(id: String) = {
        ?(FactualService.Categories.get(id).get("labels").get("en").textValue())
    }
}

object FactualService {
    lazy val Categories = {
        val source = io.Source.fromURL(classOf[FactualService].getResource("/factual/categories/factual_taxonomy.json"))
        val categories = source.mkString
        source.close()
        val om = new ObjectMapper
        om.readValue(categories, classOf[JsonNode])
    }
}