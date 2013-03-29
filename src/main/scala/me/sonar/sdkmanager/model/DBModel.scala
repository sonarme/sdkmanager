package me.sonar.sdkmanager.model

import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.query.Query._
import org.springframework.data.mongodb.core.query.Criteria._

@Document(collection = "sdk_apps")
class App {
    var id: String = _
    var apiKey: String = _
}

@Repository
class AppDao extends SimpleMongoRepository[App]

@Document(collection = "sdk_campaigns")
case class AppCampaign(
                              var id: String,
                              var appId: String,
                              var campaignJson: String)

@Repository
class AppCampaignDao extends SimpleMongoRepository[AppCampaign] {
    def findByAppId(appId: String) = find(query(where("appId") is appId))
}
