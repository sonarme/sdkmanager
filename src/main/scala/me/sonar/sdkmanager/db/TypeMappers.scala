package me.sonar.sdkmanager.db

import scala.slick.lifted.MappedTypeMapper
import org.joda.time.DateTime
import java.sql.Date
import me.sonar.sdkmanager.model.Platform
import me.sonar.sdkmanager.model.db.PlaceType

object TypeMappers {

    implicit def date2dateTime = MappedTypeMapper.base[DateTime, Date](
        dateTime => new Date(dateTime.getMillis),
        date => new DateTime(date)
    )

    implicit def platform2String = MappedTypeMapper.base[Platform, String](
        enum => enum.name(),
        name => Platform.valueOf(name)
    )

    implicit def placetype2String = MappedTypeMapper.base[PlaceType, String](
        enum => enum.name(),
        name => PlaceType.valueOf(name)
    )
}
