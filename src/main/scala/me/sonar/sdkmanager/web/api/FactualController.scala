package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import grizzled.slf4j.Logging
import javax.inject.Inject
import me.sonar.sdkmanager.core.{FactualService}
import org.springframework.web.bind.annotation._
import scala.Array
import scala.collection.JavaConversions._
import me.sonar.sdkmanager.model.api.{FactualResponse, FactualRequest}

@Controller
class FactualController extends Logging {
    @Inject
    var factualService: FactualService = _

    @RequestMapping(value = Array("/factual"), method = Array(RequestMethod.POST))
    @ResponseBody
    def factual(@RequestBody factualRequest: FactualRequest) = {
        val data = factualService.getFactualPlaces(factualRequest).getData
        FactualResponse(data)
    }
}