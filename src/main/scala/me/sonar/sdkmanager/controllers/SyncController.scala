package me.sonar.sdkmanager.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{ResponseBody, RequestBody, RequestMethod, RequestMapping}
import me.sonar.sdkmanager.model._
import grizzled.slf4j.Logging

@Controller
@RequestMapping(value = Array("/"))
class SyncController extends Logging {

    @RequestMapping(value = Array("/"), method = Array(RequestMethod.HEAD, RequestMethod.GET))
    @ResponseBody
    def ping(): String = ""

    @RequestMapping(value = Array("/sync"), method = Array(RequestMethod.POST))
    @ResponseBody
    def sync(@RequestBody syncRequest: SyncRequest): SyncResponse = {
        info("api: " + syncRequest.clientVersion)
        // sonar hq
        val fence = StaticGeoFence(lat = 40.7453940, lng = -73.9838360, radius = 800)
        fence.id = "testfence"
        fence.processRole = true
        SyncResponse(
            campaigns = Seq(
                Campaign(id = "test", triggers = Seq(
                    fence),
                    rule = Rule(actions = Seq(
                        MessageAction(text = "Hello"))))))
    }

}
