package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import me.sonar.sdkmanager.model.db.DB
import me.sonar.sdkmanager.web.api.{TimeGrouping, AggregationType}
import org.scala_tools.time.Imports._
import scala.slick.session.Database
import scala.slick.jdbc.{GetResult, StaticQuery => Q}

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

    case class AggregationResult(time: Long, count: Long)

    implicit val getSupplierResult = GetResult(r => AggregationResult(r.nextLong(), r.nextLong()))

    def aggregateVisits(appId: String, geofenceListId: String, agg: AggregationType, group: TimeGrouping) =
        db withSession {
            implicit session: Session =>
            // TODO: hacky
                val grouper = group match {
                    case TimeGrouping.hour => "UNIX_TIMESTAMP(ge.entering) / 60 / 60 * 60 * 60 * 1000"
                    case TimeGrouping.day => "UNIX_TIMESTAMP(DATE(ge.entering)) * 1000"
                    case TimeGrouping.month => "UNIX_TIMESTAMP(LAST_DAY(ge.entering) - INTERVAL 1 MONTH + INTERVAL 1 DAY) * 1000"
                    case TimeGrouping.timeOfDay => "HOUR(ge.entering)"
                    case TimeGrouping.week => "UNIX_TIMESTAMP(DATE_ADD(DATE(ge.entering), INTERVAL(1-DAYOFWEEK(ge.entering)) DAY)) * 1000"
                }

                val sql = """select """ + grouper + """ as grouper, avg(UNIX_TIMESTAMP(ge.exiting) - UNIX_TIMESTAMP(ge.entering)) from GeofenceLists gfl join GeofenceListsToPlaces gfl2place on gfl.id=gfl2place.geofenceListId join GeofenceEvents ge on gfl2place.placeId=ge.geofenceId and gfl.appId=ge.appId where gfl.appId=? and gfl.name=? group by grouper order by grouper desc limit 25"""

                Q.query[(String, String), AggregationResult](sql).list(appId, geofenceListId)


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