package models

import akka.actor.Actor
import play.api.libs.json._

import play.api.cache.Cache
import play.api.Play.current

import scala.slick.driver.MySQLDriver.simple._

case class Link (
                var name:Option[String] = None,
                var author:Option[String] = None,
                var content:Option[String] = None,
                var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                var tombstone: Option[Int] = Option(0),
                var lid: Option[Long] = None
                    )

case class LinkWrapper (
                    val name:Option[String] = None,
                    val author:Option[String] = None,
                    val content:Option[String] = None,
                    val inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    val updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    val tombstone: Option[Int] = Option(0),
                    val lid: Option[Long] = None
                    )

case class LinkListWrapper(
                          val links:Option[List[LinkWrapper]],
                          val count:Option[Int])

class Links(tag: Tag)
    extends Table[Link](tag, "link") {

    def lid = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[Option[String]]("name")
    def author = column[Option[String]]("author")
    def content = column[Option[String]]("content")
    def tombstone = column[Option[Int]]("tombstone")
    def inittime = column[Option[Long]]("init_time")
    def updtime = column[Option[Long]]("update_time")

    def * = (name,author,content,inittime,updtime,tombstone,lid) <> (Link.tupled,Link.unapply)

}

trait LinkJSONTrait{
    implicit val LinkJSONFormat = Json.format[Link]
    implicit val LinkWrapperJSONFormat = Json.format[LinkWrapper]
    implicit val LinkListWrapperJSONFormat = Json.format[LinkListWrapper]
}

object Links extends LinkJSONTrait {
    val table = TableQuery[Links]

    //set Keys
    private lazy  val linkListKey = "linkList"

    def pack(link: Link) = {}

    def wrap(link: Link) :LinkWrapper= {
        val wrapper = LinkWrapper(
        link.name,
        link.author,
        link.content,
        link.inittime,
        link.updtime,
        link.tombstone,
        link.lid
        )
        wrapper
    }

    def init(link: Link)
            (implicit session: Session): Option[LinkWrapper] = {
        var retOpt = None: Option[LinkWrapper]
        pack(link)
        val lid = (table returning table.map(_.lid)) += link
        link.lid = lid
        retOpt = Option(wrap(link))

        Cache.remove(linkListKey)

        retOpt
    }

    def query(page:Int,size:Int)(implicit session:Session):Option[LinkListWrapper] = {
        var ret = None:Option[LinkListWrapper]

        val queryLinkListCacheOpt = Cache.getAs[LinkListWrapper](linkListKey)
        if(queryLinkListCacheOpt.isDefined){
            ret = Option(queryLinkListCacheOpt.get)

        }else{
            val queryLinkList = table.filter(_.tombstone === 0).sortBy(_.lid.asc).list
            val linkListWrapper = queryLinkList.map(link => wrap(link))

            val retLinkListWrapper = LinkListWrapper(
                Option(linkListWrapper.slice(size * page, size * page + size)),
                Option(linkListWrapper.length)
            )

            ret = Option(retLinkListWrapper)
            Cache.set(linkListKey,retLinkListWrapper,3600)
        }

        ret
    }

    def remove(lid:Long)
              (implicit session:Session):Option[LinkWrapper] = {
        var ret = None: Option[LinkWrapper]

        val updtime = Option(System.currentTimeMillis() / 1000L)
        val linkOpt = table.filter(_.lid === lid)
            .filter(_.tombstone === 0).take(1).firstOption
        if(linkOpt.isDefined){
            val link = linkOpt.get
            link.tombstone = Option(1)
            link.updtime = updtime

            table.filter(_.lid === lid).map(row => (row.tombstone, row.updtime)).update(link.tombstone,link.updtime)

            ret = Option(wrap(link))
        }
        ret
    }

    def update(link: Link)
              (implicit session:Session):Option[LinkWrapper] = {
        var ret = None: Option[LinkWrapper]
        val updtime = Option(System.currentTimeMillis() / 1000L)

        val linkOpt = table.filter(_.lid === link.lid)
            .filter(_.tombstone === 0).take(1).firstOption
        if(linkOpt.isDefined){

            val queryLink = linkOpt.get
            if(link.name.isDefined){
                queryLink.name = link.name
            }
            if(link.author.isDefined){
                queryLink.author = link.author
            }
            if(link.content.isDefined){
                queryLink.content = link.content
            }

            table.filter(_.lid === link.lid).map(row =>
                (row.name,row.author,row.content, row.updtime))
                .update(queryLink.name,queryLink.author,queryLink.content,link.updtime)

            ret = Option(wrap(queryLink))
        }

        ret
    }

}

object LinksActor{
    case class Init(db:Database,link: Link)
    case class Query(db:Database,page:Int,size:Int)
    case class Remove(db:Database,lid:Long)
    case class Update(db:Database,link: Link)
}

import models.LinksActor.{Init, Query, Remove,Update}

class LinksActor extends Actor{
    override def receive: Receive = {
        case Init(db:Database,link:Link) =>
            db.withSession{ implicit session => sender ! Links.init(link)}

        case Query(db:Database,page:Int,size:Int) =>
            db.withSession { implicit session => sender ! Links.query(page,size)}

        case Remove(db:Database,lid:Long) =>
            db.withSession { implicit session => sender ! Links.remove(lid)}

        case Update(db:Database,link:Link) =>
            db.withSession{ implicit session => sender ! Links.update(link)}
    }
}