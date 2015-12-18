package models

import akka.actor.Actor
import models.ReplysActor.QueryChild

import scala.slick.driver.MySQLDriver.simple._

object Reply2Message extends ReplysTrait{
    override val table = TableQuery[ReplysTable](
        (tag: Tag) => new ReplysTable(tag, "reply2message")
    )
}

import models.ReplysActor.{Init, Query, Retrieve,Smile}

class Replys2MessageActor extends Actor {
    def receive: Receive = {
        case Init(db: Database, reply: Reply) => {
            db.withSession { implicit session =>
                sender ! models.Reply2Message.init(reply)
            }
        }
        case Query(db: Database, aid: Long) => {
            db.withSession { implicit session =>
                sender !  models.Reply2Message.query(aid)
            }
        }
        case QueryChild(db: Database, rid: Long) => {
            db.withSession { implicit session =>
                sender !  models.Reply2Message.queryChild(rid)
            }
        }
        case Retrieve(db: Database, rid: Long) => {
            db.withSession { implicit session =>
                sender ! models.Reply2Message.retrieve(rid)
            }
        }
        case Smile(db:Database,rid:Long) => {
            db.withSession{ implicit session =>
                sender ! models.Reply2Message.smile(rid)
            }
        }
    }
}