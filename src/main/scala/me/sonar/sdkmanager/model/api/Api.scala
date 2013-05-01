package me.sonar.sdkmanager.model.api

import me.sonar.sdkmanager.core.Config
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.fasterxml.jackson.annotation.JsonSubTypes._
import beans.BeanProperty
import org.joda.time.DateTime
import me.sonar.sdkmanager.model.db.{AppMetadata, ProfileAttribute}

case class SyncRequest(var clientVersion: Int, var events: Seq[PublicEvent] = Seq.empty[PublicEvent], var profileAttributes: Seq[ProfileAttribute] = Seq.empty[ProfileAttribute]) {
    def this() = this(-1)
}

case class GeofenceEvent(var geofenceId: String, var lat: Double, var lng: Double, var entering: Option[DateTime] = None, var exiting: Option[DateTime] = None) extends PublicEvent {
    def this() = this(null, -1, -1)
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(Array(
    new Type(name = "geo", value = classOf[GeofenceEvent])
))
abstract class PublicEvent {
    @BeanProperty
    var id: String = _
    @BeanProperty
    var appId: String = _
}

case class SyncResponse(apiVersion: Int = Config.ApiVersion, campaigns: Seq[Campaign], profileAttributes: Seq[ProfileAttribute] = Seq.empty[ProfileAttribute], disabled: Boolean = false)

case class Campaign(id: String, appId: String, triggers: Seq[Trigger], rule: Rule)

case class Condition(predicate: Predicate)

case class Frequency(timeWindowInMs: Long, count: Int)

case class Rule(conditions: Seq[Condition], actions: Seq[Action], frequency: Frequency)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(Array(
    new Type(name = "msg", value = classOf[MessageAction])
))
abstract class Action()

case class MessageAction(text: String, url: Option[String] = None, viewLabel: Option[String] = None) extends Action

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(Array(
    new Type(name = "sta", value = classOf[StaticGeoFence]),
    new Type(name = "dyn", value = classOf[DynamicGeoFence])
))
abstract class Trigger {
    @BeanProperty
    var id: String = _
    @BeanProperty
    var publish: Boolean = _
    @BeanProperty
    var action: Boolean = _
}

case class StaticGeoFence(lat: Double, lng: Double, radius: Float, entering: Boolean) extends Trigger

case class DynamicGeoFence(inferredLocationType: String, radius: Float) extends Trigger

case class ComparePredicate(`var`: String, op: String, value: String) extends Predicate

case class OrPredicate(predicates: Seq[Predicate]) extends Predicate

case class AndPredicate(predicates: Seq[Predicate]) extends Predicate

case class NotPredicate(predicate: Predicate) extends Predicate

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(Array(
    new Type(name = "cmp", value = classOf[ComparePredicate]),
    new Type(name = "or", value = classOf[OrPredicate]),
    new Type(name = "and", value = classOf[AndPredicate]),
    new Type(name = "not", value = classOf[NotPredicate])))
abstract class Predicate




case class FactualRequest(query: Option[String], geo: Option[FactualGeo] = None, filter: Option[FactualFilter] = None, limit: Option[Int] = None, offset: Option[Int] = None)

case class FactualFilter(category: Option[Seq[String]] = None,
                         region: Option[Seq[String]] = None,
                         locality: Option[Seq[String]] = None)

case class FactualGeo(lat: Double,
                      lng: Double,
                      radius: Int = 5000)

case class FactualResponse(data: java.util.List[java.util.Map[String, Object]])