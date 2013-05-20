package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import grizzled.slf4j.Logging
import org.springframework.web.bind.annotation._
import scala.Array
import collection.JavaConversions._
import me.sonar.sdkmanager.model.api.{GeofenceListRequest, GeofenceListsResponse}
import me.sonar.sdkmanager.model.db.{DB, GeofenceList}
import org.springframework.beans.factory.annotation.Autowired
import me.sonar.sdkmanager.core.{FactualService, AggregationService}
import me.sonar.sdkmanager.core.ScalaGoodies._
import com.factual.driver.ReadResponse

@Controller
class DashboardController extends Logging with DB {

    import profile.simple._

    @Autowired
    var aggregationService: AggregationService = _
    @Autowired
    var factualService: FactualService = _

    @RequestMapping(value = Array("/geofencelists/{appId}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def geofenceLists(@PathVariable("appId") appId: String) = db.withTransaction {
        implicit session: Session =>
        // TODO: security
            GeofenceListsResponse((for (g <- GeofenceLists if g.appId === appId) yield g).list())
    }


    @RequestMapping(value = Array("/geofencelist/{id}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def geofenceList(@PathVariable("id") id: Long) = db.withTransaction {
        implicit session: Session =>
        // TODO: security
        // places...etc
            GeofenceLists.findById(id)
    }

    @RequestMapping(value = Array("/geofencelist"), method = Array(RequestMethod.POST))
    @ResponseBody
    def createGeofenceList(@RequestBody geofenceList: GeofenceListRequest) = db.withTransaction {
        implicit session: Session =>
            GeofenceLists.insert(GeofenceList(0, geofenceList.appId, geofenceList.name))
        // TODO: geofenceList.places
    }

    @RequestMapping(value = Array("geofencelist/{id}"), method = Array(RequestMethod.POST, RequestMethod.PUT))
    @ResponseBody
    def putGeofenceList(@PathVariable("id") id: Long,
                        @RequestBody geofenceList: GeofenceListRequest) = db.withTransaction {
        implicit session: Session =>
        // TODO: security etc.
            GeofenceLists.update(GeofenceList(id, geofenceList.appId, geofenceList.name))
    }


    @RequestMapping(value = Array("analytics/places"), method = Array(RequestMethod.GET))
    @ResponseBody
    def placesChart(@RequestParam("type") `type`: PlacesChartType,
                    @RequestParam("agg") agg: AggregationType,
                    @RequestParam("group") group: TimeGrouping,
                    @RequestParam("geofenceListId") geofenceListId: String,
                    @RequestParam("appId") appId: String,
                    @RequestParam("since") since: Long) = db.withTransaction {
        implicit session: Session =>
        // TODO: security etc.
            val geofenceListIdLong = if (geofenceListId == "all_places") 0L else geofenceListId.toLong
            val data = aggregationService.aggregateVisits(`type`, appId, geofenceListIdLong, agg, group, since)
            Map("entries" -> data)
    }

    @RequestMapping(value = Array("analytics/customers"), method = Array(RequestMethod.GET))
    @ResponseBody
    def customers(@RequestParam("appId") appId: String,
                  @RequestParam("geofenceListId") geofenceListId: String,
                  @RequestParam("type") attribute: String,
                  @RequestParam("since") since: Long) = db.withTransaction {
        implicit session: Session =>
        // TODO: security etc.
            val geofenceListIdLong = if (geofenceListId == "all_places") 0L else geofenceListId.toLong
            Map("terms" -> aggregationService.aggregateCustomers(appId, geofenceListIdLong, attribute))
    }

    @RequestMapping(value = Array("analytics/topPlaces"), method = Array(RequestMethod.GET))
    @ResponseBody
    def topPlaces(@RequestParam("appId") appId: String,
                  @RequestParam("geofenceListId") geofenceListId: String,
                  @RequestParam("since") since: Long,
                  @RequestParam(required = false, defaultValue = "5", value = "limit") limit: Int) = db.withTransaction {
        implicit session: Session =>
        //TODO: security etc.
            val geofenceListIdLong = if (geofenceListId == "all_places") 1L else geofenceListId.toLong
            val data = aggregationService.topPlaces(appId, geofenceListIdLong, since).take(limit)
            val factualIds = data.map(d => d.term.substring(d.term.indexOf("factual-") + 8))

            val factualPlaces = factualService.getFactualPlacesById(factualIds).asInstanceOf[Map[String,ReadResponse]]

            val res = data.map {
                d =>
                    val temp = factualPlaces.get(d.term.substring(d.term.indexOf("factual-") + 8)).orNull
                    val f = ?(temp.getData.head)
                    TopPlaces(d.term, ?(f.get("name").toString), ?(f.get("address").toString), ?(f.get("locality").toString), d.count, d.unique.toInt, d.dwell.toInt)
            }
            Map("list" -> res)

    }

}

case class TopPlaces(id: String, name: String, address: String, locality: String, total: Long, unique: Int, dwell: Int)