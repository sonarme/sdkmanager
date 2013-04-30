package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import grizzled.slf4j.Logging
import javax.inject.Inject
import me.sonar.sdkmanager.core.{FactualService}
import org.springframework.web.bind.annotation._
import scala.Array
import me.sonar.sdkmanager.model.api.FactualRequest

@Controller
class FactualController extends Logging {
    @Inject
    var factualService: FactualService = _

    @RequestMapping(value = Array("/factual"), method = Array(RequestMethod.POST))
    @ResponseBody
    def factual(@RequestBody factualRequest: FactualRequest) = {
        val response = factualService.getFactualPlaces(factualRequest)
        response.getData
    }
}