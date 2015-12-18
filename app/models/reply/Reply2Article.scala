package models

import akka.actor.Actor
import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by xjpz_wz on 2015/9/7.
 */
object Reply2Article extends ReplysTrait{
    override val table = TableQuery[ReplysTable](
        (tag: Tag) => new ReplysTable(tag, "reply2article")
    )
}

import models.ReplysActor.{Init, Query, Retrieve,QueryChild,Smile}

class Replys2ArticleActor extends Actor {
    def receive: Receive = {
        case Init(db: Database, reply: Reply) => {
            db.withSession { implicit session =>
                sender ! models.Reply2Article.init(reply)
            }
        }
        case Query(db: Database, aid: Long) => {
            db.withSession { implicit session =>
                sender !  models.Reply2Article.query(aid)
            }
        }
        case QueryChild(db: Database, rid: Long) => {
            db.withSession { implicit session =>
                sender !  models.Reply2Article.queryChild(rid)
            }
        }
        case Retrieve(db: Database, rid: Long) => {
            db.withSession { implicit session =>
                sender ! models.Reply2Article.retrieve(rid)
            }
        }
        case Smile(db:Database,rid:Long) => {
            db.withSession{ implicit session =>
                sender ! models.Reply2Article.smile(rid)
            }
        }
    }
}