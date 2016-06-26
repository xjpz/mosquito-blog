package models

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xjpz on 2016/5/28.
  */

case class Article(
                    var title: Option[String] = None,
                    var content: Option[String] = None,
                    var catalog: Option[String] = None,
                    var uid: Option[Long] = None,
                    var status: Option[Int] = Option(0),
                    var atype: Option[Int] = Option(0),
                    var read: Option[Int] = Option(0),
                    var smile: Option[Int] = Option(0),
                    var reply: Option[Int] = Option(0),
                    var descrp: Option[String] = None,
                    var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    var tombstone: Option[Int] = Option(0),
                    var aid: Option[Long] = None) {

  def patch(article: Article) :Article= {
    this.copy(
      title = article.title.orElse(this.title),
      content = article.content.orElse(this.content),
      catalog = article.catalog.orElse(this.catalog),
      uid = this.uid,
      status = article.status.orElse(this.status),
      atype = article.atype.orElse(this.atype),
      read = article.read.orElse(this.read),
      smile = article.smile.orElse(this.smile),
      reply = article.read.orElse(this.reply),
      descrp = article.descrp.orElse(this.descrp),
      inittime = this.inittime,
      updtime = Option(System.currentTimeMillis() / 1000L),
      tombstone = article.tombstone.orElse(this.tombstone),
      aid = this.aid
    )
  }

  def pack(article: Article) = {

    val catalogOpt = article.catalog

    if (catalogOpt.isDefined) {
      var catalog = catalogOpt.get
      catalog = catalog.replace("，", ",")
      catalog = catalog.replace(".", ",")
      article.catalog = Option(catalog)
    }
  }

  def wrapArticleList() = {
    if (this.content.isDefined) {
      var content = this.content.get
      content = content.replaceAll("<p .*?>", "\r\n")
      content = content.replaceAll("<br\\s*/?>", "\r\n")
      content = content.replaceAll("\\<.*?>", "")
      this.content = if (content.length > 200) Option(content.substring(0, 200) + "...") else Option(content)
    }
  }
}

case class ArticleListWrapper(articles: List[Article], count: Int)

class Articles @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  class ArticlesTable(tag: Tag) extends Table[Article](tag, "article") {

    def aid = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def title = column[Option[String]]("title")

    def content = column[Option[String]]("content")

    def catalog = column[Option[String]]("catalog")

    def uid = column[Option[Long]]("uid")

    def status = column[Option[Int]]("status")

    def atype = column[Option[Int]]("type")

    def read = column[Option[Int]]("read")

    def smile = column[Option[Int]]("smile")

    def reply = column[Option[Int]]("reply")

    def descrp = column[Option[String]]("descrp")

    def tombstone = column[Option[Int]]("tombstone")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def * = (title, content, catalog, uid, status, atype, read, smile, reply, descrp, inittime, updtime, tombstone, aid) <>(Article.tupled, Article.unapply)
  }

  val table = TableQuery[ArticlesTable]

  def _queryCatalog :DBIO[Seq[String]] = table.filter(_.catalog.isDefined).filter(_.tombstone === 0).map(_.catalog.get).result
  def _queryByReadCount(page:Int,size:Int):DBIO[Seq[Article]] = table.filter(_.tombstone === 0).sortBy(_.aid.desc).sortBy(_.read.desc).drop(page * size).take(size).result
  def _queryByReplyCount(page:Int,size:Int):DBIO[Seq[Article]] = table.filter(_.tombstone === 0).sortBy(_.aid.desc).sortBy(_.reply.desc).drop(page * size).take(size).result
  def _queryBySmileCount(page:Int,size:Int):DBIO[Seq[Article]] = table.filter(_.tombstone === 0).sortBy(_.aid.desc).sortBy(_.smile.desc).drop(page * size).take(size).result

  def queryById(id: Long): DBIO[Option[Article]] = table.filter(_.aid === id).result.headOption

  def retrieve(aid: Long): Future[Option[Article]] = {
    db.run(queryById(aid))
  }

  def query:Future[Seq[Article]] = {
    db.run(table.filter(_.tombstone === 0).sortBy(_.aid.desc).result)
  }

  def queryCatalog:Future[Seq[String]] = {
    db.run(_queryCatalog)
  }

  def queryByReadCount(page:Int = 0,size:Int = 5):Future[Seq[Article]] = {
    db.run(_queryByReadCount(page, size))
  }

  def queryByReplyCount(page:Int = 0,size:Int = 5):Future[Seq[Article]] = {
    db.run(_queryByReplyCount(page,size))
  }

  def queryBySmileCount(page:Int = 0,size:Int = 5):Future[Seq[Article]] = {
    db.run(_queryBySmileCount(page,size))
  }

  def queryByCatalog(catalog:String) : Future[Seq[Article]] = {
    db.run(table.filter(_.catalog like "%" + catalog + "%").filter(_.tombstone === 0).sortBy(_.aid.desc).result)
  }

  def queryByUid(uid:Long): Future[Seq[Article]] = {
    db.run(table.filter(_.uid === uid).filter(_.tombstone === 0).sortBy(_.aid.desc).result)
  }

  def init(article: Article):Future[Option[Long]] = {
    db.run((table returning table.map(_.aid)) += article)
  }

  def update(article: Article):Future[Int] = {
    val query = table.filter(_.aid === article.aid)

    val update = query.result.head.flatMap { queryArticle =>
      query.update(queryArticle.patch(article))
    }
    db.run(update)
  }

  def updateReadCount(aid:Long,read:Int):Future[Int] = {
    db.run(
      table.filter(_.aid === aid).filter(_.tombstone === 0).
        map( row => (row.read,row.updtime)).update(Some(read),Option(System.currentTimeMillis()/1000)))
  }

  def updateSmileCount(aid:Long,smile:Int):Future[Int] = {
    db.run(
      table.filter(_.aid === aid).filter(_.tombstone === 0).
        map( row => (row.smile,row.updtime)).update(Some(smile),Option(System.currentTimeMillis()/1000)))
  }

  def revoke(aid:Long):Future[Int] = {
    db.run(
      table.filter(_.aid === aid).filter(_.tombstone === 0).
        map(row => (row.tombstone,row.updtime)).update(Some(1),Option(System.currentTimeMillis()/1000)))
  }
}