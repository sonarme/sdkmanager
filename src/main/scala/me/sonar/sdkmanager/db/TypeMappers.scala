package me.sonar.sdkmanager.db

import scala.slick.lifted.MappedTypeMapper
import me.sonar.sdkmanager.model.Platform
import me.sonar.sdkmanager.model.db.PlaceType

object TypeMappers {


    implicit def platform2String = MappedTypeMapper.base[Platform, String](
        enum => enum.name(),
        name => Platform.valueOf(name)
    )

    implicit def placetype2String = MappedTypeMapper.base[PlaceType, String](
        enum => enum.name(),
        name => PlaceType.valueOf(name)
    )
}
