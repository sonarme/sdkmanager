package me.sonar.sdkmanager.core

import collection.JavaConversions._
import me.sonar.sdkmanager.{SpringComponentTest, SimpleTest}
import org.jsoup.Jsoup
import javax.inject.Inject
import me.sonar.sdkmanager.model.Platform

class AppMetadataTest extends SpringComponentTest {

    @Inject
    var appMetadataService: AppMetadataService = _

    "Google Play" should "return app metadata" in {
        val appMetadataResponse = appMetadataService.getAppMetadatas(List("me.sonar.android", "com.android.nfc"), Platform.android)
        Thread.sleep(5000)
        assert(appMetadataResponse.appMetadatas.size > 0)
    }
}