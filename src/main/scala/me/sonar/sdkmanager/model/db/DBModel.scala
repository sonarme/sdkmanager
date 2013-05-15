package me.sonar.sdkmanager.model.db

import org.joda.time.DateTime
import scala.slick.integration.{Entity, StringEntity, _Component, Profile}
import me.sonar.sdkmanager.db.TypeMappers._
import javax.inject.Inject
import scala.slick.session.Database
import scala.slick.driver.MySQLDriver
import me.sonar.sdkmanager.model.Platform
import scala.slick.lifted.{SimpleExpression, Column, SimpleFunction}
import com.github.tototoshi.slick.JodaSupport._

case class App(var id: String, apiKey: String) extends StringEntity[App]

case class ProfileAttribute(
                                   var id: String, appId: String, deviceId: String, `type`: String = "none", value: String, probability: Double = 1, lastModified: DateTime
                                   ) extends StringEntity[ProfileAttribute]


case class AppCampaign(
                              var id: String,
                              var appId: String,
                              var campaignJson: String) extends StringEntity[AppCampaign]

case class GeofenceEvent(
                                var id: String,
                                var appId: String,
                                var platform: Platform,
                                var deviceId: String,
                                var geofenceId: String,
                                var lat: Double,
                                var lng: Double,
                                var entering: DateTime,
                                var exiting: DateTime) extends StringEntity[GeofenceEvent]


case class FactualGeopulse(
                                  var id: String,
                                  response: String) extends StringEntity[FactualGeopulse]

case class AppMetadata(
                              var id: String,
                              platform: Platform,
                              category: String) extends StringEntity[AppMetadata]

case class Place(var id: String, name: String, lat: Double, lng: Double, `type`: PlaceType) extends StringEntity[Place]

case class GeofenceList(var id: Long, var appId: String, var name: String) extends Entity[GeofenceList]

case class Geofence(var id: Long, var geofenceListId: Long, appId: String, name: String) extends Entity[GeofenceList]


trait DB extends _Component with Profile {
    val profile = MySQLDriver
    @Inject
    var db: Database = _

    lazy val ddl = Apps.ddl ++ ProfileAttributes.ddl ++ Campaigns.ddl ++ GeofenceEvents.ddl ++ FactualGeopulseResponses.ddl ++ AppMetadatas.ddl

    val unixTimestamp = SimpleFunction.unary[DateTime, Long]("UNIX_TIMESTAMP")
    val hour = SimpleFunction.unary[DateTime, Int]("HOUR")
    val year = SimpleFunction.unary[DateTime, Int]("YEAR")
    val week = SimpleFunction.unary[DateTime, Int]("WEEK")
    val month = SimpleFunction.unary[DateTime, Int]("MONTH")
    val dayOfYear = SimpleFunction.unary[DateTime, Int]("DAYOFYEAR")

    sealed abstract class Interval(val sql: String)

    case object Day extends Interval("DAY")

    case object Hour extends Interval("HOUR")

    def dateAdd(i: Interval) = SimpleExpression.binary[DateTime, Int, DateTime] {
        (l, r, qb) =>
            qb.sqlBuilder += "DATE_ADD("
            qb.expr(l)
            qb.sqlBuilder += ", INTERVAL "
            qb.expr(r)
            qb.sqlBuilder += ' ' += i.sql += ')'
    }

    object Apps extends StringMapper[App]("Apps") {

        def apiKey = column[String]("apiKey")

        def * = id ~ apiKey <>(App, App.unapply _)
    }


    //@Repository
    //class AppDao extends SimpleMongoRepository[App] {
    //    def findByApiKey(apiKey: String) = find(query(where("apiKey") is apiKey)).headOption
    //}

    import profile.simple._


    object ProfileAttributes extends StringMapper[ProfileAttribute]("ProfileAttributes") {

        def appId = column[String]("appId")

        def deviceId = column[String]("deviceId")

        def `type` = column[String]("type")

        def value = column[String]("value")

        def probability = column[Double]("probability")

        def lastModified = column[DateTime]("lastModified")

        def * = id ~ appId ~ deviceId ~ `type` ~ value ~ probability ~ lastModified <>(ProfileAttribute, ProfileAttribute.unapply _)
    }

    //
    //@Repository
    //class ProfileAttributesDao extends SimpleMongoRepository[ProfileAttributes] {
    //
    //    val aggregateMap = """function Map() {
    //                         |
    //                         |	emit(
    //                         |		this.deviceId,					// how to group
    //                         |		{attributes: this.attributes}	// associated data point (document)
    //                         |	);
    //                         |
    //                         |}
    //                         | """.stripMargin
    //    val aggregateReduce = """function Reduce(key, values) {
    //                            |
    //                            |var result = {};
    //                            |    values.forEach(function(value) {
    //                            |        var field;
    //                            |        for (field in value) {
    //                            |            if (value.hasOwnProperty(field)) {
    //                            |                result[field] = value[field];
    //                            |            }
    //                            |        }
    //                            |    });
    //                            |    return result;
    //                            |
    //                            |}""".stripMargin
    //
    //    def mergeUpsert(o: ProfileAttributes) = {
    //        findOne(o.id).map(_.attributes) foreach {
    //            existing =>
    //                o.attributes = (o.attributes ++ existing).distinctBy(_.id)
    //        }
    //        save(o)
    //    }
    //
    //    def aggregateGeofences(appId: String, tempCollection: String) = {
    //        mapReduce( s"""{appId:"$appId"}""", aggregateMap, aggregateReduce, None, tempCollection, MapReduceCommand.OutputType.REDUCE)
    //    }
    //
    //    def removeAttributesWithType(id: String, `type`: String) {
    //        mongoOperations.updateFirst(query(where("_id") is id),
    //            new Update().pull("attributes", new ProfileAttributePull(`type`)), classOf[ProfileAttributes])
    //    }
    //}

    //private case class ProfileAttributePull(var `type`: String)

    object Campaigns extends StringMapper[AppCampaign]("Campaigns") {

        def appId = column[String]("appId")

        def campaignJson = column[String]("campaignJson")

        def * = id ~ appId ~ campaignJson <>(AppCampaign, AppCampaign.unapply _)
    }

    //@Repository
    //class AppCampaignDao extends SimpleMongoRepository[AppCampaign] {
    //    def findByAppId(appId: String) = find(query(where("appId") is appId))
    //}


    object GeofenceEvents extends StringMapper[GeofenceEvent]("GeofenceEvents") {

        def appId = column[String]("appId")

        def platform = column[Platform]("platform")

        def deviceId = column[String]("deviceId")

        def geofenceId = column[String]("geofenceId")

        def lat = column[Double]("lat")

        def lng = column[Double]("lng")

        def entering = column[DateTime]("entering")

        def exiting = column[DateTime]("exiting")

        def * = id ~ appId ~ platform ~ deviceId ~ geofenceId ~ lat ~ lng ~ entering ~ exiting <>(GeofenceEvent, GeofenceEvent.unapply _)
    }

    //
    //
    //@Repository
    //class GeofenceEventDao extends SimpleMongoRepository[GeofenceEvent] {
    //    val aggregateMap = """|function Map() {
    //                         |	var geofenceIds = {}
    //                         |	geofenceIds[this.geofenceId] = true;
    //                         |	emit(
    //                         |		this.deviceId,
    //                         |		{geofenceIdsObj: geofenceIds}
    //                         |	);
    //                         |}""".stripMargin
    //    val aggregateReduce = """
    //                            |
    //                            |function Reduce(key, values) {
    //                            |
    //                            |	var reduced = {geofenceIdsObj: {}}; // initialize a doc (same format as emitted value)
    //                            |
    //                            |	values.forEach(function(val) {
    //                            |		for (var attrname in val.geofenceIdsObj) {
    //                            |			reduced.geofenceIdsObj[attrname] = true;
    //                            |		}
    //                            |	});
    //                            |	return reduced;
    //                            |}
    //                            | """.stripMargin
    //    val aggregateFinalize = """function Finalize(key, reduced) {
    //                              |	reduced.geofenceIds = [];
    //                              |    for(var key in reduced.geofenceIdsObj){
    //                              |      reduced.geofenceIds.push(key);
    //                              |    }
    //                              |	delete reduced.geofenceIdsObj;
    //                              |	return reduced;
    //                              |}""".stripMargin
    //
    //    def aggregateGeofences(appId: String, tempCollection: String) = {
    //        mapReduce( s"""{appId:"$appId"}""", aggregateMap, aggregateReduce, Some(aggregateFinalize), tempCollection, MapReduceCommand.OutputType.REPLACE)
    //    }
    //}


    object FactualGeopulseResponses extends StringMapper[FactualGeopulse]("FactualGeopulseResponses") {

        def response = column[String]("response")

        def * = id ~ response <>(FactualGeopulse, FactualGeopulse.unapply _)
    }


    object AppMetadatas extends StringMapper[AppMetadata]("AppMetadatas") {

        def platform = column[Platform]("platform")

        def category = column[String]("category")

        def * = id ~ platform ~ category <>(AppMetadata, AppMetadata.unapply _)
    }

    object GeofenceLists extends Mapper[GeofenceList]("GeofenceLists") {

        def appId = column[String]("appId")

        def name = column[String]("name")

        def * = id ~ appId ~ name <>(GeofenceList, GeofenceList.unapply _)

        def places = GeofenceListsToPlaces.filter(_.geofenceListId === id).flatMap(_.place)
    }

    object GeofenceListsToPlaces extends Table[(Long, String)]("GeofenceListsToPlaces") {

        def geofenceListId = column[Long]("geofenceListId")

        def placeId = column[String]("placeId")

        def * = geofenceListId ~ placeId

        def place = foreignKey("placeId_fk", placeId, Places)(b => b.id)
    }

    object Places extends StringMapper[Place]("Places") {

        def name = column[String]("name")

        def lat = column[Double]("lat")

        def lng = column[Double]("lng")

        def `type` = column[PlaceType]("type")

        def * = id ~ name ~ lat ~ lng ~ `type` <>(Place, Place.unapply _)

    }

    // TODO: place, geofence
}
