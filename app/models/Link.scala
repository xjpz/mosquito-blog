package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by xjpz on 2016/5/29.
  */

case class Link(
                 var name: Option[String] = None,
                 var author: Option[String] = None,
                 var content: Option[String] = None,
                 var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                 var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                 var tombstone: Option[Int] = Option(0),
                 var lid: Option[Long] = None)

case class LinkListWrapper(links: List[Link], count: Int)

object Link {
  implicit val LinkJSONFormat = Json.format[Link]
}

object LinkListWrapper {
  implicit val LinkListWrapperJSONFormat = Json.format[LinkListWrapper]
}

@Singleton
class Links @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  class LinksTable(tag: Tag)
    extends Table[Link](tag, "link") {

    def lid = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def name = column[Option[String]]("name")

    def author = column[Option[String]]("author")

    def content = column[Option[String]]("content")

    def tombstone = column[Option[Int]]("tombstone")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def * = (name, author, content, inittime, updtime, tombstone, lid) <> ((Link.apply _).tupled, Link.unapply)
  }

  val table = TableQuery[LinksTable]

  def queryById(lid: Long): DBIO[Option[Link]] = table.filter(_.lid === lid).result.headOption

  def retrieve(lid: Long): Future[Option[Link]] = {
    db.run(queryById(lid))
  }

  def query: Future[Seq[Link]] = {
    db.run(table.filter(_.tombstone === 0).sortBy(_.lid.asc).result)
  }

}
