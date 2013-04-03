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
    val visitsPerVisitor = JSON.parse( """{ $group : { _id : { platform: "$platform", deviceId: "$deviceId", geofenceId: "$geofenceId" } , "visitsPerVisitor" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]
    val visitsPerVisitorAvg = JSON.parse( """{ $group : { _id : "$geofenceId", "visitsPerVisitorMin" : { $min : "$visitsPerVisitor"}, "visitsPerVisitorMax" : { $max : "$visitsPerVisitor"}, "visitsPerVisitorAvg" : { $avg : "$visitsPerVisitor"}}}""").asInstanceOf[BasicDBObject]
    val dwellTime = JSON.parse( """{ $project : { _id: 1, geofenceId: 1, dwellTime: { $subtract: [ "$exiting", "$entering" ] } }}""").asInstanceOf[BasicDBObject]
    val dwellTimeAvg = JSON.parse( """{ $group : { _id : "$geofenceId", dwellTimeMin : { $min : "$dwellTime" }, dwellTimeMax : { $max : "$dwellTime" }, dwellTimeAvg : { $avg : "$dwellTime" } } }""").asInstanceOf[BasicDBObject]
    val visitsPerHourOfDay = JSON.parse( """{ $group : { _id : { visitId: "$_id", geofenceId: "$geofenceId", hourOfDay: { $hour : "$entering" } } , "visitsPerHourOfDay" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]
    val visitorsPerHourOfDay = JSON.parse( """{ $group : { _id : { geofenceId: "$geofenceId", hourOfDay: { $hour : "$entering" } } , "visitorsPerHourOfDay" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]

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
        geofenceEventDao.aggregate(visitsPerVisitor, visitsPerVisitorAvg)

}
