package me.sonar.sdkmanager.core

import com.fasterxml.jackson.databind.{DeserializationFeature, PropertyNamingStrategy, ObjectMapper}
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class RestObjectMapper extends ObjectMapper {

    registerModule(new JodaModule)
    // override serializers/deserializers in order to print seconds
    //jodaModule.addDeserializer[DateTime](classOf[DateTime], new DateTimeSecsDeserializer)
    //jodaModule.addSerializer[DateTime](classOf[DateTime], new DateTimeSecsSerializer)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    registerModule(DefaultScalaModule)
}
