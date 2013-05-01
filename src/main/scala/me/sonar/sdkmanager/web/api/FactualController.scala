package me.sonar.sdkmanager.web.api

import org.springframework.stereotype.Controller
import grizzled.slf4j.Logging
import javax.inject.Inject
import me.sonar.sdkmanager.core.{FactualService}
import org.springframework.web.bind.annotation._
import scala.Array
import scala.collection.JavaConversions._
import me.sonar.sdkmanager.core.ScalaGoodies._
import me.sonar.sdkmanager.model.api.{FactualFilter, FactualGeo, FactualResponse, FactualRequest}

@Controller
class FactualController extends Logging {
    @Inject
    var factualService: FactualService = _

    @RequestMapping(value = Array("/factual"), method = Array(RequestMethod.GET))
    @ResponseBody
    def factual(@RequestParam(required = false, value = "query") query: String,
                @RequestParam(required = false, value = "lat") lat: java.lang.Double,
                @RequestParam(required = false, value = "lng") lng: java.lang.Double,
                @RequestParam(required = false, value = "radius") radius: java.lang.Integer,
                @RequestParam(required = false, value = "category") category: String,
                @RequestParam(required = false, value = "country") country: String,
                @RequestParam(required = false, value = "region") region: String,
                @RequestParam(required = false, value = "locality") locality: String,
                @RequestParam(required = false, value = "limit") limit: java.lang.Integer,
                @RequestParam(required = false, value = "offset") offset: java.lang.Integer) = {
        val geo = (optionDouble(lat), optionDouble(lng)) match {
            case (Some(latitude), Some(longitude)) => Option(FactualGeo(latitude, longitude, optionInteger(radius).getOrElse(5000)))
            case _ => None
        }
        val categories = optionString(category)
        val countries = optionString(country)
        val regions = optionString(region)
        val localities = optionString(locality)
        val filter =
            if (categories.isDefined || regions.isDefined || localities.isDefined || countries.isDefined)
                Option(FactualFilter(categories.map(_.split(",")), regions.map(_.split(",")), localities.map(_.split(",")), countries.map(_.split(","))))
            else
                None

        val factualRequest = FactualRequest(Option(query), geo, filter, optionInteger(limit), optionInteger(offset))
        val data = factualService.getFactualPlaces(factualRequest).getData
        FactualResponse(data)
    }
}