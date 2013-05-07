package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import grizzled.slf4j.Logging
import org.springframework.web.bind.annotation._
import scala.Array
import javax.inject.Inject
import me.sonar.sdkmanager.core.GeofenceListService
import me.sonar.sdkmanager.model.api.{GeofenceListRequest, GeofenceListsResponse}
import me.sonar.sdkmanager.model.db.GeofenceList

@Controller
class DashboardController extends Logging {
    @Inject
    var geofenceListService: GeofenceListService = _

    @RequestMapping(value = Array("/geofencelists/{appId}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def geofenceLists(@PathVariable("appId") appId: String) = {
        GeofenceListsResponse(geofenceListService.findByAppId(appId))
    }


    @RequestMapping(value = Array("/geofencelist/{id}"), method = Array(RequestMethod.GET))
    @RequestBody
    def geofenceList(@PathVariable("id") id: String) = {
        ""
    }

    @RequestMapping(value = Array("/geofencelist"), method = Array(RequestMethod.POST))
    @ResponseBody
    def createGeofenceList(@RequestBody geofenceList: GeofenceListRequest) = {
        geofenceListService.save(GeofenceList(geofenceList.appId, geofenceList.name, geofenceList.places))
    }

    @RequestMapping(value = Array("geofencelist/{id}"), method = Array(RequestMethod.POST, RequestMethod.PUT))
    @ResponseBody
    def putGeofenceList(@PathVariable("id") id: String,
                        @RequestBody geofenceList: GeofenceListRequest) = {
        geofenceListService.findById(id) match {
            case Some(gfl) =>
                gfl.name = geofenceList.name
                gfl.appId = geofenceList.appId
                gfl.places = geofenceList.places
                geofenceListService.save(gfl)
            case None => throw new RuntimeException("Cannot find geofence list with id: " + id)
        }
    }
}