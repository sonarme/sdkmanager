package me.sonar.sdkmanager.model.db

import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Query._
import org.springframework.data.mongodb.core.query.Criteria._
import org.joda.time.DateTime
import me.sonar.sdkmanager.core.SimpleMongoRepository

@Document(collection = "sdk_apps")
class App {
    var id: String = _
    var apiKey: String = _
}

@Repository
class AppDao extends SimpleMongoRepository[App] {
    def findByApiKey(apiKey: String) = find(query(where("apiKey") is apiKey)).headOption
}


@Document(collection = "sdk_campaigns")
case class AppCampaign(
                              var id: String,
                              var appId: String,
                              var campaignJson: String)

@Repository
class AppCampaignDao extends SimpleMongoRepository[AppCampaign] {
    def findByAppId(appId: String) = find(query(where("appId") is appId))
}


@Document(collection = "sdk_geofence_events")
case class GeofenceEvent(
                                var id: String,
                                var appId: String,
                                var platform: String,
                                var deviceId: String,
                                var geofenceId: String,
                                var lat: Double,
                                var lng: Double,
                                var entering: DateTime,
                                var exiting: DateTime)

@Repository
class GeofenceEventDao extends SimpleMongoRepository[GeofenceEvent]
