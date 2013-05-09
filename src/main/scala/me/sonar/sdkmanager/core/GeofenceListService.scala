package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import me.sonar.sdkmanager.model.db.{GeofenceList, DB}
import scala.slick.session.Session

@Service
class GeofenceListService extends DB {

    import profile.simple._

    def save(geofenceList: GeofenceList) = GeofenceLists.insert(geofenceList)

    def findByAppId(appId: String): Seq[GeofenceList] = db withSession {
        implicit session: Session =>
            (for (g <- GeofenceLists if g.appId === appId) yield g).list()
    }

    def findById(id: Long): Option[GeofenceList] = GeofenceLists.findById(id)
}