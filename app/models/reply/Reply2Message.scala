package models.reply

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by xjpz on 2016/5/29.
  */

case class Reply2Message(
                          var aid: Option[Long] = None,
                          var uid: Option[Long] = None,
                          var name: Option[String] = None,
                          var url: Option[String] = None,
                          var email: Option[String] = None,
                          var content: Option[String] = None,
                          var quote: Option[Long] = Option(0),
                          var smile: Option[Int] = Option(0),
                          var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                          var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                          var tombstone: Option[Int] = Option(0),
                          var rid: Option[Long] = None)

case class Reply2MessageListWrapper(articles: List[Reply2Message], count: Int)

case class Reply2MessageListTree(queryReply: Seq[Reply2Message], reply: Reply2Message, tree: List[Reply2Message])

object Reply2Message {
  implicit val ReplyJSONFormat = Json.format[Reply2Message]
}

object Reply2MessageListWrapper {
  implicit val ReplyListWrapperJSONFormat = Json.format[Reply2MessageListWrapper]
}

object Reply2MessageListTree {
  implicit val ReplyListTreeJSONFormat = Json.format[Reply2MessageListTree]
}

class Reply2Messages @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  private class Reply2MessagesTable(tag: Tag) extends Table[Reply2Message](tag, "reply_message") {
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

    def * = (aid, uid, name, url, email, content, quote, smile, inittime, updtime, tombstone, rid) <> ((Reply2Message.apply _).tupled, Reply2Message.unapply)
  }

  private val table = TableQuery[Reply2MessagesTable]

  def retrieve(aid: Long): Future[Option[Reply2Message]] = {
    dbConfig.db.run(table.filter(_.rid === aid).filter(_.tombstone === 0).result.headOption)
  }

  def queryByAid(aid: Long): Future[Seq[Reply2Message]] = {
    dbConfig.db.run(table.filter(_.aid === aid).filter(_.tombstone === 0).result)
  }

  def init(reply: Reply2Message): Future[Option[Long]] = {
    dbConfig.db.run((table returning table.map(_.rid)) += reply)
  }

  def updateSmileCount(rid: Long, smile: Int): Future[Int] = {
    dbConfig.db.run(
      table.filter(_.rid === rid).filter(_.tombstone === 0).
        map(row => (row.smile, row.updtime)).update(Some(smile), Option(System.currentTimeMillis() / 1000)))
  }

  @annotation.tailrec
  final def parseReplyTree(ridSuperSeq: Seq[Long], queryReplySeq: Seq[Reply2Message], childReplyRetList: ListBuffer[Reply2Message]): ListBuffer[Reply2Message] = {
    val childReplyFilterList = queryReplySeq.filter(i => ridSuperSeq.contains(i.quote.get))

    if (childReplyFilterList.nonEmpty) {
      childReplyRetList ++= childReplyFilterList
      val ridSeq: Seq[Long] = childReplyFilterList.map(_.rid.get)

      parseReplyTree(ridSeq, queryReplySeq, childReplyRetList)
    } else {
      childReplyRetList
    }
  }

}
