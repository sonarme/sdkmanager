package me.sonar.sdkmanager

import com.mongodb.casbah.MongoCollection

package object dao {
    def mongoCollection(collectionName: String, sourceName: String = "default"): MongoCollection = {
        null //connection(collectoiapp.plugin[SalatPlugin].map(_.collection(collectionName, sourceName)).getOrElse(throw new PlayException("SalatPlugin is not registered.", "You need to register the plugin with \"500:se.radley.plugin.salat.SalatPlugin\" in conf/play.plugins"))
    }

    /*def connection: MongoConnection = {
          if (conn == null) {
            conn = options.map(opts => MongoConnection(hosts, opts)).getOrElse(MongoConnection(hosts))

            val authOpt = for {
              u <- user
              p <- password
            } yield connection(dbName).authenticate(u, p)

            if (!authOpt.getOrElse(true)) {
              throw configuration.reportError("mongodb", "Access denied to MongoDB database: [" + dbName + "] with user: [" + user.getOrElse("") + "]")
            }

            conn.setWriteConcern(writeConcern)
          }
          conn
        }*/

}
