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

case class Reply2Article(
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

case class Reply2ArticleListWrapper(articles: List[Reply2Article], count: Int)

case class Reply2ArticleListTree(queryReply: Seq[Reply2Article], reply: Reply2Article, tree: List[Reply2Article])

object Reply2Article {
  implicit val ReplyJSONFormat = Json.format[Reply2Article]
}

object Reply2ArticleListWrapper {
  implicit val ReplyListWrapperJSONFormat = Json.format[Reply2ArticleListWrapper]
}

object Reply2ArticleListTree {
  implicit val ReplyListTreeJSONFormat = Json.format[Reply2ArticleListTree]
}

class Reply2Articles @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  private class Reply2ArticlesTable(tag: Tag) extends Table[Reply2Article](tag, "reply_article") {
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

    def * = (aid, uid, name, url, email, content, quote, smile, inittime, updtime, tombstone, rid) <> ((Reply2Article.apply _).tupled, Reply2Article.unapply)
  }

  private val table = TableQuery[Reply2ArticlesTable]

  def retrieve(aid: Long): Future[Option[Reply2Article]] = {
    dbConfig.db.run(table.filter(_.rid === aid).filter(_.tombstone === 0).result.headOption)
  }

  def queryByAid(aid: Long): Future[Seq[Reply2Article]] = {
    dbConfig.db.run(table.filter(_.aid === aid).filter(_.tombstone === 0).result)
  }

  def init(reply: Reply2Article): Future[Option[Long]] = {
    dbConfig.db.run((table returning table.map(_.rid)) += reply)
  }

  def updateSmileCount(rid: Long, smile: Int): Future[Int] = {
    dbConfig.db.run(
      table.filter(_.rid === rid).filter(_.tombstone === 0).
        map(row => (row.smile, row.updtime)).update(Some(smile), Option(System.currentTimeMillis() / 1000)))
  }

  @annotation.tailrec
  final def parseReplyTree(ridSuperSeq: Seq[Long], queryReplySeq: Seq[Reply2Article], childReplyRetList: ListBuffer[Reply2Article]): ListBuffer[Reply2Article] = {
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
