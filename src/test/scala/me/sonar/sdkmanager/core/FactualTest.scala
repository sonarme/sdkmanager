package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api._
import me.sonar.sdkmanager.model.api.FactualPlaceRequest
import me.sonar.sdkmanager.model.api.FactualGeo
import scala.collection.JavaConversions._
import org.json.JSONArray

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
        assert(sushi.data != null)
        assert(sushi.data.head.get("name").toString.toLowerCase.indexOf("sushi") > -1)

        val request2 = new FactualPlaceRequest(query = Option("sushi"), geo = Option(FactualGeo(40.745396, -73.983661, 1000)))
        val nycsushi = factualService.getFactualPlaces(request2)
        assert(nycsushi.data != null)
        assert(nycsushi.data.head.get("locality").toString.equals("New York"))

        val request3 = new FactualPlaceRequest(query = Option("burger"), filter = Option(FactualFilter(region = Option(List("MA", "NH")))))
        val manhburger = factualService.getFactualPlaces(request3)
        assert(manhburger.data != null)
        assert(List("MA", "NH").contains(manhburger.data.head.get("region")))

        val request4 = new FactualPlaceRequest(query = None, filter = Option(FactualFilter(category = Option(List("social", "bar")), country = Option(List("US","CA")))))
        val socialAndBar = factualService.getFactualPlaces(request4)
        assert(socialAndBar.data != null)

        val category = socialAndBar.data.get(0).get("category_labels").asInstanceOf[JSONArray].get(0).asInstanceOf[JSONArray].get(0).toString.toLowerCase
        assert(List("social", "bar").contains(category))
    }
}
