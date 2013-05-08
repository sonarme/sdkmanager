package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.model.db.{GeofenceList, DB}

@Service
class GeofenceListService extends DB {

    def save(geofenceList: GeofenceList) = null //geofenceListDao.save(geofenceList)

    def findByAppId(appId: String): Seq[GeofenceList] = Seq.empty[GeofenceList] //geofenceListDao.findByAppId(appId)

    def findById(id: String): Option[GeofenceList] = None //geofenceListDao.findOne(id)
}