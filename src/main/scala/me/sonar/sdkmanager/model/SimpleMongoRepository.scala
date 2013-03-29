package me.sonar.sdkmanager.model

import javax.inject.Inject
import org.springframework.data.mongodb.core.MongoOperations
import org.bson.types.ObjectId
import collection.JavaConversions._
import org.springframework.data.mongodb.core.query.Query

class SimpleMongoRepository[T: Manifest] {
    @Inject
    var mongoOperations: MongoOperations = _

    def clazz = manifest[T].runtimeClass.asInstanceOf[Class[T]]

    def save[S <: T](entity: S) = {
        mongoOperations.save(entity)
        entity
    }

    def saveMultiple[S <: T](entities: Iterable[S]) = entities map save

    def findOne(id: String) =
        Option(ObjectId.massageToObjectId(id)) flatMap (objectId => Option(mongoOperations.findById(objectId, clazz)))

    def find(query: Query) =
        mongoOperations.find(query, clazz).toSeq


}

