package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.model.db._
import me.sonar.sdkmanager.model._
import com.mongodb.{DBCollection, DBObject, BasicDBObject}
import org.bson.BasicBSONDecoder
import com.mongodb.util.JSON
import org.scala_tools.time.Imports._
import me.sonar.sdkmanager.model.api.SyncRequest
import collection.JavaConversions._
import ch.hsr.geohash.{WGS84Point, GeoHash}
import com.factual.driver.{ReadResponse, Point, Geopulse, Factual}
import collection.JavaConversions._
import org.springframework.data.mongodb.core.{CollectionCallback, MongoOperations}
import java.util.UUID
import me.sonar.sdkmanager.core.CountStats
import me.sonar.sdkmanager.model.api.SyncRequest
import me.sonar.sdkmanager.model.db.ProfileAttributes
import me.sonar.sdkmanager.model.db.ProfileAttribute
import scala.Some

@Service
class SyncService {
    @Inject
    var geofenceEventDao: GeofenceEventDao = _
    @Inject
    var profileAttributesDao: ProfileAttributesDao = _
    @Inject
    var factualService: FactualService = _
    @Inject
    var mongoOperations: MongoOperations = _
    @Inject
    var appMetadataService: AppMetadataService = _

    val decoder = new BasicBSONDecoder

    def appIdFilter(appId: String) = JSON.parse( s"""{                 $$match : { appId : "$appId" }}""").asInstanceOf[BasicDBObject]

    val visitsPerVisitor = JSON.parse( """{ $group : { _id : { deviceId: "$deviceId", geofenceId: "$geofenceId" } , "visitsPerVisitor" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]
    val visitsPerVisitorAvg = JSON.parse( """{ $group : { _id : "$_id.geofenceId", "visitsPerVisitorMin" : { $min : "$visitsPerVisitor"}, "visitsPerVisitorMax" : { $max : "$visitsPerVisitor"}, "visitsPerVisitorAvg" : { $avg : "$visitsPerVisitor"}}}""").asInstanceOf[BasicDBObject]
    val dwellTime = JSON.parse( """{ $project : { _id: 1, geofenceId: 1, dwellTime: { $subtract: [ "$exiting", "$entering" ] } }}""").asInstanceOf[BasicDBObject]
    val dwellTimeAvg = JSON.parse( """{ $group : { _id : "$geofenceId", dwellTimeMin : { $min : "$dwellTime" }, dwellTimeMax : { $max : "$dwellTime" }, dwellTimeAvg : { $avg : "$dwellTime" } } }""").asInstanceOf[BasicDBObject]
    val visitors = JSON.parse( """{ $group : { _id : { deviceId: "$deviceId", geofenceId: "$geofenceId", hourOfDay: { $hour : "$entering" } } }}""").asInstanceOf[BasicDBObject]
    val visitorsPerHourOfDay = JSON.parse( """{ $group : { _id : { geofenceId: "$_id.geofenceId", hourOfDay: "$_id.hourOfDay"  } , "visitorsPerHourOfDay" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]
    val visitsPerHourOfDay = JSON.parse( """{ $group : { _id : { geofenceId: "$geofenceId", hourOfDay: { $hour : "$entering" } } , "visitsPerHourOfDay" : { $sum : 1}}}""").asInstanceOf[BasicDBObject]

    val visitorsGeofencesProject = JSON.parse( """{ $project : { geofenceIds: "$value.geofenceIds", attributes: "$value.attributes" } }""").asInstanceOf[BasicDBObject]
    val visitorsGeofencesUnwind1 = JSON.parse( """{ $unwind : "$geofenceIds" }""").asInstanceOf[BasicDBObject]
    val visitorsGeofencesUnwind2 = JSON.parse( """{ $unwind : "$attributes" }""").asInstanceOf[BasicDBObject]
    val visitorsGeofencesGroup = JSON.parse( """{ $group : { _id: { geofenceId: "$geofenceIds", attributeKey: "$attributes._id", attributeValue: "$attributes.value" }, "weight": { $sum: "$attributes.probability"} } }""").asInstanceOf[BasicDBObject]

    def save(platform: Platform, deviceId: String, appId: String, syncRequest: SyncRequest) = {
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
            val factualData = syncRequest.profileAttributes.find(_.`type` == "home") match {
                case Some(profileAttribute) =>
                    factualService.getFactualData(profileAttribute.value)
                case _ => Seq.empty[ProfileAttribute]
            }
            val appCategories = syncRequest.profileAttributes.find(_.`type` == "installed_apps") match {
                case Some(profileAttribute) =>
                    appMetadataService.getAppCategories(profileAttribute.value.split(","), platform)
                case _ => Seq.empty[ProfileAttribute]
            }

            val profileAttributes = ProfileAttributes(appId = appId, deviceId = compositeDeviceId, factualData ++ appCategories ++ syncRequest.profileAttributes.toSeq)
            profileAttributesDao.removeAttributesWithType(profileAttributes.id, "category")
            val mergedAttributes: ProfileAttributes = profileAttributesDao.mergeUpsert(profileAttributes)
            syncRequest.profileAttributes = mergedAttributes.attributes
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

    case class GeofenceAttribute(geofenceId: String, attributeKeyValue: (String, String), weight: Double)

    def aggregateGeofenceData(appId: String) = {
        val tempName = "sdk_temp_aggregation_" + UUID.randomUUID().toString
        mongoOperations.createCollection(tempName)
        val result = try {
            geofenceEventDao.aggregateGeofences(appId, tempName)
            profileAttributesDao.aggregateGeofences(appId, tempName)
            mongoOperations.execute(tempName, new CollectionCallback[Iterable[GeofenceAttribute]] {
                def doInCollection(collection: DBCollection) = {
                    collection.aggregate(visitorsGeofencesProject, visitorsGeofencesUnwind1, visitorsGeofencesUnwind2, visitorsGeofencesGroup).results.flatMap {
                        dbo =>
                            val id = dbo.get("_id").asInstanceOf[DBObject]
                            val geofenceId = id.get("geofenceId").toString
                            val attributeKey = id.get("attributeKey").toString
                            val attributeValue = id.get("attributeValue").toString
                            val weight = try { dbo.get("weight").asInstanceOf[Double] } catch { case c: ClassCastException => dbo.get("weight").asInstanceOf[Int]}
                            // TODO: hack
                            if (attributeKey == "work" || attributeKey == "home") None
                            else Some(GeofenceAttribute(geofenceId, (attributeKey, attributeValue), weight))
                    }
                }
            })
        } finally {
            mongoOperations.dropCollection(tempName)
        }
        // TODO: hacky
        result.groupBy(_.geofenceId).mapValues {
            x =>
                val totals = x.groupBy(_.attributeKeyValue._1).mapValues(_.map(_.weight).sum)
                x.groupBy(_.attributeKeyValue).mapValues(x => x.head.weight / totals(x.head.attributeKeyValue._1))
        }
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
