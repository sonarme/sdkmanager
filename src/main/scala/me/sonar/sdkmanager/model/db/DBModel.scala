package me.sonar.sdkmanager.model.db

import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Query._
import org.springframework.data.mongodb.core.query.Criteria._
import org.joda.time.DateTime
import me.sonar.sdkmanager.core.SimpleMongoRepository
import collection.JavaConversions._
import java.util.Date
import me.sonar.sdkmanager.core.ScalaGoodies._
import com.mongodb.MapReduceCommand

@Document(collection = "sdk_apps")
class App {
    var id: String = _
    var apiKey: String = _
}

@Repository
class AppDao extends SimpleMongoRepository[App] {
    def findByApiKey(apiKey: String) = find(query(where("apiKey") is apiKey)).headOption
}

case class ProfileAttribute(
                                   var key: String, var value: String, var probability: Double = 1, var lastModified: Date = new Date
                                   )

@Document(collection = "sdk_profile_attributes")
case class ProfileAttributes(
                                    var appId: String,
                                    var deviceId: String,
                                    var attributes: java.util.List[ProfileAttribute]) {
    var id = appId + "-" + deviceId

}


@Repository
class ProfileAttributesDao extends SimpleMongoRepository[ProfileAttributes] {
    val aggregateMap = """function Map() {
                         |
                         |	emit(
                         |		this.deviceId,					// how to group
                         |		{attributes: this.attributes}	// associated data point (document)
                         |	);
                         |
                         |}
                         | """.stripMargin
    val aggregateReduce = """function Reduce(key, values) {
                            |
                            |var result = {};
                            |    values.forEach(function(value) {
                            |        var field;
                            |        for (field in value) {
                            |            if (value.hasOwnProperty(field)) {
                            |                result[field] = value[field];
                            |            }
                            |        }
                            |    });
                            |    return result;
                            |
                            |}""".stripMargin

    def mergeUpsert(o: ProfileAttributes) = {
        findOne(o.id).map(_.attributes) foreach {
            existing =>
                o.attributes = (o.attributes ++ existing).distinctBy(_.key)
        }
        save(o)
    }

    def aggregateGeofences(appId: String, tempCollection: String) = {
        mapReduce( s"""{appId:"$appId"}""", aggregateMap, aggregateReduce, None, tempCollection, MapReduceCommand.OutputType.REDUCE)
    }
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
class GeofenceEventDao extends SimpleMongoRepository[GeofenceEvent] {
    val aggregateMap = """|function Map() {
                         |	var geofenceIds = {}
                         |	geofenceIds[this.geofenceId] = true;
                         |	emit(
                         |		this.deviceId,
                         |		{geofenceIdsObj: geofenceIds}
                         |	);
                         |}""".stripMargin
    val aggregateReduce = """
                            |
                            |function Reduce(key, values) {
                            |
                            |	var reduced = {geofenceIdsObj: {}}; // initialize a doc (same format as emitted value)
                            |
                            |	values.forEach(function(val) {
                            |		for (var attrname in val.geofenceIdsObj) {
                            |			reduced.geofenceIdsObj[attrname] = true;
                            |		}
                            |	});
                            |	return reduced;
                            |}
                            | """.stripMargin
    val aggregateFinalize = """function Finalize(key, reduced) {
                              |	reduced.geofenceIds = [];
                              |    for(var key in reduced.geofenceIdsObj){
                              |      reduced.geofenceIds.push(key);
                              |    }
                              |	delete reduced.geofenceIdsObj;
                              |	return reduced;
                              |}""".stripMargin

    def aggregateGeofences(appId: String, tempCollection: String) = {
        mapReduce( s"""{appId:"$appId"}""", aggregateMap, aggregateReduce, Some(aggregateFinalize), tempCollection, MapReduceCommand.OutputType.REPLACE)
    }
}


@Document(collection = "sdk_factual_geopulse")
case class FactualGeopulse(
                                  var id: String,
                                  var response: String)

@Repository
class FactualGeopulseDao extends SimpleMongoRepository[FactualGeopulse]


@Document(collection = "sdk_app_metadata")
case class AppMetadata(
                              var id: String,
                              var platform: String,
                              var category: String)

@Repository
class AppMetadataDao extends SimpleMongoRepository[AppMetadata]