package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.model.db.{ProfileAttributes, ProfileAttributesDao, GeofenceEventDao}
import me.sonar.sdkmanager.model._
import com.mongodb.{DBObject, BasicDBObject}
import org.bson.BasicBSONDecoder
import com.mongodb.util.JSON
import org.scala_tools.time.Imports._
import me.sonar.sdkmanager.model.api.SyncRequest
import collection.JavaConversions._
import ch.hsr.geohash.{WGS84Point, GeoHash}
import com.factual.driver.{ReadResponse, Point, Geopulse, Factual}
import collection.JavaConversions._

@Service
class SyncService {
    @Inject
    var geofenceEventDao: GeofenceEventDao = _
    @Inject
    var profileAttributesDao: ProfileAttributesDao = _
    @Inject
    var factual: Factual = _
    val decoder = new BasicBSONDecoder

    def appIdFilter(appId: String) = JSON.parse( s"""{       $$match : { appId : "$appId" }}""").asInstanceOf[BasicDBObject]

    val visitsPerVisitor = JSON.parse( """{ $group : { _id : { deviceId: "$deviceId", geofenceId: "$geofenceId" } , "visitsPerVisitor" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]
    val visitsPerVisitorAvg = JSON.parse( """{ $group : { _id : "$_id.geofenceId", "visitsPerVisitorMin" : { $min : "$visitsPerVisitor"}, "visitsPerVisitorMax" : { $max : "$visitsPerVisitor"}, "visitsPerVisitorAvg" : { $avg : "$visitsPerVisitor"}}}""").asInstanceOf[BasicDBObject]
    val dwellTime = JSON.parse( """{ $project : { _id: 1, geofenceId: 1, dwellTime: { $subtract: [ "$exiting", "$entering" ] } }}""").asInstanceOf[BasicDBObject]
    val dwellTimeAvg = JSON.parse( """{ $group : { _id : "$geofenceId", dwellTimeMin : { $min : "$dwellTime" }, dwellTimeMax : { $max : "$dwellTime" }, dwellTimeAvg : { $avg : "$dwellTime" } } }""").asInstanceOf[BasicDBObject]
    val visitors = JSON.parse( """{ $group : { _id : { deviceId: "$deviceId", geofenceId: "$geofenceId", hourOfDay: { $hour : "$entering" } } }}""").asInstanceOf[BasicDBObject]
    val visitorsPerHourOfDay = JSON.parse( """{ $group : { _id : { geofenceId: "$_id.geofenceId", hourOfDay: "$_id.hourOfDay"  } , "visitorsPerHourOfDay" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]
    val visitsPerHourOfDay = JSON.parse( """{ $group : { _id : { geofenceId: "$geofenceId", hourOfDay: { $hour : "$entering" } } , "visitsPerHourOfDay" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]

    def save(platform: String, deviceId: String, appId: String, syncRequest: SyncRequest) = {
        val compositeDeviceId = platform + "-" + deviceId
        if (syncRequest.events != null) {
            val geofenceEvents = syncRequest.events.collect {
                case ge: api.GeofenceEvent => ge
            }
            geofenceEventDao.saveMultiple(geofenceEvents.map {
                ge =>
                    db.GeofenceEvent(id = ge.id, appId = appId, platform = platform, deviceId = compositeDeviceId, geofenceId = ge.geofenceId, lat = ge.lat, lng = ge.lng, entering = ge.entering.orNull, exiting = ge.exiting.orNull)
            })
        }
        if (syncRequest.profileAttributes != null) {
            // TODO: this should happen on another schedule, not on sync
            syncRequest.profileAttributes.get("work") foreach {
                workGeo => val centerPoint = GeoHash.fromGeohashString(workGeo).getBoundingBoxCenterPoint
                val geopulse = factual.geopulse(new Geopulse(new Point(centerPoint.getLatitude, centerPoint.getLongitude)))
                geopulse.getData
            }
            val mergedAttributes: ProfileAttributes = profileAttributesDao.mergeUpsert(ProfileAttributes(appId = appId, deviceId = compositeDeviceId, syncRequest.profileAttributes))
            syncRequest.profileAttributes = mergedAttributes.attributes.toMap
        }
        syncRequest
    }

    implicit class CountAggregator(it: Iterable[Map[String, Any]]) {
        def results(primaryKey: String, secondaryNumKey: String, valueKey: String) = it.map {
            g =>
                val id = g("_id").asInstanceOf[DBObject]
                val geofenceId = id.get(primaryKey).toString
                val hourOfDay = id.get(secondaryNumKey).asInstanceOf[java.lang.Integer].intValue()
                val visits = g(valueKey).asInstanceOf[java.lang.Integer].intValue()
                (geofenceId, (hourOfDay, visits))
        }.groupBy(_._1).mapValues(_.map(_._2).toMap)
    }

    implicit class StatsAggregator(it: Iterable[Map[String, Any]]) {
        def results(primaryKey: String, baseName: String) = it.flatMap {
            g =>
                val gMin = g(baseName + "Min")
                if (gMin == null) None
                else {
                    val geofenceId = g("_id").toString
                    val min = gMin.asInstanceOf[java.lang.Number].longValue()
                    val max = g(baseName + "Max").asInstanceOf[java.lang.Number].longValue()
                    val avg = math.round(g(baseName + "Avg").asInstanceOf[java.lang.Double])
                    Some((geofenceId, CountStats(min = min, max = max, avg = avg)))
                }
        }.groupBy(_._1).mapValues(_.head._2)
    }

    def aggregateVisitsPerHourOfDay(appId: String): Map[String, Map[Int, Int]] =
        geofenceEventDao.aggregate(appIdFilter(appId), visitsPerHourOfDay).results("geofenceId", "hourOfDay", "visitsPerHourOfDay")

    def aggregateVisitorsPerHourOfDay(appId: String) =
        geofenceEventDao.aggregate(appIdFilter(appId), visitors, visitorsPerHourOfDay).results("geofenceId", "hourOfDay", "visitorsPerHourOfDay")

    def aggregateDwellTime(appId: String) =
        geofenceEventDao.aggregate(appIdFilter(appId), dwellTime, dwellTimeAvg).results("geofenceId", "dwellTime")

    def aggregateVisitsPerVisitor(appId: String) =
        geofenceEventDao.aggregate(appIdFilter(appId), visitsPerVisitor, visitsPerVisitorAvg).results("geofenceId", "visitsPerVisitor")

}

case class CountStats(min: Long, max: Long, avg: Long)
