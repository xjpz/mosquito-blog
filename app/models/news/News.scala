package models.news

import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.json._

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by Administrator on 2015/7/27.
 */
case class News(
                   var content: Option[String] = None,
                   var uid: Option[Long] = Option(0),
                   var name: Option[String] = None,
                   var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                   var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                   var tombstone: Option[Int] = Option(0),
                   var id: Option[Long] = None
                   )

case class NewsWrapper(
                          val content: Option[String] = None,
                          val uid: Option[Long] = None,
                          val name: Option[String] = None,
                          val inittime: Option[Long] = Option(0),
                          val updtime: Option[Long] = Option(0),
                          val tombstone: Option[Int] = Option(0),
                          val id: Option[Long] = None
                          )

case class NewsListWrapper(
                              val news: Option[List[NewsWrapper]],
                              val count: Option[Int]
                              )

class NewsTable(tag: Tag, table: String) extends Table[News](tag, table) {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def content = column[Option[String]]("content")

    def uid = column[Option[Long]]("uid")

    def name = column[Option[String]]("name")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def tombstone = column[Option[Int]]("tombstone")

    def * = (content, uid, name, inittime, updtime, tombstone, id) <>(News.tupled, News.unapply)
}

trait NewsJSONTrait {
    implicit val NewsJSONFormat = Json.format[News]
    implicit val NewsWrapperJSONFormat = Json.format[NewsWrapper]
    implicit val NewsListWrapperJSONFormat = Json.format[NewsListWrapper]
}

trait NewsTrait extends NewsJSONTrait {

    val newsListKey = ""
    val newsHeadKey = ""

    val table = TableQuery[NewsTable](
        (tag: Tag) => new NewsTable(tag, "")
    )

    private def pack(news: News) = {
        //TODO
    }

    private def wrap(news: News): NewsWrapper = {

        val wrapper = NewsWrapper(
            news.content,
            news.uid,
            news.name,
            news.inittime,
            news.updtime,
            news.tombstone,
            news.id
        )

        wrapper
    }

    def insert(news: News)
              (implicit session: Session): Option[NewsWrapper] = {
        var ret = None: Option[NewsWrapper]

        val id = (table returning table.map(_.id)) += news
        news.id = id

        ret = Option(wrap(news))

        //remove Cache
        Cache.remove(newsListKey)
        Cache.remove(newsHeadKey)

        ret
    }

    def query(page: Int, size: Int)(implicit session: Session): Option[NewsListWrapper] = {
        var ret = None: Option[NewsListWrapper]
        val newsListWrapperCache = Cache.getAs[NewsListWrapper](newsListKey)
        if (newsListWrapperCache.isDefined) {
            ret = newsListWrapperCache
        } else {
            val count = table.filter(_.tombstone === 0).length.run
            val newsListOpt = table.filter(_.tombstone === 0)
                .sortBy(_.id.asc).drop(page * size).take(size).list
            val newsListTOWrapper = newsListOpt.map(
                (news) => wrap(news)
            )

            val newsListWrapper = NewsListWrapper(
                Option(newsListTOWrapper),
                Option(count)
            )
            Cache.set(newsListKey,newsListWrapper,3600)
            ret = Option(newsListWrapper)
        }
        ret
    }

    def findHead (implicit session: Session): Option[NewsWrapper] = {
        var ret = None: Option[NewsWrapper]

        val news2HeadOpt = Cache.getAs[NewsWrapper](newsHeadKey)
        if(news2HeadOpt.isDefined){
            ret = news2HeadOpt
        }else{
            val newsWrapperOpt = table.filter(_.tombstone === 0) .sortBy(_.id.desc).take(1).firstOption
            if(newsWrapperOpt.isDefined){
                val wrapper = wrap(newsWrapperOpt.get)
                Cache.set(newsHeadKey,wrapper,3600)
                ret = Option(wrapper)
            }
        }

        ret
    }

    def delete(nid:Long)(implicit session: Session): Option[NewsWrapper] = {
        var ret = None: Option[NewsWrapper]

        val updtime = Option(System.currentTimeMillis() / 1000L)

        val queryNewOpt = table.filter(_.id === nid).filter(_.tombstone === 0).take(1).firstOption
        if(queryNewOpt.isDefined){

            val news = queryNewOpt.get

            table.filter(_.id === nid).filter(_.tombstone === 0)
                .map(row => (row.tombstone, row.updtime))
                .update(Option(1),updtime)

            news.tombstone = Option(1)
            news.updtime = updtime

            ret = Option(wrap(news))
        }

        ret
    }
}

object NewsActor {

    case class Insert(db: Database, news: News)

    case class Query(db: Database, page: Int, size: Int)

    case class FindHead(db:Database)

    case class Delete(db:Database,nid:Long)

}