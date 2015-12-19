package models

import akka.actor.Actor
import models.ArticlesActor.Update
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.json._
import utils.Snoopy

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by xjpz_wz on 2015/7/16.
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
                      var aid: Option[Long] = None
                      )

case class ArticleWrapper(
                             val title: Option[String] = None,
                             val content: Option[String] = None,
                             val catalog: Option[String] = None,
                             val uid: Option[Long] = None,
                             val status: Option[Int] = Option(0),
                             val atype: Option[Int] = Option(0),
                             val read: Option[Int] = Option(0),
                             val smile: Option[Int] = Option(0),
                             val reply: Option[Int] = Option(0),
                             val descrp: Option[String] = None,
                             val inittime: Option[Long] = None,
                             val updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                             val tombstone: Option[Int] = Option(0),
                             val aid: Option[Long] = None
                             )


case class ArticleListWrapper(
                                 val articles: Option[List[ArticleWrapper]],
                                 val count: Option[Int]
                                 )

case class CatalogList(
                          val catalogs: Option[List[String]],
                          val count: Option[Int]
                          )

class Articles(tag: Tag)
    extends Table[Article](tag, "article") {

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

    def * = (title, content, catalog, uid, status, atype, read, smile,reply, descrp, inittime, updtime, tombstone, aid) <>(Article.tupled, Article.unapply)
}

trait ArticleJSONTrait {
    implicit val ArticleJSONFormat = Json.format[Article]
    implicit val ArticleWrapperJSONFormat = Json.format[ArticleWrapper]
    implicit val ArticleListWrapperJSONFormat = Json.format[ArticleListWrapper]
    implicit val CatalogListJSONFormay = Json.format[CatalogList]
}

object Articles extends ArticleJSONTrait {
    val table = TableQuery[Articles]

    //Cache
	private lazy val CacheTime = 3600

    private lazy  val articleListKey = "articleList"
    private lazy val articleUIDlistKey = "articleUIDlist"
    private lazy val articleCatalogListKey = "articleCatalogList"
    private lazy val articleActionRankKey = "articleActionRank:"


    private def pack(article: Article) = {
        val descrp = article.descrp

        if (descrp.isDefined) {
            article.descrp = Snoopy.comp(descrp)
        }

        val catalogOpt = article.catalog

        if (catalogOpt.isDefined) {
            var catalog = catalogOpt.get
            catalog = catalog.replace("，", ",")
            catalog = catalog.replace(".", ",")

            article.catalog = Option(catalog)

        }
    }

    private def wrap(article: Article): ArticleWrapper = {

        val wrapper = ArticleWrapper(
            article.title,
            article.content,
            article.catalog,
            article.uid,
            article.status,
            article.atype,
            article.read,
            article.smile,
            article.reply,
            article.descrp,
            article.inittime,
            article.updtime,
            article.tombstone,
            article.aid
        )
        wrapper
    }

    def wrapForQuery(article: Article): ArticleWrapper = {

        var contentFormat = None: Option[String]
        if (article.content.isDefined) {
            var content = article.content.get
            content = content.replaceAll("<p .*?>", "\r\n")
            content = content.replaceAll("<br\\s*/?>", "\r\n")
            content = content.replaceAll("\\<.*?>", "")
            contentFormat = if (content.length > 200) Option(content.substring(0, 200) + "...") else Option(content)
        }

        val wrapper = ArticleWrapper(
            article.title,
            contentFormat,
            article.catalog,
            article.uid,
            article.status,
            article.atype,
            article.read,
            article.smile,
	        article.reply,
            article.descrp,
            article.inittime,
            article.updtime,
            article.tombstone,
            article.aid
        )
        wrapper
    }

    def init(article: Article)
            (implicit session: Session): Option[ArticleWrapper] = {
        var ret = None: Option[ArticleWrapper]
        pack(article)
        val aid = (table returning table.map(_.aid)) += article
        article.aid = aid
        ret = Option(wrap(article))

        Cache.remove(articleListKey)
	    Cache.remove(articleUIDlistKey)
        Cache.remove(articleCatalogListKey)

        ret
    }

    def retrieve(aid: Long)
                (implicit session: Session): Option[ArticleWrapper] = {
        var ret = None: Option[ArticleWrapper]

        readCount(aid)

        val articleOpt = table.filter(_.aid === aid)
            .filter(_.tombstone === 0).take(1).firstOption
        if (articleOpt.isDefined) {
            val article = articleOpt.get
            ret = Option(wrap(article))
        }
        ret
    }

    def query(page: Int, size: Int)
             (implicit session: Session): Option[ArticleListWrapper] = {
        var ret = None: Option[ArticleListWrapper]

        val articleListWrapperCacheOpt = Cache.getAs[ArticleListWrapper](articleListKey)
        if (articleListWrapperCacheOpt.isDefined) {
            val articleListWrapperCache = articleListWrapperCacheOpt.get
            val articleListWrapperCacheByPage = articleListWrapperCache.articles.get.slice(size * page, size * page + size)
            val articleListWrapperRet = ArticleListWrapper(
                Option(articleListWrapperCacheByPage),
                articleListWrapperCache.count)
            ret = Option(articleListWrapperRet)
        } else {
            val count = table.filter(_.tombstone === 0).length.run

            val queryArticleList = table.filter(_.tombstone === 0)
                .sortBy(_.aid.desc).list
            val articleListTOWrapper = queryArticleList.map(
                (article) => wrapForQuery(article)
            )
            val articleListWrapperCache = ArticleListWrapper(
                Option(articleListTOWrapper),
                Option(count))

            val articleListWrapperRet = ArticleListWrapper(
                Option(articleListTOWrapper.slice(size * page, size * page + size)),
                Option(count))

            Cache.set(articleListKey, articleListWrapperCache,CacheTime)

            ret = Option(articleListWrapperRet)
        }

        ret
    }

	def queryWithUid(uid: Long,page: Int, size: Int)
	                (implicit session: Session): Option[ArticleListWrapper] = {
		var ret = None: Option[ArticleListWrapper]

//		val articleUIDListWrapperCache = Cache.getAs[ArticleListWrapper](articleUIDlistKey)
//		if (articleUIDListWrapperCache.isDefined) {
//			ret = articleUIDListWrapperCache
//		} else {
			val count = table.filter(_.uid === uid).filter(_.tombstone === 0).length.run

			val queryArticleList = table.filter(_.uid === uid).filter(_.tombstone === 0)
				.sortBy(_.aid.desc).list
			val articleListTOWrapper = queryArticleList.map(
				(article) => wrapForQuery(article)
			)
			val articleListWrapper = ArticleListWrapper(
				Option(articleListTOWrapper),
				Option(count))

//			Cache.set(articleUIDlistKey, articleListWrapper,CacheTime)

			ret = Option(articleListWrapper)
//		}
		ret
	}

    def queryAction(action:String,page: Int, size: Int)
                          (implicit session: Session): Option[ArticleListWrapper] = {
        var ret = None: Option[ArticleListWrapper]

        val count = table.filter(_.tombstone === 0).length.run

        var articleListFinal = new ListBuffer[Article]()
        action match {
            case "read" =>
                val queryArticleReadRankCacheOpt = Cache.getAs[List[Article]](articleActionRankKey+"read")
                if(queryArticleReadRankCacheOpt.isDefined){
                    articleListFinal ++= queryArticleReadRankCacheOpt.get
                }else{
                    val queryArticleList = table.filter(_.tombstone === 0).sortBy(_.aid.desc)
                        .sortBy(_.read.desc).drop(page * size).take(size).list
                    articleListFinal ++= queryArticleList

                    Cache.set(articleActionRankKey+"read",queryArticleList,3600)
                }
            case "reply" =>
                val queryArticleReadRankCacheOpt = Cache.getAs[List[Article]](articleActionRankKey+"reply")
                if(queryArticleReadRankCacheOpt.isDefined){
                    articleListFinal ++= queryArticleReadRankCacheOpt.get
                }else{
                    val queryArticleList = table.filter(_.tombstone === 0).sortBy(_.aid.desc)
                        .sortBy(_.reply.desc).drop(page * size).take(size).list
                    articleListFinal ++= queryArticleList

                    Cache.set(articleActionRankKey+"reply",queryArticleList,3600)
                }
            case "smile" =>
                val queryArticleReadRankCacheOpt = Cache.getAs[List[Article]](articleActionRankKey+"smile")
                if(queryArticleReadRankCacheOpt.isDefined){
                    articleListFinal ++= queryArticleReadRankCacheOpt.get
                }else{
                    val queryArticleList = table.filter(_.tombstone === 0).sortBy(_.aid.desc)
                        .sortBy(_.smile.desc).drop(page * size).take(size).list
                    articleListFinal ++= queryArticleList

                    Cache.set(articleActionRankKey+"smile",queryArticleList,3600)
                }
            case _ =>
                val queryArticleReadRankCacheOpt = Cache.getAs[List[Article]](articleActionRankKey+"default")

                if(queryArticleReadRankCacheOpt.isDefined){
                    articleListFinal ++= queryArticleReadRankCacheOpt.get
                }else{
                    val queryArticleList = table.filter(_.tombstone === 0)
                        .sortBy(_.aid.desc).drop(page * size).take(size).list
                    articleListFinal ++= queryArticleList

                    Cache.set(articleActionRankKey+"default",queryArticleList,3600)
                }
        }
        val articleListTOWrapper = articleListFinal.toList.map(
            (article) => wrapForQuery(article)
        )
        val articleListWrapper = ArticleListWrapper(
            Option(articleListTOWrapper),
            Option(count))
        ret = Option(articleListWrapper)

        ret
    }

    def readCount(aid: Long)
                 (implicit session: Session): Boolean = {
        var ret = false
        val updtime = Option(System.currentTimeMillis() / 1000L)
        val articleOpt = table.filter(_.aid === aid)
            .filter(_.tombstone === 0).take(1).firstOption
        if (articleOpt.isDefined) {
            val article = articleOpt.get
            val read = article.read.get
            val updateRead = read + 1
            table.filter(_.aid === aid).filter(_.tombstone === 0)
                .map(row => (row.read, row.updtime)).update((Option(updateRead), updtime))
            ret = true
            Cache.remove(articleListKey)
            Cache.remove(articleUIDlistKey)

            Cache.remove(articleActionRankKey+"read")
            Cache.remove(articleActionRankKey+"smile")
            Cache.remove(articleActionRankKey+"reply")
            Cache.remove(articleActionRankKey+"default")
        }
        ret
    }

    def smileCount(aid: Long)
                  (implicit session: Session): Boolean = {
        var ret = false
        val updtime = Option(System.currentTimeMillis() / 1000L)
        val articleOpt = table.filter(_.aid === aid)
            .filter(_.tombstone === 0).take(1).firstOption
        if (articleOpt.isDefined) {
            val article = articleOpt.get
            val smile = article.smile.get
            val updateSmile = smile + 1
            table.filter(_.aid === aid).filter(_.tombstone === 0)
                .map(row => (row.smile, row.updtime)).update((Option(updateSmile), updtime))
            ret = true
            Cache.remove(articleListKey)
            Cache.remove(articleUIDlistKey)

            Cache.remove(articleActionRankKey+"read")
            Cache.remove(articleActionRankKey+"smile")
            Cache.remove(articleActionRankKey+"reply")
            Cache.remove(articleActionRankKey+"default")
        }
        ret
    }

	def replyCount(aid:Long)
	              (implicit session: Session): Boolean = {
		var ret = false
		val updtime = Option(System.currentTimeMillis() / 1000L)
		val articleOpt = table.filter(_.aid === aid)
			.filter(_.tombstone === 0).take(1).firstOption
		if (articleOpt.isDefined) {
			val article = articleOpt.get
			val reply = article.reply.get
			val updateReply = reply + 1
			table.filter(_.aid === aid).filter(_.tombstone === 0)
				.map(row => (row.reply, row.updtime)).update((Option(updateReply), updtime))
			ret = true
            Cache.remove(articleListKey)
            Cache.remove(articleUIDlistKey)

            Cache.remove(articleActionRankKey+"read")
            Cache.remove(articleActionRankKey+"smile")
            Cache.remove(articleActionRankKey+"reply")
            Cache.remove(articleActionRankKey+"default")
		}
		ret
	}

    def queryWithCatalog(catalog:String,page:Int,size:Int)
                        (implicit session: Session):Option[ArticleListWrapper] = {
        var ret = None: Option[ArticleListWrapper]

        val count = table.filter(_.catalog like "%"+ catalog + "%")
            .filter(_.tombstone === 0).length.run

        val queryArticleList = table.filter(_.catalog like "%"+ catalog + "%")
            .filter(_.tombstone === 0).sortBy(_.aid.desc).drop(page * size).take(size).list

        val articleListTOWrapper = queryArticleList.map(
            (article) => wrap(article)
        )
        val articleListWrapper = ArticleListWrapper(
            Option(articleListTOWrapper),
            Option(count))

        ret = Option(articleListWrapper)
        ret
    }


    def queryCatalog(implicit session: Session): Option[CatalogList] = {
        var ret = None: Option[CatalogList]

        val queryCatalogCacheOpt = Cache.getAs[CatalogList](articleCatalogListKey)
        if(queryCatalogCacheOpt.isDefined){
            ret = Option(queryCatalogCacheOpt.get)
        }else{
            val catalogItemStr = new ListBuffer[String]
            val queryCatalogList = table.filter(_.tombstone === 0).map(_.catalog).run
            if (queryCatalogList.nonEmpty) {
                for (item <- queryCatalogList) {
                    if(item.isDefined){
                        if (item.getOrElse("").contains(",")) {
                            catalogItemStr ++= item.get.split(",").toList
                        } else {
                            catalogItemStr += item.get
                        }
                    }
                }
                val catalogList = CatalogList(
                    Option(catalogItemStr.toList),
                    Option(catalogItemStr.length)
                )
                ret = Option(catalogList)
                Cache.set(articleCatalogListKey,catalogList,CacheTime)
            }
        }
        ret
    }

    def update(article: Article)(implicit session: Session): Option[ArticleWrapper] = {
        var ret = None: Option[ArticleWrapper]
        val updtime = Option(System.currentTimeMillis() / 1000L)

        pack(article)

        val articleOpt = table.filter(_.aid === article.aid).filter(_.uid === article.uid)
            .filter(_.tombstone === 0).take(1).firstOption
        if(articleOpt.isDefined){
            val queryArticle = articleOpt.get

            table.filter(_.aid === article.aid).filter(_.uid === article.uid).filter(_.tombstone === 0)
                .map(row => (row.title,row.content,row.catalog,row.status, row.updtime))
                .update(article.title,article.content,article.catalog,article.status,updtime)

            queryArticle.title = article.title
            queryArticle.content = article.content
            queryArticle.catalog = article.catalog
            queryArticle.status = article.status
            queryArticle.updtime = updtime

            ret = Option(wrap(queryArticle))
        }
        ret
    }

    def delete(aid:Long,uid:Long)(implicit session: Session): Option[ArticleWrapper] = {
        var ret = None: Option[ArticleWrapper]

        val updtime = Option(System.currentTimeMillis() / 1000L)

        val articleOpt = table.filter(_.aid === aid).filter(_.uid === uid)
            .filter(_.tombstone === 0).take(1).firstOption

        if(articleOpt.isDefined){
            val queryArticle = articleOpt.get

            if(Option(aid) == queryArticle.aid && Option(uid) == queryArticle.uid){
                table.filter(_.aid === aid).filter(_.uid === uid).filter(_.tombstone === 0)
                    .map(row => (row.tombstone, row.updtime))
                    .update(Option(1),updtime)

                queryArticle.tombstone = Option(1)
                queryArticle.updtime = updtime
                ret = Option(wrap(queryArticle))
            }
        }

        ret
    }
}

import models.ArticlesActor.{Delete, Init, Query, QueryAction, QueryCatalog, QueryWithCatalog, QueryWithUid, ReadCount, ReplyCount, Retrieve, SmileCount}

object ArticlesActor {

    case class Init(db: Database, article: Article)

    case class Retrieve(db: Database, aid: Long)

    case class ReadCount(db: Database, aid: Long)

    case class SmileCount(db: Database, aid: Long)

	case class ReplyCount(db: Database, aid: Long)

    case class Query(db: Database, page: Int, size: Int)

    case class QueryWithUid(db: Database, uid: Long,page: Int, size: Int)

    case class QueryCatalog(db: Database)

    case class QueryAction(db: Database,action:String, page: Int, size: Int)

    case class QueryWithCatalog(db:Database,catalog:String,page: Int, size: Int)

    case class Update(db:Database,article: Article)

    case class Delete(db:Database,aid:Long,uid:Long)

}

class ArticlesActor extends Actor {
    def receive: Receive = {
        case Init(db: Database, article: Article) =>
            db.withSession { implicit session =>
                sender ! models.Articles.init(article)
            }

        case Retrieve(db: Database, aid: Long) =>
            db.withSession { implicit session =>
                sender ! models.Articles.retrieve(aid)
            }

        case Query(db: Database, page: Int, size: Int) =>
            db.withSession { implicit session =>
                sender ! models.Articles.query(page, size)
            }

        case ReadCount(db: Database, aid: Long) =>
            db.withSession { implicit session =>
                sender ! models.Articles.readCount(aid)
            }

        case SmileCount(db: Database, aid: Long) =>
            db.withSession { implicit session =>
                sender ! models.Articles.smileCount(aid)
            }

        case ReplyCount(db: Database, aid: Long) =>
	        db.withSession { implicit session =>
		        sender ! models.Articles.replyCount(aid)
	        }

        case QueryWithUid(db: Database, uid: Long,page: Int, size: Int) =>
            db.withSession { implicit session =>
                sender ! models.Articles.queryWithUid(uid,page, size)
            }

        case QueryAction(db: Database,action:String, page: Int, size: Int) =>
            db.withSession { implicit session =>
                sender ! models.Articles.queryAction(action, page: Int, size: Int)
            }

        case QueryCatalog(db: Database) =>
            db.withSession { implicit session =>
                sender ! models.Articles.queryCatalog
            }

        case QueryWithCatalog(db:Database,catalog:String,page:Int, size: Int) =>
            db.withSession{ implicit session =>
                sender ! models.Articles.queryWithCatalog(catalog,page,size)
            }

        case Update(db:Database,article:Article) =>
            db.withSession{ implicit session =>
                sender ! models.Articles.update(article)
            }

        case Delete(db:Database,aid:Long,uid:Long) =>
            db.withSession{ implicit session =>
                sender ! models.Articles.delete(aid,uid)
            }

    }
}

