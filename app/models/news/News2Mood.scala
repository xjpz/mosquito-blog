package models.news

import akka.actor.Actor
import models.news.NewsActor.{Delete, FindHead}
import scala.slick.driver.MySQLDriver.simple._

object News2Mood extends NewsTrait {

    override val newsListKey = "new2Mood:List"
    override val newsHeadKey = "new2MoodHead"

    override val table = TableQuery[NewsTable](
        (tag: Tag) => new NewsTable(tag, "news2mood")
    )
}

import models.news.NewsActor.{Insert,Query}

class News2MoodsActor extends Actor {
    def receive: Receive = {
        case Insert(db: Database, news: News) => {
            db.withSession { implicit session =>
                sender ! News2Mood.insert(news)
            }
        }case Query(db: Database, page: Int, size: Int) => {
            db.withSession { implicit session =>
                sender ! News2Mood.query(page, size)
            }
        }
        case FindHead(db:Database) => {
            db.withSession{ implicit session =>
                sender ! News2Mood.findHead
            }
        }
        case Delete(db:Database,nid:Long)=>{
            db.withSession{implicit session =>
                sender ! News2Mood.delete(nid)
            }
        }
    }
}
