package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import me.sonar.sdkmanager.model._
import api.SyncRequest
import api.SyncResponse
import grizzled.slf4j.Logging
import javax.inject.Inject
import me.sonar.sdkmanager.core.{SyncService, CampaignService}
import scala.collection.mutable.ListBuffer

@Controller
class SyncController extends Logging {
    @Inject
    var campaignService: CampaignService = _
    @Inject
    var syncService: SyncService = _

    val campaignsList = ListBuffer[java.util.Map[String, Any]]()

    @RequestMapping(value = Array("/"), method = Array(RequestMethod.HEAD, RequestMethod.GET))
    @ResponseBody
    def ping(): String = ""

    @RequestMapping(value = Array("/campaigns"), method = Array(RequestMethod.POST))
    @ResponseBody
    def createCampaign(@RequestBody body: java.util.Map[String, Any]) = {
        info(s"POST $body")
        campaignsList += body
        ""
    }

    @RequestMapping(value = Array("/campaigns"), method = Array(RequestMethod.GET))
    @ResponseBody
    def campaigns = campaignsList

    @RequestMapping(value = Array("/sync"), method = Array(RequestMethod.POST))
    @ResponseBody
    def sync(@RequestHeader("X-Sonar-ApiKey") apiKey: String,
             @RequestHeader("X-Sonar-Platform") platform: Platform,
             @RequestHeader("X-Sonar-DeviceId") deviceId: String,
             @RequestBody syncRequest: SyncRequest): SyncResponse = {
        info("api: " + syncRequest.clientVersion)
        val app = campaignService.findAppByApiKey(apiKey).getOrElse(throw new RuntimeException("App not found"))
        val savedRequest: SyncRequest = syncService.save(platform, deviceId, app.id, syncRequest)
        val campaigns = campaignService.findByAppId(app.id)
        SyncResponse(
            campaigns = campaigns, profileAttributes = savedRequest.profileAttributes)
    }


}
