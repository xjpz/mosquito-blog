package models.news

import akka.actor.Actor
import models.news.NewsActor.Query

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by Administrator on 2015/7/27.
 */
object News2Message extends NewsTrait {

    override val newsListKey = "news2Message:List"
    override val newsHeadKey = "news2MessageHead"

    override val table = TableQuery[NewsTable](
        (tag: Tag) => new NewsTable(tag, "news2message")
    )
}

import models.news.NewsActor.Insert

class News2MessagesActor extends Actor {
    def receive: Receive = {
        case Insert(db: Database, news: News) => {
            db.withSession { implicit session =>
                sender ! News2Message.insert(news)
            }
        }
        case Query(db: Database, page: Int, size: Int) => {
            db.withSession { implicit session =>
                sender ! News2Message.query(page, size)
            }
        }
    }
}
