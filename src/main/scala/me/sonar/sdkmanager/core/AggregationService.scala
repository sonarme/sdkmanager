package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import me.sonar.sdkmanager.model.db.DB
import me.sonar.sdkmanager.web.api.{TimeGrouping, AggregationType}

@Service
class AggregationService extends DB {

    import profile.simple._

    def getAttributesOfType(`type`: String) = db withSession {
        implicit session: Session =>
            (for (pa <- ProfileAttributes if pa.`type` === `type`) yield pa).list()
    }

    def aggregateVisitsPerHourOfDay(appId: String): Map[String, Map[Int, Int]] = db withSession {
        implicit session: Session =>
            val filteredEventsWithDwellTime = (for {
                ge <- GeofenceEvents if ge.appId === appId
            } yield (ge.geofenceId, ge.exiting, dateAdd(Hour)(ge.exiting, 1)))
            println(filteredEventsWithDwellTime.list)
            null
    }

    /*  geofenceEventDao.aggregate(appIdFilter(appId), visitsPerHourOfDay).results("geofenceId", "hourOfDay", "visitsPerHourOfDay")*/

    def aggregateVisitorsPerHourOfDay(appId: String): Map[String, Map[Int, Int]] = null

    /*geofenceEventDao.aggregate(appIdFilter(appId), visitors, visitorsPerHourOfDay).results("geofenceId", "hourOfDay", "visitorsPerHourOfDay")*/

    def aggregateDwellTime(appId: String, geofenceListId: String, agg: AggregationType, group: TimeGrouping): Map[String, CountStats] =
        db withSession {
            implicit session: Session =>
                val filteredEventsWithDwellTime = (for {
                    ge <- GeofenceEvents if ge.appId === appId
                } yield (ge, unixTimestamp(ge.exiting) - unixTimestamp(ge.entering))).groupBy(_._1.geofenceId)
                val dwellTimeStats = filteredEventsWithDwellTime.map {
                    case (geofenceId, groupings) =>
                        val dwellTimes = groupings.map(_._2)
                        (geofenceId, dwellTimes.min.getOrElse(0L), dwellTimes.max.getOrElse(0L), dwellTimes.avg.getOrElse(0L))
                }

                val results = (for ((geofenceId, min, max, avg) <- dwellTimeStats.list()) yield geofenceId -> CountStats(min, max, avg)).toMap
                results
        }

    /*geofenceEventDao.aggregate(appIdFilter(appId), dwellTime, dwellTimeAvg).results("geofenceId", "dwellTime")*/
    def aggregateVisits(appId: String, geofenceListId: String, agg: AggregationType, group: TimeGrouping): Iterable[(Int, Long)] =
        db withSession {
            implicit session: Session =>
                val filteredEventsWithDwellTime = (for {
                    gfl <- GeofenceLists if gfl.appId === appId && gfl.name === geofenceListId
                    gfl2place <- GeofenceListsToPlaces if gfl2place.geofenceListId === gfl.id
                    ge <- GeofenceEvents if ge.appId === appId && gfl2place.placeId === ge.geofenceId
                } yield (unixTimestamp(ge.exiting) - unixTimestamp(ge.entering), hour(ge.entering)))
                val result = filteredEventsWithDwellTime.groupBy(_._2).map {
                    case (absHour, groupings) =>
                        val dwellTimes = groupings.map(_._1)
                        val aggregated = agg match {
                            case AggregationType.average => dwellTimes.avg
                            case AggregationType.total => dwellTimes.sum
                        }
                        (absHour, aggregated.getOrElse(0L))
                }
                result.list()
        }

    def aggregateVisitsPerVisitor(appId: String, geofenceListId: String): Map[String, CountStats] = db withSession {
        implicit session: Session =>
            val filteredEventsPerVisitor = (for {
                ge <- GeofenceEvents if ge.appId === appId
            } yield (ge.geofenceId, ge.deviceId)).groupBy(identity)
            val visitsPerGeofenceAndVisitor = filteredEventsPerVisitor.map {
                case ((geofenceId, deviceId), visits) =>
                    (geofenceId, visits.length)
            }

            val visitsPerGeofence = visitsPerGeofenceAndVisitor.groupBy(_._1).map {
                case (geofenceId, groupings) =>
                    val visitCounts = groupings.map(_._2)
                    (geofenceId, visitCounts.min.getOrElse(0), visitCounts.max.getOrElse(0), visitCounts.avg.getOrElse(0))
            }
            val results = (for ((geofenceId, min, max, avg) <- visitsPerGeofence.list()) yield geofenceId -> CountStats(min, max, avg)).toMap
            results
    }

}