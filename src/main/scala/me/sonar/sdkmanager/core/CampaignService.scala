package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.web.api.RestObjectMapper
import me.sonar.sdkmanager.model.api.Campaign
import me.sonar.sdkmanager.model.db.{AppCampaignDao, AppCampaign}

@Service
class CampaignService {
    @Inject
    var campaignDao: AppCampaignDao = _
    val mapper = new RestObjectMapper

    def save(campaign: Campaign) = campaignDao.save(AppCampaign(campaign.id, campaign.appId, mapper.writeValueAsString(campaign)))

    def findByAppId(appId: String) = campaignDao.findByAppId(appId).map(ac => mapper.readValue(ac.campaignJson, classOf[Campaign]))
}
