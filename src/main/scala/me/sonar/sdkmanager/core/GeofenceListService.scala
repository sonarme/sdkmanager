package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import me.sonar.sdkmanager.model.db.{GeofenceList, GeofenceListDao}
import javax.inject.Inject

@Service
class GeofenceListService {
    @Inject
    var geofenceListDao: GeofenceListDao = _

    def save(geofenceList: GeofenceList) = geofenceListDao.save(geofenceList)

    def findByAppId(appId: String) = geofenceListDao.findByAppId(appId)

    def findById(id: String) = geofenceListDao.findOne(id)
}