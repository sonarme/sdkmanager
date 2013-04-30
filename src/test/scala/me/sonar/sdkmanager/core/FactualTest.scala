package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import me.sonar.sdkmanager.model.api._
import me.sonar.sdkmanager.model.api.FactualRequest
import me.sonar.sdkmanager.model.api.FactualGeo

class FactualTest extends SpringComponentTest {
    @Inject
    var factualService: FactualService = _

    "factual" should "return data" in {

        val data = factualService.getFactualData("dr5rsx47")

        assert(data != null)
    }

    "factual" should "return category ids" in {
        val categoryIds = factualService.getFactualCategoryIds("bar")
        assert(categoryIds.getData.size() > 0)
    }

    "factual" should "return places data" in {
        val factualRequest = new FactualRequest(query = "sushi")
        val sushi = factualService.getFactualPlaces(factualRequest)
        assert(sushi.getData != null)

        val request2 = new FactualRequest(query = "sushi", geo = Option(FactualGeo(40.745396, -73.983661, 1000)))
        val nycsushi = factualService.getFactualPlaces(request2)
        assert(nycsushi.getData != null)

        val request3 = new FactualRequest(query = "burger", filter = Option(FactualFilter(region = Option(List("MA", "NH")))))
        val manhburger = factualService.getFactualPlaces(request3)
        assert(manhburger.getData != null)

        val request4 = new FactualRequest(query = "", filter = Option(FactualFilter(category = Option(List("social", "bar")))))
        val socialAndBar = factualService.getFactualPlaces(request4)
        assert(socialAndBar.getData != null)
    }
}
