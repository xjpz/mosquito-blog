package models

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

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


class Moods @Inject()(articles: Articles)(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  class MoodsTable(tag: Tag) extends Table[Mood](tag, "news2mood") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def content = column[Option[String]]("content")

    def uid = column[Option[Long]]("uid")

    def name = column[Option[String]]("name")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def tombstone = column[Option[Int]]("tombstone")

    def * = (content, uid, name, inittime, updtime, tombstone, id) <> (Mood.tupled, Mood.unapply)
  }

  val table = TableQuery[MoodsTable]

  def queryById(id: Long): DBIO[Option[Mood]] = table.filter(_.id === id).result.headOption

  def retrieve(id: Long): Future[Option[Mood]] = {
    db.run(queryById(id))
  }

  def queryHead: Future[Option[Mood]] = {
    db.run(table.filter(_.tombstone === 0).sortBy(_.id.desc).result.headOption)
  }
}