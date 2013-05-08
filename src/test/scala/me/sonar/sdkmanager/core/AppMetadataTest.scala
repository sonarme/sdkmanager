package me.sonar.sdkmanager.core

import collection.JavaConversions._
import me.sonar.sdkmanager.{SpringComponentTest, SimpleTest}
import org.jsoup.Jsoup
import javax.inject.Inject
import me.sonar.sdkmanager.model.Platform
import me.sonar.sdkmanager.model.db.{DB, ProfileAttribute}
import scala.slick.session.Database

class AppMetadataTest extends SpringComponentTest with DB {

    @Inject
    var appMetadataService: AppMetadataService = _

    "Google Play" should "return app metadata" in {
        val response = appMetadataService.getAppCategories(List("me.sonar.android", "com.android.nfc", "ht.highlig", "org.wikipedia"), Platform.android)
        Thread.sleep(5000)
        assert(response.size > 0)
        assert(response.get(0).probability === 1)
        assert(response.get(1).probability === 2)
    }
}