package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import grizzled.slf4j.Logging
import javax.inject.Inject
import me.sonar.sdkmanager.core.AppMetadataService
import org.springframework.web.bind.annotation._
import scala.beans.BeanProperty
import collection.JavaConversions._
import me.sonar.sdkmanager.model.Platform
import me.sonar.sdkmanager.model.api.AppMetadataRequest

@Controller
class AppMetadataController extends Logging {
    @Inject
    var appMetadataService: AppMetadataService = _

    @RequestMapping(value = Array("/appmeta"), method = Array(RequestMethod.POST))
    @ResponseBody
    def appmetas(@RequestHeader("X-Sonar-Platform") platform: Platform,
                 @RequestBody appMetadataRequest: AppMetadataRequest) = {
        appMetadataService.getAppMetadatas(appMetadataRequest.keys, platform)
    }
}