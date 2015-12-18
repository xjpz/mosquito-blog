package models

/**
 * Created by xjpz_wz on 2015/10/5.
 */

import akka.actor.Actor
import play.api.libs.json.Json
import scala.slick.driver.MySQLDriver.simple._

case class Custom (
                  var uid:Option[Long] = None,
                  var descrp:Option[String] = None,
                  var top:Option[String] = None,
                  var right:Option[String] = None,
                  var left:Option[String] = None,
                  var bottom:Option[String] = None,
                  var style:Option[String] = None,
                  var javascript:Option[String] = None,
                  var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                  var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                  var tombstone: Option[Int] = Option(0)
                      )

case class CustomWrapper(
                            val uid:Option[Long] = None,
                            val descrp:Option[String] = None,
                            val top:Option[String] = None,
                            val right:Option[String] = None,
                            val left:Option[String] = None,
                            val bottom:Option[String] = None,
                            val style:Option[String] = None,
                            val javascript:Option[String] = None,
                            val inittime: Option[Long] = None,
                            val updtime: Option[Long] = None,
                            val tombstone: Option[Int] = Option(0)
                            )

class Customs (tag: Tag)
    extends Table[Custom](tag, "ucustom") {

    def uid = column[Option[Long]]("uid")
    def descrp = column[Option[String]]("descrp")
    def top = column[Option[String]]("top")
    def right = column[Option[String]]("right")
    def left = column[Option[String]]("left")
    def bottom = column[Option[String]]("bottom")
    def style = column[Option[String]]("style")
    def javascript = column[Option[String]]("javascript")
    def inittime = column[Option[Long]]("init_time")
    def updtime = column[Option[Long]]("update_time")
    def tombstone = column[Option[Int]]("tombstone")

    def * = (uid,descrp,top,right,left,bottom,style,javascript,inittime,updtime,tombstone) <> (Custom.tupled,Custom.unapply)

}

trait CustomJSONTrait{
    implicit val CustomJSONFormat = Json.format[Custom]
    implicit val customWrapperJSONFormat = Json.format[CustomWrapper]
}

object Customs extends CustomJSONTrait{
    val table = TableQuery[Customs]

    def pack(custom: Custom) = {
        //...
    }

    def wrap(custom: Custom):CustomWrapper = {
        val wrapper = CustomWrapper(
            custom.uid,
            custom.descrp,
            custom.top,
            custom.right,
            custom.left,
            custom.bottom,
            custom.style,
            custom.javascript,
            custom.inittime,
            custom.updtime,
            custom.tombstone
        )
        wrapper
    }

    def init(custom: Custom)
            (implicit session: Session):Option[CustomWrapper] = {
        var ret = None:Option[CustomWrapper]
        table  += custom
        ret = Option(wrap(custom))
        ret
    }

    def retrieve(uid:Long)
                (implicit session: Session):Option[CustomWrapper] = {

        var ret = None:Option[CustomWrapper]
        val customOpt = table.filter(_.uid === uid)
            .filter(_.tombstone === 0).take(1).firstOption

        if(customOpt.isDefined){
            ret = Option(wrap(customOpt.get))
        }

        ret
    }

}

object CustomsActor{
    case class Init(db:Database,custom: Custom)
    case class Retrieve(db:Database,uid:Long)
}

import models.CustomsActor.{Init,Retrieve}

class CustomsActor extends Actor{
    override def receive: Receive = {
        case Init(db:Database,custom:Custom) => {
            db.withSession{implicit session =>
                sender ! models.Customs.init(custom)
            }
        }
        case Retrieve(db:Database,uid:Long) => {
            db.withSession{implicit session =>
                sender ! models.Customs.retrieve(uid)
            }
        }
    }
}