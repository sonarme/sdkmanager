package me.sonar.sdkmanager.web.api

import com.fasterxml.jackson.databind.{SerializationFeature, DeserializationFeature, PropertyNamingStrategy, ObjectMapper}
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.annotation.JsonInclude.Include

class RestObjectMapper extends ObjectMapper {

    registerModule(new JodaModule)
    // override serializers/deserializers in order to print seconds
    //jodaModule.addDeserializer[DateTime](classOf[DateTime], new DateTimeSecsDeserializer)
    //jodaModule.addSerializer[DateTime](classOf[DateTime], new DateTimeSecsSerializer)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
    enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
    setSerializationInclusion(Include.NON_NULL)
    registerModule(DefaultScalaModule)
}
