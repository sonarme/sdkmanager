package me.sonar.sdkmanager.core

import org.springframework.stereotype.Service
import javax.inject.Inject
import me.sonar.sdkmanager.model.Platform
import scala.slick.session.Database
import me.sonar.sdkmanager.model.db.{DB, ProfileAttribute}
import scala.slick.integration.Profile
import scala.slick.driver.H2Driver

@Service
class AppMetadataService extends DB {

    def getAppCategories(ids: Iterable[String], platform: Platform) = db.withSession {
        ProfileAttributes.findAll()
        /* (ids map {
             id =>
                 platform match {
                     case `android` => appMetadataDao.findOne(platform + "-" + id).orElse(fetchAppMetadata(id, platform))
                     case `ios` => throw new RuntimeException("not implemented yet")
                     case _ => throw new RuntimeException("not implemented")
                 }
         })
                 .flatten
                 .map(appMeta => ProfileAttribute(appMeta.category, 1, "category"))  //todo: figure out better probability
                 .groupBy(_.value).mapValues(c =>
             c.reduceLeft((r, s) => {
                 ProfileAttribute(s.value, r.probability + s.probability, s.`type`) //(if we have 2 social categories, merge them and add the probabilities)
             })).values.toSeq*/

    }

    private def fetchAppMetadata(id: String, platform: Platform) = {
        /* future {
             platform match {
                 case `android` =>
                     val doc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + id).get()

                     val metadataList = doc.getElementsByClass("doc-metadata-list")
                     val dt = metadataList.select("dt")
                     val dd = metadataList.select("dd")

                     val pairsHash = (dt zip dd map {
                         case (metadata, value) => (metadata.text().toLowerCase.trim, value.text().toLowerCase.trim)
                     }).toMap

                     appMetadataDao.save(AppMetadata(platform.name() + "-" + id, platform, pairsHash.getOrElse("category:", "")))
                 case _ =>
             }
         }*/
        None
    }
}