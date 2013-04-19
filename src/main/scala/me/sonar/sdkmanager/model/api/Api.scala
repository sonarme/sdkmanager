package me.sonar.sdkmanager.model.api

import me.sonar.sdkmanager.core.Config
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.fasterxml.jackson.annotation.JsonSubTypes._
import beans.BeanProperty
import org.joda.time.DateTime
import me.sonar.sdkmanager.model.db.ProfileAttribute

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

case class SyncResponse(apiVersion: Int = Config.ApiVersion, campaigns: Seq[Campaign], profileAttributes: Seq[ProfileAttribute] = Seq.empty[ProfileAttribute])

case class Campaign(id: String, appId: String, triggers: Seq[Trigger], rule: Rule)

case class Condition(predicate: Predicate)

case class Rule(conditions: Seq[Condition], actions: Seq[Action])

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
    var processRole: Boolean = _
}

case class StaticGeoFence(lat: Double, lng: Double, radius: Float, entering: Boolean) extends Trigger

case class DynamicGeoFence(inferredLocationType: String, radius: Float) extends Trigger

case class Compare(`var`: String, op: String, value: String) extends Predicate

case class Exists(predicates: Seq[Predicate]) extends Predicate

case class ForAll(predicates: Seq[Predicate]) extends Predicate

case class Not(predicate: Predicate) extends Predicate

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(Array(
    new Type(name = "compare", value = classOf[Compare]),
    new Type(name = "exists", value = classOf[Exists]),
    new Type(name = "forall", value = classOf[ForAll]),
    new Type(name = "not", value = classOf[Not])))
abstract class Predicate

