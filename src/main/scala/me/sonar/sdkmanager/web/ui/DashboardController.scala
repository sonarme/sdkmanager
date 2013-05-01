package me.sonar.sdkmanager.web.ui

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestBody, RequestParam, RequestMethod, RequestMapping}
import scala.Array
import org.springframework.http.{MediaType, HttpOutputMessage}
import java.nio.charset.Charset
import au.com.bytecode.opencsv.CSVWriter
import java.io.OutputStreamWriter
import javax.servlet.http.HttpServletResponse
import javax.inject.Inject
import me.sonar.sdkmanager.core.{CountStats, CampaignService, SyncService}
import org.scala_tools.time.Imports._
import grizzled.slf4j.Logging

@Controller
class DashboardController extends Logging {
    @Inject
    var syncService: SyncService = _
    @Inject
    var campaignService: CampaignService = _

    @RequestMapping(value = Array("/stats"), method = Array(RequestMethod.GET))
    def stats(@RequestParam("appId") appId: String, response: HttpServletResponse) {
        val dwellTimes = syncService.aggregateDwellTime(appId)
        val visitorsPerHourOfDay = syncService.aggregateVisitorsPerHourOfDay(appId)
        val visitsPerVisitor = syncService.aggregateVisitsPerVisitor(appId)
        val visitsPerHourOfDay = syncService.aggregateVisitsPerHourOfDay(appId)
        val attributes = syncService.aggregateGeofenceData(appId)
        val attributeKeys = attributes.values.flatMap(_.keySet).toArray.distinct.sorted
        val gfs = dwellTimes.keySet ++ visitorsPerHourOfDay.keySet ++ visitsPerVisitor.keySet ++ visitsPerHourOfDay.keySet
        response.setContentType(new MediaType("text", "csv", Charset.forName("utf-8")).toString)
        response.addHeader("Content-Disposition", """attachment; filename="stats.csv"""")
        val writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream))
        try {
            var first = true
            gfs foreach {
                gf =>
                    val wrapper = CSVWrapper(geofenceId = gf, dwellTimes = dwellTimes.get(gf), visitsPerVisitor.get(gf), visitorsPerHourOfDay = visitorsPerHourOfDay.getOrElse(gf, Map.empty[Int, Int]),
                        visitsPerHourOfDay = visitsPerHourOfDay.getOrElse(gf, Map.empty[Int, Int]))
                    if (first)
                        writer.writeNext(wrapper.keys.toArray ++ attributeKeys.map(x => x._1 + " " + x._2))
                    val attributesForGf = attributes.getOrElse(gf, Map.empty)
                    val avgs = attributeKeys.map(x => attributesForGf.get(x).map(_.toString).orNull)
                    writer.writeNext(wrapper.values.toArray ++ avgs)
                    first = false
            }
        } finally {
            writer.close()
        }
    }
}

case class CSVWrapper(geofenceId: String, dwellTimes: Option[CountStats], visitsPerVisitor: Option[CountStats], visitorsPerHourOfDay: Map[Int, Int], visitsPerHourOfDay: Map[Int, Int]) {
    val config =
        Seq(("Geofence", () => geofenceId),
            ("Visits total", () => visitsPerHourOfDay.values.sum.toString),
            ("Visitors total", () => visitorsPerHourOfDay.values.sum.toString),
            ("Dwell Time(m) Min", () => dwellTimes.map(_.min / 1000 / 60).mkString),
            ("Dwell Time(m) Avg", () => dwellTimes.map(_.avg / 1000 / 60).mkString),
            ("Dwell Time(m) Max", () => dwellTimes.map(_.max / 1000 / 60).mkString),
            ("Visits/Visitor Min", () => visitsPerVisitor.map(_.min).mkString),
            ("Visits/Visitor Avg", () => visitsPerVisitor.map(_.avg).mkString),
            ("Visits/Visitor Max", () => visitsPerVisitor.map(_.max).mkString)
        ) ++ (0 to 23).map(h => (s"Visits $h:00-${h + 1}:00", () => visitsPerHourOfDay.get(h).map(_.toString).mkString)) ++
                (0 to 23).map(h => (s"Visitors $h:00-${h + 1}:00", () => visitorsPerHourOfDay.get(h).map(_.toString).mkString))

    def keys = config.map(_._1)

    def values = config.map(_._2())
}
