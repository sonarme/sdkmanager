package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.web.api.RestObjectMapper
import me.sonar.sdkmanager.model.api.Campaign
import scala.slick.session.Database
import me.sonar.sdkmanager.model.db.DB
import scala.slick.driver.H2Driver
import scala.slick.integration.Profile
import me.sonar.sdkmanager.model.db.App

@Service
class CampaignService extends DB {

    val mapper = new RestObjectMapper

    def save(campaign: Campaign): Campaign = null

    /*database.withSession {
            implicit s: Session =>
                Campaigns.insert(campaign.id, campaign.appId, mapper.writeValueAsString(campaign))
        }
    */
    def findByAppId(appId: String): Seq[Campaign] = Seq.empty[Campaign]

    /*database.withSession {
            implicit s: Session => for (c <- Campaigns if c.appId == appId) yield mapper.readValue(c.campaignJson, classOf[Campaign])
        }
    */
    def findAppByApiKey(apiKey: String): Option[App] = None

    /* database.withSession {
            implicit s: Session => for (c <- Apps if c.apiKey == apiKey) yield c
        }*/
}
