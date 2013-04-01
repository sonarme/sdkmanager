package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import me.sonar.sdkmanager.model._
import grizzled.slf4j.Logging
import javax.inject.Inject
import me.sonar.sdkmanager.core.CampaignService
import me.sonar.sdkmanager.model.SyncRequest
import me.sonar.sdkmanager.model.SyncResponse

@Controller
@RequestMapping(value = Array("/"))
class SyncController extends Logging {
    @Inject
    var campaignService: CampaignService = _

    @RequestMapping(value = Array("/"), method = Array(RequestMethod.HEAD, RequestMethod.GET))
    @ResponseBody
    def ping(): String = ""

    @RequestMapping(value = Array("/save"), method = Array(RequestMethod.POST))
    @ResponseBody
    def save() {
        val fence = StaticGeoFence(lat = 40.7453940, lng = -73.9838360, radius = 800, entering = true)
        fence.id = "testfence"
        fence.processRole = true
        fence.publish = true
        campaignService.save(Campaign(id = "test", appId = "testApp", triggers = Seq(
            fence),
            rule = Rule(actions = Seq(
                MessageAction(text = "Hello")))))
    }

    @RequestMapping(value = Array("/sync"), method = Array(RequestMethod.POST))
    @ResponseBody
    def sync(@RequestHeader("X-Sonar-ApiKey") apiKey: String, @RequestBody syncRequest: SyncRequest): SyncResponse = {
        info("api: " + syncRequest.clientVersion)
        val campaigns = campaignService.findByAppId(apiKey)
        SyncResponse(
            campaigns = campaigns)
    }

}
