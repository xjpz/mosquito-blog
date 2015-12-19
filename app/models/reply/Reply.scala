package models

import globals.Global
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.slick.driver.MySQLDriver.simple._

// Akka imports

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

case class Reply(
                    var aid: Option[Long] = None,
                    var uid: Option[Long] = None,
                    var name: Option[String] = None,
                    var url: Option[String] = None,
                    var email: Option[String] = None,
                    var content: Option[String] = None,
                    var quote: Option[Long] = Option(0),
                    var smile:Option[Int] = Option(0),
                    var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    var tombstone: Option[Int] = Option(0),
                    var rid: Option[Long] = None
                    )

case class ReplyWrapper(
                           val aid: Option[Long] = None,
                           val uid: Option[Long] = None,
                           val name: Option[String] = None,
                           val url: Option[String] = None,
                           val email: Option[String] = None,
                           val content: Option[String] = None,
                           val quote: Option[Long] = None,
                           val smile:Option[Int] = None,
                           val inittime: Option[Long] = None,
                           val updtime: Option[Long] = None,
                           val tombstone: Option[Int] = None,
                           val rid: Option[Long] = None
                           )

case class ReplyListWrapper(
                               val replys: Option[List[ReplyWrapper]],
                               val count: Option[Int]
                               )


class ReplysTable(tag: Tag, table: String) extends Table[Reply](tag, table) {
    def rid = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def aid = column[Option[Long]]("aid")

    def uid = column[Option[Long]]("uid")

    def name = column[Option[String]]("name")

    def url = column[Option[String]]("url")

    def email = column[Option[String]]("email")

    def content = column[Option[String]]("content")

    def quote = column[Option[Long]]("quote")

    def smile = column[Option[Int]]("smile")

    def tombstone = column[Option[Int]]("tombstone")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def * = (aid, uid, name, url, email, content, quote,smile, inittime, updtime, tombstone, rid) <>(Reply.tupled, Reply.unapply)
}

trait ReplyJSONTrait {
    implicit val ReplyJSONFormat = Json.format[Reply]
    implicit val ReplyWrapperJSONFormat = Json.format[ReplyWrapper]
    implicit val ReplayListWrapperJSONFormat = Json.format[ReplyListWrapper]
}

trait ReplysTrait extends ReplyJSONTrait {
    val table = TableQuery[ReplysTable](
        (tag: Tag) => new ReplysTable(tag, "")
    )

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val aActor = system.actorOf(Props(new ArticlesActor()))

    def pack(reply: Reply) = {

    }

    def wrap(reply: Reply): ReplyWrapper = {

        val wrapper = ReplyWrapper(
            reply.aid,
            reply.uid,
            reply.name,
            reply.url,
            reply.email,
            reply.content,
            reply.quote,
            reply.smile,
            reply.inittime,
            reply.updtime,
            reply.tombstone,
            reply.rid
        )
        wrapper
    }

    def init(reply: Reply)
            (implicit session: Session): Option[ReplyWrapper] = {
        var ret = None: Option[ReplyWrapper]
        val id = (table returning table.map(_.rid)) += reply
        reply.rid = id
        ret = Option(wrap(reply))

        val future = aActor ! ArticlesActor.ReplyCount(Global.db, reply.aid.get)
//        val flag = Await.result(future, timeout.duration)
//            .asInstanceOf[Boolean]

        ret
    }

    def query(aid: Long)
             (implicit session: Session): Option[ReplyListWrapper] = {
        var ret = None: Option[ReplyListWrapper]

        val replyListOpt = table.filter(_.aid === aid).filter(_.quote === 0L).filter(_.tombstone === 0).sortBy(_.rid.asc).list

        val replyListWrapper = replyListOpt.map(
            (reply) => wrap(reply)
        )

        ret = Option(
            ReplyListWrapper(Option(replyListWrapper), Option(replyListWrapper.length))
        )
        ret
    }

    def queryChild(rid:Long)
                      (implicit session: Session): Option[ReplyListWrapper] = {
        var ret = None: Option[ReplyListWrapper]

        queryAllChild(rid)
        val result = reply2ArticleChildListBuff.toList.sortBy(_.inittime)

        ret = Option(
            ReplyListWrapper(Option(result), Option(result.length))
        )
        reply2ArticleChildListBuff.clear()
        ret
    }

    val reply2ArticleChildListBuff = new ListBuffer[ReplyWrapper]()

    def queryAllChild(rid:Long)(implicit session: Session):Unit = {

        val replyListOpt = table.filter(_.quote === rid).filter(_.tombstone === 0).sortBy(_.rid.asc).list
        if(replyListOpt.nonEmpty){

            val replyListWrapper = replyListOpt.map(
                (reply) => wrap(reply)
            )
            reply2ArticleChildListBuff ++= replyListWrapper
            replyListOpt.foreach(iter => queryAllChild(iter.rid.get))
        }

    }

    def retrieve(rid:Long)
                (implicit session: Session): Option[ReplyWrapper] = {
        var ret = None: Option[ReplyWrapper]
        val replyOpt = table.filter(_.rid === rid)
            .filter(_.tombstone === 0).take(1).firstOption
        if(replyOpt.isDefined){
            ret = Option(wrap(replyOpt.get))
        }
        ret
    }

    def smile(rid:Long)(implicit session: Session): Option[ReplyWrapper] = {
        var ret = None: Option[ReplyWrapper]
        val updtime = Option(System.currentTimeMillis() / 1000L)

        val replyOpt = table.filter(_.rid===rid).filter(_.tombstone===0).take(1).firstOption
        if(replyOpt.isDefined){
            val reply = replyOpt.get
            val smile = reply.smile.getOrElse(0)
            val updateSmile = smile + 1

            table.filter(_.rid === rid).filter(_.tombstone === 0)
                .map(row => (row.smile, row.updtime)).update((Option(updateSmile), updtime))
            reply.smile = Option(updateSmile)
            reply.updtime = updtime
            ret = Option(wrap(reply))
        }
        ret
    }
}

object ReplysActor {

    case class Init(db: Database, reply: Reply)

    case class Query(db: Database, aid: Long)

    case class QueryChild(db: Database, rid: Long)

    case class Retrieve(db:Database,rid:Long)

    case class Smile(db:Database,rid:Long)

}

