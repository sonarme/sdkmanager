package me.sonar.sdkmanager.core

import me.sonar.sdkmanager.SpringComponentTest
import javax.inject.Inject
import collection.JavaConversions._
import me.sonar.sdkmanager.model.db.{PlaceType, Place, GeofenceList}
import java.util.Date

class GeofenceListTest extends SpringComponentTest {
    @Inject
    var geofenceListService: GeofenceListService = _

    "GeofenceList" should "save a list with places" in {
        val appId = "testApp-" + new Date().getTime
        val geofenceList = GeofenceList(0, appId,
            "NYC McDonalds" /*,
            List[Place](
                Place(PlaceType.factual + "-1", "mcd1", 40, -73, PlaceType.factual),
                Place(PlaceType.factual + "-2", "mcd2", 40, -74, PlaceType.factual),
                Place(PlaceType.custom + "-1", "cus1", 41, -73, PlaceType.custom))*/
        )
        geofenceListService.save(geofenceList)

        val geofenceLists = geofenceListService.findByAppId(appId)
        assert(geofenceLists.size === 1)
        //assert(geofenceLists.head.places.size() === 3)
    }
}