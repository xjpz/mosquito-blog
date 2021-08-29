package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by xjpz on 2016/6/8.
  */

case class Mood(
                 var content: Option[String] = None,
                 var uid: Option[Long] = Option(0),
                 var name: Option[String] = None,
                 var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                 var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                 var tombstone: Option[Int] = Option(0),
                 var id: Option[Long] = None)

object Mood {
  implicit val MoodJSONFormat = Json.format[Mood]

}

@Singleton
class Moods @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class MoodsTable(tag: Tag) extends Table[Mood](tag, "mood") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def content = column[Option[String]]("content")

    def uid = column[Option[Long]]("uid")

    def name = column[Option[String]]("name")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def tombstone = column[Option[Int]]("tombstone")

    def * = (content, uid, name, inittime, updtime, tombstone, id) <> ((Mood.apply _).tupled, Mood.unapply)
  }

  private val table = TableQuery[MoodsTable]

  def retrieve(id: Long): Future[Option[Mood]] = {
    db.run(table.filter(_.id === id).result.headOption)
  }

  def queryHead: Future[Option[Mood]] = {
    db.run(table.filter(_.tombstone === 0).sortBy(_.id.desc).result.headOption)
  }
}