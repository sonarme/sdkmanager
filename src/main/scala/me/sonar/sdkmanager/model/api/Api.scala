package me.sonar.sdkmanager.model.api

import me.sonar.sdkmanager.core.Config
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import beans.BeanProperty
import org.joda.time.DateTime

case class SyncRequest(var clientVersion: Int, var events: Seq[PublicEvent] = Seq.empty[PublicEvent]) {
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

case class SyncResponse(apiVersion: Int = Config.ApiVersion, campaigns: Seq[Campaign])

case class Campaign(id: String, appId: String, triggers: Seq[Trigger], rule: Rule)

case class Rule(actions: Seq[Action])

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(Array(
    new Type(name = "msg", value = classOf[MessageAction])
))
abstract class Action()

case class MessageAction(text: String, url: Option[String] = None, viewLabel: Option[String] = None) extends Action

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(Array(
    new Type(name = "sta", value = classOf[StaticGeoFence])
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