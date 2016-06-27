package models.reply

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.language.postfixOps

/**
  * Created by xjpz on 2016/5/29.
  */

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
                  var rid: Option[Long] = None)

case class ReplyListWrapper(articles: List[Reply], count:Int)

case class ReplyListTree(
                         queryReply:Seq[Reply],reply:Reply, tree:List[Reply])

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

trait Replys {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)

  val table = TableQuery[ReplysTable](
    (tag: Tag) => new ReplysTable(tag, "")
  )

  def _queryById(id: Long): DBIO[Option[Reply]] = table.filter(_.rid === id).filter(_.tombstone === 0).result.headOption

  def _queryByAid(aid:Long):DBIO[Seq[Reply]] = {
    table.filter(_.aid === aid).filter(_.tombstone === 0).result
  }

  def retrieve(aid: Long): Future[Option[Reply]] = {
    dbConfig.db.run(_queryById(aid))
  }

  def queryByAid(aid:Long):Future[Seq[Reply]] = {
    dbConfig.db.run(_queryByAid(aid))
  }

  def init(reply: Reply):Future[Option[Long]] = {
    dbConfig.db.run((table returning table.map(_.rid)) += reply)
  }

  def updateSmileCount(rid:Long,smile:Int):Future[Int] = {
    dbConfig.db.run(
      table.filter(_.rid === rid).filter(_.tombstone === 0).
        map( row => (row.smile,row.updtime)).update(Some(smile),Option(System.currentTimeMillis()/1000)))
  }

  @annotation.tailrec
  final def parseReplyTree(ridSuperSeq:Seq[Long],queryReplySeq:Seq[Reply],childReplyRetList:ListBuffer[Reply]):ListBuffer[Reply] = {
    val childReplyFilterList = queryReplySeq.filter(i => ridSuperSeq.contains(i.quote.get))

    if(childReplyFilterList.nonEmpty){
      childReplyRetList ++= childReplyFilterList
      val ridSeq:Seq[Long] = childReplyFilterList.map(_.rid.get)

      parseReplyTree(ridSeq,queryReplySeq,childReplyRetList)
    }else{
      childReplyRetList
    }
  }

}