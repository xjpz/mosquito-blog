package models

import akka.actor.Actor
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._

case class UserInfo (
                    var uid:Option[Long] = None,
                    var rname:Option[String] = None,
                    var descrp:Option[String] = None,
                    var gender:Option[Int] = Option(0),
                    var birthday:Option[String] = None,
                    var area:Option[String] = None,
                    var reg_ip:Option[String] = None,
                    var last_ip:Option[String] = None,
                    var last_time:Option[Long] = None,
                    var credits:Option[Int] = Option(0),
                    var level:Option[Int] = Option(0),
                    var honor:Option[String] = None,
                    var photo:Option[String] = None,
                    var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                    var tombstone: Option[Int] = Option(0)
	                    )

case class UserInfoWrapper(
	                          val uid:Option[Long] = None,
	                          val rname:Option[String] = None,
	                          val descrp:Option[String] = None,
	                          val gender:Option[Int] = None,
	                          val birthday:Option[String] = None,
	                          val area:Option[String] = None,
	                          val reg_ip:Option[String] = None,
	                          val last_ip:Option[String] = None,
	                          val last_time:Option[Long] = None,
	                          val credits:Option[Int] = None,
	                          val level:Option[Int] = None,
	                          val honor:Option[String] = None,
	                          val photo:Option[String] = None,
	                          val inittime: Option[Long] = None,
	                          val updtime: Option[Long] = None,
	                          val tombstone: Option[Int] = None
	                          )

class UserInfos(tag: Tag)
	extends Table[UserInfo](tag, "userinfo") {

	def uid = column[Option[Long]]("uid")
	def rname = column[Option[String]]("rname")
	def descrp = column[Option[String]]("descrp")
	def gender = column[Option[Int]]("gender")
	def birthday = column[Option[String]]("birthday")
	def area = column[Option[String]]("area")
	def reg_ip = column[Option[String]]("reg_ip")
	def last_ip = column[Option[String]]("last_ip")
	def last_time = column[Option[Long]]("last_time")
	def credits = column[Option[Int]]("credits")
	def level = column[Option[Int]]("level")
	def honor = column[Option[String]]("honor")
	def photo = column[Option[String]]("photo")
	def inittime = column[Option[Long]]("init_time")
	def updtime = column[Option[Long]]("update_time")
	def tombstone = column[Option[Int]]("tombstone")

	def * = (uid,rname,descrp,gender,birthday,area,reg_ip,last_ip,last_time,
		credits,level,honor,photo,inittime,updtime,tombstone) <> (UserInfo.tupled,UserInfo.unapply)
}

trait UserInfoJSONTrait {
	implicit val UserInfoJSONFormat = Json.format[UserInfo]
	implicit val UserInfoWrapperJSONFormat = Json.format[UserInfoWrapper]
}

object UserInfos extends UserInfoJSONTrait{
	val table = TableQuery[UserInfos]

	def pack(userInfo: UserInfo) = {
		//....
	}

	def wrap(userInfo: UserInfo):UserInfoWrapper = {
		val wrapper = UserInfoWrapper(
			userInfo.uid,
			userInfo.rname,
			userInfo.descrp,
			userInfo.gender,
			userInfo.birthday,
			userInfo.area,
			userInfo.reg_ip,
			userInfo.last_ip,
			userInfo.last_time,
			userInfo.credits,
			userInfo.level,
			userInfo.honor,
			userInfo.photo,
			userInfo.inittime,
			userInfo.updtime,
			userInfo.tombstone
		)
		wrapper
	}

	def init(userInfo: UserInfo)
	        (implicit session: Session):Option[UserInfoWrapper] = {
		var ret = None:Option[UserInfoWrapper]

		table += userInfo
		ret = Option(wrap(userInfo))
		ret
	}

}

object UserInfoActor{
	case class Init(db:Database,userInfo: UserInfo)
}

import models.UserInfoActor.Init

class UserInfoActor extends Actor {
	override def receive: Receive = {
		case Init(db:Database,userinfo:UserInfo) => {
			db.withSession{ implicit session =>
				sender ! UserInfos.init(userinfo)
			}
		}
	}
}