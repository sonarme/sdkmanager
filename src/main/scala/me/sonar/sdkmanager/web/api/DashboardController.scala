package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import grizzled.slf4j.Logging
import org.springframework.web.bind.annotation._
import scala.Array
import me.sonar.sdkmanager.model.api.{GeofenceListRequest, GeofenceListsResponse}
import me.sonar.sdkmanager.model.db.{DB, GeofenceList}
import org.springframework.beans.factory.annotation.Autowired
import me.sonar.sdkmanager.core.AggregationService

@Controller
class DashboardController extends Logging with DB {

    import profile.simple._

    @Autowired
    var aggregationService: AggregationService = _

    @RequestMapping(value = Array("/geofencelists/{appId}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def geofenceLists(@PathVariable("appId") appId: String) = db.withTransaction {
        implicit session: Session =>
        // TODO: security
            GeofenceListsResponse((for (g <- GeofenceLists if g.appId === appId) yield g).list())
    }


    @RequestMapping(value = Array("/geofencelist/{id}"), method = Array(RequestMethod.GET))
    @RequestBody
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
    def countStats(@RequestParam("type") `type`: String,
                   @RequestParam("agg") agg: String,
                   @RequestParam("group") group: String,
                   @RequestParam("geofenceListId") geofenceListId: String,
                   @RequestParam("appId") appId: String) = db.withTransaction {
        implicit session: Session =>
        // TODO: security etc.
            `type` match {
                case "dwellTime" =>
                    aggregationService.aggregateDwellTime(appId, geofenceListId)
                case "visits" => aggregationService.aggregateVisits(appId, geofenceListId)
                case "visitsPerVisitor" => aggregationService.aggregateVisitsPerVisitor(appId, geofenceListId)
            }
    }

    @RequestMapping(value = Array("analytics/timeStats"), method = Array(RequestMethod.GET))
    @ResponseBody
    def timeStats(@RequestParam("type") `type`: String,
                  @RequestParam("appId") appId: String) = db.withTransaction {
        implicit session: Session =>
        // TODO: security etc.
            `type` match {
                case "visitors" => aggregationService.aggregateVisitorsPerHourOfDay(appId)
                case "visits" => aggregationService.aggregateVisitsPerHourOfDay(appId)
            }
    }
}