package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api._
import me.sonar.sdkmanager.model.api.FactualPlaceRequest
import me.sonar.sdkmanager.model.api.FactualGeo
import scala.collection.JavaConversions._
import org.json.JSONArray
import com.fasterxml.jackson.databind.ObjectMapper
import me.sonar.sdkmanager.web.api.RestObjectMapper
import org.apache.commons.io.FileUtils
import java.io.File

class FactualTest extends SpringComponentTest {
    @Inject
    var factualService: FactualService = _

    "factual" should "return data" in {

        val data = factualService.getFactualData("dr5rsx47")

        assert(data != null)
    }

    "factual" should "return category ids" in {
        val categoryIds = factualService.getFactualCategoryIds("bar")
        assert(categoryIds.size() > 0)
    }

    "factual" should "return places data" in {
        val factualRequest = new FactualPlaceRequest(query = Option("sushi"))
        val sushi = factualService.getFactualPlaces(factualRequest)
        assert(sushi.places.getData != null)
        assert(sushi.places.getData.head.get("name").toString.toLowerCase.indexOf("sushi") > -1)

        val request2 = new FactualPlaceRequest(query = Option("sushi"), geo = Option(FactualGeo(40.745396, -73.983661, 1000)))
        val nycsushi = factualService.getFactualPlaces(request2)
        assert(nycsushi.places.getData != null)
        assert(nycsushi.places.getData.head.get("locality").toString.equals("New York"))

        val request3 = new FactualPlaceRequest(query = Option("burger"), filter = Option(FactualFilter(region = Option(List("MA", "NH")))))
        val manhburger = factualService.getFactualPlaces(request3)
        assert(manhburger.places.getData != null)
        assert(List("MA", "NH").contains(manhburger.places.getData.head.get("region")))
        assert(manhburger.facets.getData != null)

        val request4 = new FactualPlaceRequest(query = None, filter = Option(FactualFilter(category = Option(List("social", "bar")), country = Option(List("US", "CA")))))
        val socialAndBar = factualService.getFactualPlaces(request4)
        assert(socialAndBar.places.getData != null)

        val category = socialAndBar.places.getData.get(0).get("category_labels").asInstanceOf[JSONArray].get(0).asInstanceOf[JSONArray].get(0).toString.toLowerCase
        assert(List("social", "bar").contains(category))
    }

    "factualService" should "return readable categories from ids" in {
        val category = factualService.getCategoryFromId("2")
        assert(category === "Automotive")
        val cat2 = factualService.getCategoryFromId("385")
        assert(cat2 === "Gyms and Fitness Centers")
    }
    /*
    "factualService" should "return all Walmart locations" in {
        val places = List("MA", "CT", "NY", "NJ", "FL", "CA", "KS", "VA", "WA").map {
            region => {
                val factualRequest = new FactualPlaceRequest(query = Option("walmart"), filter = Option(FactualFilter(country = Option(List("US")), region = Option(List(region)), category = Option(List("Department Stores")))), limit = Option(50))
                getPlaces(region, factualRequest)
            }
        }.toMap

        val mapper = new RestObjectMapper()
        val json = mapper.writeValueAsString(places)
        FileUtils.writeStringToFile(new File("/Users/rogchang/Desktop/walmart.json"), json)
    }

    "factualService" should "return all Target locations" in {
        val places = List("MA", "CT", "NY", "NJ", "FL", "CA", "KS", "VA", "WA").map {
            region => {
                val factualRequest = new FactualPlaceRequest(query = Option("target"), filter = Option(FactualFilter(country = Option(List("US")), region = Option(List(region)), category = Option(List("Department Stores")))), limit = Option(50))
                getPlaces(region, factualRequest)
            }
        }.toMap

        val mapper = new RestObjectMapper()
        val json = mapper.writeValueAsString(places)
        FileUtils.writeStringToFile(new File("/Users/rogchang/Desktop/target.json"), json)
    }

    "factualService" should "return bars" in {
        val places = List(("NY", "New York"), ("CA", "San Francisco"), ("IL", "Chicago"), ("FL", "Miami"), ("MO", "Kansas City")).map {
            case (region, locality) => {
                val factualRequest = new FactualPlaceRequest(query = Option("bar"), filter = Option(FactualFilter(country = Option(List("US")), region = Option(List(region)), locality = Option(List(locality)), category = Option(List("bars")))), limit = Option(50))
                getPlaces(region, factualRequest)
            }
        }.toMap
        val mapper = new RestObjectMapper()
        val json = mapper.writeValueAsString(places)
        FileUtils.writeStringToFile(new File("/Users/rogchang/Desktop/bars.json"), json)
    }

    "factualService" should "return schools" in {
        val places = List(("NY", "New York"), ("CA", "San Francisco"), ("IL", "Chicago"), ("FL", "Miami"), ("MO", "Kansas City")).map {
            case (region, locality) => {
                val factualRequest = new FactualPlaceRequest(query = Option("school"), filter = Option(FactualFilter(country = Option(List("US")), region = Option(List(region)), locality = Option(List(locality)), category = Option(List("Primary and Secondary Schools")))), limit = Option(50))
                getPlaces(region, factualRequest)
            }
        }.toMap
        val mapper = new RestObjectMapper()
        val json = mapper.writeValueAsString(places)
        FileUtils.writeStringToFile(new File("/Users/rogchang/Desktop/schools.json"), json)
    }

    implicit class condensedDataMapper(list: java.util.List[java.util.Map[String, AnyRef]]) {
        def toCondensedDataList = list.map {
            d => CondensedFactualData(d.get("factual_id").toString, d.get("latitude").asInstanceOf[Double], d.get("longitude").asInstanceOf[Double])
        }.toSeq
    }

    private def getPlaces(region: String, factualRequest: FactualPlaceRequest) = {
        val locations = factualService.getFactualPlaces(factualRequest, includeFacets = false)
        val factualData = locations.places.getData.toCondensedDataList

        val iterations = (locations.places.getTotalRowCount / 50).toInt
        val more = (1 to (List(iterations, 9)).min).map { //max of 10 iterations ... this is a factual limitation
            it =>
                factualRequest.offset = Option(it * 50)
                factualService.getFactualPlaces(factualRequest, includeFacets = false).places.getData.toCondensedDataList
        }.toSeq.flatten
        (region, (factualData ++ more))
    }
    */

}

case class CondensedFactualData(id: String, lat: Double, lng: Double)