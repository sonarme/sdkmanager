package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import me.sonar.sdkmanager.model.db.DB
import me.sonar.sdkmanager.web.api.{PlacesChartType, TimeGrouping, AggregationType}
import org.scala_tools.time.Imports._
import scala.slick.session.Database
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import java.util.Date

@Service
class AggregationService extends DB {

    import profile.simple._

    def getAttributesOfType(`type`: String) = db withSession {
        implicit session: Session =>
            (for (pa <- ProfileAttributes if pa.`type` === `type`) yield pa).list()
    }

    case class AggregationResult(time: Long, count: Long)

    case class SegmentationResult(term: String, count: Long)

    implicit val getAggregationResult = GetResult(r => AggregationResult(r.nextLong(), r.nextLong()))
    implicit val getSegmentationResult = GetResult(r => SegmentationResult(r.nextString().replace('_', ' '), r.nextLong()))

    def aggregateCustomers(appId: String, geofenceListId: Long, attribute: String) = db withSession {
        implicit session: Session =>
            val sql = s"select pa.value, sum(pa.probability) as perVisitor from GeofenceLists gfl join GeofenceListsToPlaces gfl2place on gfl.id=gfl2place.geofenceListId join (select distinct appId, deviceId, geofenceId from GeofenceEvents) ge on gfl2place.placeId=ge.geofenceId and gfl.appId=ge.appId join ProfileAttributes pa on pa.deviceId=ge.deviceId where gfl.appId=? and (gfl.id=? or 0=?) and pa.`type`=? group by pa.`value`"

            Q.query[(String, Long, Long, String), SegmentationResult](sql).list(appId, geofenceListId, geofenceListId, attribute)

    }

    def aggregateVisits(chartType: PlacesChartType, appId: String, geofenceListId: Long, agg: AggregationType, group: TimeGrouping, since: Long) =
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
                val (aggregator, charter) = (agg, chartType) match {
                    case (AggregationType.average, PlacesChartType.visits) => ("avg", "count(*)")
                    case (AggregationType.total, PlacesChartType.visits) => ("sum", "count(*)")
                    case (AggregationType.unique, PlacesChartType.visits) => ("sum", "1")
                    case (AggregationType.average, PlacesChartType.dwelltime) => ("avg", "avg(UNIX_TIMESTAMP(ge.exiting) - UNIX_TIMESTAMP(ge.entering)) / 1000")
                    case (AggregationType.total, PlacesChartType.dwelltime) => ("avg", "sum(UNIX_TIMESTAMP(ge.exiting) - UNIX_TIMESTAMP(ge.entering)) / 1000")
                    case (AggregationType.unique, PlacesChartType.dwelltime) => ("avg", "sum(UNIX_TIMESTAMP(ge.exiting) - UNIX_TIMESTAMP(ge.entering)) / 1000")
                }
                val sql = s"select grouper, $aggregator(perVisitor) from (select $grouper as grouper, $charter as perVisitor from GeofenceLists gfl join GeofenceListsToPlaces gfl2place on gfl.id=gfl2place.geofenceListId join GeofenceEvents ge on gfl2place.placeId=ge.geofenceId and gfl.appId=ge.appId where gfl.appId=? and (gfl.id=? or 0=?) and ge.entering > ? group by ge.deviceId, grouper) as perVisitors group by grouper order by grouper desc limit 25"

                Q.query[(String, Long, Long, java.sql.Date), AggregationResult](sql).list(appId, geofenceListId, geofenceListId, new java.sql.Date(since))


        }

    def topPlaces(appId: String, geofenceListId: Long) =
        db withSession {
            implicit session: Session =>
                val sql = s"select ge.geofenceId, count(geofenceId) as cnt from GeofenceLists gfl join GeofenceListsToPlaces gfl2place on gfl.id=gfl2place.geofenceListId join (select appId, geofenceId from GeofenceEvents) ge on gfl2place.placeId=ge.geofenceId and gfl.appId=ge.appId where gfl.appId=? and gfl.id=? group by ge.geofenceId order by cnt desc"

                Q.query[(String, Long), SegmentationResult](sql).list(appId, geofenceListId)
        }
}