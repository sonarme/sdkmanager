package me.sonar.sdkmanager.model

import me.sonar.sdkmanager.core.Config
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import beans.BeanProperty

case class SyncRequest(var clientVersion: Int) {
    def this() = this(-1)
}

case class SyncResponse(apiVersion: Int = Config.ApiVersion, campaigns: Iterable[Campaign])

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

case class StaticGeoFence(lat: Double, lng: Double, radius: Float) extends Trigger
