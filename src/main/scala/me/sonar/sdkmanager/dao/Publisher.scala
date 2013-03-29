package me.sonar.sdkmanager.dao

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection

case class Publisher(@Key("_id") id: Int, x: String)

object PublisherDAO extends SalatDAO[Publisher, Int](collection = MongoConnection()("test_db")("test_coll"))
