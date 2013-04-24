package me.sonar.sdkmanager.core

import collection.JavaConversions._
import org.springframework.stereotype.Service
import org.jsoup.Jsoup
import me.sonar.sdkmanager.model.db.{ProfileAttribute, AppMetadataDao, AppMetadata}
import javax.inject.Inject
import scala.concurrent._
import ExecutionContext.Implicits.global
import me.sonar.sdkmanager.model.Platform
import me.sonar.sdkmanager.model.Platform._
import me.sonar.sdkmanager.model.api.AppMetadataResponse

@Service
class AppMetadataService {
    @Inject
    var appMetadataDao: AppMetadataDao = _

    def getAppCategories(ids: Iterable[String], platform: Platform) = {
        (ids map {
            id =>
                platform match {
                    case `android` => appMetadataDao.findOne(platform + "-" + id).orElse(fetchAppMetadata(id, platform))
                    case `ios` => throw new RuntimeException("not implemented yet")
                    case _ => throw new RuntimeException("not implemented")
                }
        })
                .flatten
                .map(appMeta => ProfileAttribute(appMeta.category, "true", 1, "category"))  //todo: figure out better probability
                .groupBy(_.key).mapValues(c =>
            c.reduceLeft((r, s) => {
                ProfileAttribute(s.key, s.value, r.probability + s.probability, s.`type`, s.lastModified) //(if we have 2 social categories, merge them and add the probabilities)
            })).values.toSeq
    }

    private def fetchAppMetadata(id: String, platform: Platform) = {
        future {
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
        }
        None
    }
}