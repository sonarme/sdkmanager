package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.model.db.GeofenceEventDao
import me.sonar.sdkmanager.model._
import com.mongodb.BasicDBObject
import org.bson.BasicBSONDecoder
import com.mongodb.util.JSON

@Service
class SyncService {
    @Inject
    var geofenceEventDao: GeofenceEventDao = _
    val decoder = new BasicBSONDecoder
    val aggregate = JSON.parse( """{ "$group" : { "_id" : "$platform" , "visits" : { "$sum" : 1}}}""").asInstanceOf[BasicDBObject]

    def save(platform: String, deviceId: String, events: Iterable[api.PublicEvent]) = {
        val geofenceEvents = events.collect {
            case ge: api.GeofenceEvent => ge
        }
        geofenceEventDao.saveMultiple(geofenceEvents.map {
            ge =>
                db.GeofenceEvent(id = ge.id, platform = platform, deviceId = deviceId, geofenceId = ge.geofenceId, lat = ge.lat, lng = ge.lng, entering = ge.entering.orNull, exiting = ge.exiting.orNull)
        })

    }


    def computeAggregates() =
        geofenceEventDao.aggregate(aggregate)

}
