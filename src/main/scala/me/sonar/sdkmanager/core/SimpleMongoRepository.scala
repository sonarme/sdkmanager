package me.sonar.sdkmanager.core

import javax.inject.Inject
import org.springframework.data.mongodb.core.{CollectionCallback, MongoOperations}
import org.bson.types.ObjectId
import collection.JavaConversions._
import org.springframework.data.mongodb.core.query.Query
import com.mongodb._
import com.mongodb.util.JSON

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
        Option(mongoOperations.findById(id, clazz))

    def aggregate(ops: DBObject*) =
        mongoOperations.execute(clazz, new CollectionCallback[Iterable[Map[String, Any]]] {
            def doInCollection(collection: DBCollection) = {
                val firstOp :: otherOps = ops.toList
                collection.aggregate(firstOp, otherOps: _*).results().map {
                    dbo =>
                        dbo.toMap.toMap.asInstanceOf[Map[String, Any]]
                }
            }
        })

    def mapReduce(query: String,
                  map: String
                  ,
                  reduce: String
                  ,
                  finalizeOpt: Option[String],
                  outputCollection: String
                  ,
                  `type`: MapReduceCommand.OutputType) = {
        mongoOperations.execute(clazz, new CollectionCallback[MapReduceOutput] {
            def doInCollection(collection: DBCollection) = {
                val mrc = new MapReduceCommand(collection, map, reduce, outputCollection, `type`, JSON.parse(query).asInstanceOf[BasicDBObject])
                finalizeOpt foreach mrc.setFinalize

                collection.mapReduce(mrc)
            }
        })
    }

    def find(query: Query) =
        mongoOperations.find(query, clazz).toSeq


}

