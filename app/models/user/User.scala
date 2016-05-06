package models

import akka.actor.Actor
import globals.Global
import org.apache.commons.codec.binary.Base64
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.json._

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by Administrator on 2015/7/14.
 */
case class User(
                   var name: Option[String] = None,
                   var password: Option[String] = None,
                   var email: Option[String] = None,
                   var phone: Option[String] = None,
                   var descrp: Option[String] = None,
                   var utype: Option[Int] = None,
                   var status: Option[Int] = None,
                   var qopenid:Option[String] = None,
                   var qtoken:Option[String] = None,
                   var sopenid:Option[String] = None,
                   var stoken:Option[String] = None,
                   var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                   var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                   var tombstone: Option[Int] = Option(0),
                   var uid: Option[Long] = None
                   )

case class UserWrapper(
                          val name: Option[String] = None,
                          val password: Option[String] = None,
                          val email: Option[String] = None,
                          val phone: Option[String] = None,
                          val descrp: Option[String] = None,
                          val utype: Option[Int] = Option(0),
                          val status: Option[Int] = Option(0),
                          val qopenid:Option[String] = None,
                          val qtoken:Option[String] = None,
                          val sopenid:Option[String] = None,
                          val stoken:Option[String] = None,
                          val inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                          val updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                          val tombstone: Option[Int] = Option(0),
                          val uid: Option[Long] = None
                          )

case class UserListWrapper(
                          val users:Option[List[UserWrapper]],
                          val count: Option[Int]
)

class Users(tag: Tag)
    extends Table[User](tag, "user") {

    def uid = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def name = column[Option[String]]("name")

    def password = column[Option[String]]("password")

    def email = column[Option[String]]("email")

    def phone = column[Option[String]]("phone")

    def descrp = column[Option[String]]("descrp")

    def utype = column[Option[Int]]("type")

    def status = column[Option[Int]]("status")

    def qopenid = column[Option[String]]("qopenid")

    def qtoken = column[Option[String]]("qtoken")

    def sopenid = column[Option[String]]("sopenid")

    def stoken = column[Option[String]]("stoken")

    def tombstone = column[Option[Int]]("tombstone")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def * = (name, password, email, phone, descrp, utype, status, qopenid,qtoken,sopenid,stoken,inittime, updtime, tombstone, uid) <>(User.tupled, User.unapply)
}

trait UserJSONTrait {
    implicit val UserJSONFormat = Json.format[User]
    implicit val UserWrapperJSONFormat = Json.format[UserWrapper]

    implicit val UserListWrapperJSONFormat = Json.format[UserListWrapper]
}

object Users extends UserJSONTrait {
    val table = TableQuery[Users]

    //set Keys
    private lazy  val activeUserListKey = "activeUserList"

    def pack(user: User) = {
        val password = user.password
        if (password.isDefined) {
            user.password = Option(Base64.encodeBase64String(Global.shaEncoder.digest(password.get.getBytes)))
        }
    }

    def wrap(user: User): UserWrapper = {

        val wrapper = UserWrapper(
            user.name,
            user.password,
            user.email,
            user.phone,
            user.descrp,
            user.utype,
            user.status,
            user.qopenid,
            user.qtoken,
            user.sopenid,
            user.stoken,
            user.inittime,
            user.updtime,
            user.tombstone,
            user.uid
        )
        wrapper
    }

    def init(user: User)
            (implicit session: Session): Option[UserWrapper] = {
        var retOpt = None: Option[UserWrapper]
        pack(user)
        val uid = (table returning table.map(_.uid)) += user
        user.uid = uid
        retOpt = Option(wrap(user))

        Cache.remove(activeUserListKey)

        retOpt
    }

    def find(uid: Long)
            (implicit session: Session): Option[UserWrapper] = {
        var retOpt = None: Option[UserWrapper]
        val userOpt = table.filter(_.uid === uid).filter(_.tombstone === 0).take(1).firstOption
        if (userOpt.isDefined) {
            val user = userOpt.get
            retOpt = Option(wrap(user))
        }
        retOpt
    }

    def findByName(name: String)
                  (implicit session: Session): Option[UserWrapper] = {
        var retOpt = None: Option[UserWrapper]
        val userOpt = table.filter(_.name === name).filter(_.tombstone === 0).take(1).firstOption
        if (userOpt.isDefined) {
            val user = userOpt.get
            retOpt = Option(wrap(user))
        }
        retOpt
    }

    def findByEmail(email:String)
                  (implicit session: Session):Option[UserWrapper] = {
        var retOpt = None: Option[UserWrapper]
        val userOpt = table.filter(_.email === email).filter(_.tombstone === 0).take(1).firstOption
        if (userOpt.isDefined) {
            val user = userOpt.get
            retOpt = Option(wrap(user))
        }
        retOpt
    }

	def findByPhone(phone:String)
	               (implicit session: Session):Option[UserWrapper] = {
		var retOpt = None: Option[UserWrapper]
		val userOpt = table.filter(_.phone === phone).filter(_.tombstone === 0).take(1).firstOption
		if (userOpt.isDefined) {
			val user = userOpt.get
			retOpt = Option(wrap(user))
		}
		retOpt
	}

    def updatePassWord(uid: Long, passwd: String)
                      (implicit session: Session): Boolean = {
        var retOpt = false
        val updtime = Option(System.currentTimeMillis() / 1000L)
        val userOpt = table.filter(_.uid === uid).filter(_.tombstone === 0).take(1).firstOption
        if (userOpt.isDefined) {
            val user = userOpt.get
            if (user.uid == Option(uid)) {
                table.filter(_.uid === uid).filter(_.tombstone === 0)
                    .map(row => (row.password, row.updtime)).update((Option(passwd), updtime))
                retOpt = true
            }
        }
        retOpt
    }

    def userIsExists(uname:String)(implicit session: Session) :Boolean= {
        var ret = false

        uname match {
            case x if table.map(_.name).run.mkString.toUpperCase.contains(x.toUpperCase) =>  ret = true
            case x if table.map(_.email).run.mkString.toUpperCase.contains(x.toUpperCase)=>  ret = true
            case x if table.map(_.phone).run.contains(Option(x)) =>  ret = true
            case _ =>
        }
        ret
    }

    def queryActiveUser(page:Int,size:Int)(implicit session: Session):Option[UserListWrapper] = {
        var retOpt = None:Option[UserListWrapper]

        val activeUserListCacheOpt = Cache.getAs[UserListWrapper](activeUserListKey)
        if(activeUserListCacheOpt.isDefined){
            retOpt = Option(activeUserListCacheOpt.get)

        }else{
            val userListWrapperOpt = table.filter(_.tombstone === 0).sortBy(_.uid.asc).list
            val userListTOWrapper = userListWrapperOpt.map(user => wrap(user))

            val retUserListWrapper = UserListWrapper(
                Option(userListTOWrapper.slice(size * page, size * page + size)),
                Option(userListTOWrapper.length)
            )

            Cache.set(activeUserListKey,retUserListWrapper,3600)
            retOpt = Option(retUserListWrapper)

        }

        retOpt
    }

    //QqConnect

    def findOpenid(otype:Int,openid:String)(implicit session: Session):Option[UserWrapper] = {
        var retOpt = None: Option[UserWrapper]
        otype match {
            case 1 =>
                //x==1 qopenid  腾讯QQ
                val userOpt = table.filter(_.qopenid===openid).filter(_.tombstone===0).take(1).firstOption
                if(userOpt.isDefined){
                    retOpt = Option(wrap(userOpt.get))
                }

            case 2 =>
                //x==2 sopenid 新浪微博
                val userOpt = table.filter(_.sopenid===openid).filter(_.tombstone===0).take(1).firstOption
                if(userOpt.isDefined){
                    retOpt = Option(wrap(userOpt.get))
                }

            case _ => //不匹配，不做任何操作
        }
        retOpt
    }

    def addQConnUser(user:User)(implicit session: Session):Option[UserWrapper] = {
        var retOpt = None:Option[UserWrapper]

	    //判断openid是否存在。

	    // 如果openid存在，则说明此用户之前登录过或者已与本地user表中的用户绑定。写入cookie，使用户为登录状态，到此结束。

	    //如果用户openid不存在，则判断用户名是否存在。
	    //如果用户名不存在，则直接生成新的本地用户，并绑定uid与openid。写入cookie，使用户为登录状态，到此结束。

	    //如果用户名存在，提醒用户是否验证并与之绑定。如果用户选择验证，并验证通过，则与之绑定。写入cookie，使用户为登录状态，到此结束。

	    //如果用户放弃验证，或者验证失败，则生成新的本地用户，并生成新的用户名，绑定uid与openid。写入cookie，使用户为登录状态，到此结束。

        val queryUserOpt = table.filter(_.qopenid===user.qopenid).take(1).firstOption

        queryUserOpt match {
            case x if queryUserOpt.isDefined =>
                //Openid 存在。写入cookie，使用户为登录状态
                retOpt = Option(wrap(queryUserOpt.get))

            case _ =>
                //Openid不存在
                val unameIsExistsUserOpt = table.filter(_.name===user.name).take(1).firstOption
                unameIsExistsUserOpt match{
                    case x if unameIsExistsUserOpt.isDefined =>
                        //用户名存在。提醒用户是否验证并与之绑定。如果用户选择验证，并验证通过，则与之绑定。写入cookie，使用户为登录状态，到此结束。
                        //TODO...................... 以后再说
                        //retOpt = Option(wrap(unameIsExistsUserOpt.get))

                    case _ =>
                        //用户名不存在，生成新的本地用户，并绑定uid与openid。写入cookie，使用户为登录状态。
                        user.password = Option((System.currentTimeMillis()/1000).toString)
                        pack(user)
                        val uid = (table returning table.map(_.uid)) += user
                        user.uid = uid
                        retOpt = Option(wrap(user))

                }

        }

        Cache.remove(activeUserListKey)
        retOpt
    }

	def addSConnUser(user:User)(implicit session: Session):Option[UserWrapper] = {
		var retOpt = None:Option[UserWrapper]

        val queryUserOpt = table.filter(_.sopenid===user.sopenid).take(1).firstOption

        queryUserOpt match {
            case x if queryUserOpt.isDefined =>
                //Openid 存在。写入cookie，使用户为登录状态
                retOpt = Option(wrap(queryUserOpt.get))

            case _ =>
                //Openid不存在
                val unameIsExistsUserOpt = table.filter(_.name===user.name).take(1).firstOption
                unameIsExistsUserOpt match{
                    case x if unameIsExistsUserOpt.isDefined =>
                        //用户名存在。提醒用户是否验证并与之绑定。如果用户选择验证，并验证通过，则与之绑定。写入cookie，使用户为登录状态，到此结束。
                        //TODO...................... 以后再说
                        //retOpt = Option(wrap(unameIsExistsUserOpt.get))

                    case _ =>
                        //用户名不存在，生成新的本地用户，并绑定uid与openid。写入cookie，使用户为登录状态。
                        user.password = Option((System.currentTimeMillis()/1000).toString)
                        pack(user)

                        val uid = (table returning table.map(_.uid)) += user
                        user.uid = uid
                        retOpt = Option(wrap(user))

                }

        }
		retOpt
	}
}

import models.UsersActor.{AddQConnUser, AddSConnUser, Find, FindByEmail, FindByName, FindByPhone, Init, QueryActiveUser, UpdatePassWord, UserIsExists}

object UsersActor {

    case class Init(db: Database, user: User)

    case class Find(db: Database, uid: Long)

    case class FindByName(db: Database, name: String)

	case class FindByEmail(db: Database, email: String)

	case class FindByPhone(db: Database, phone: String)

    case class UpdatePassWord(db: Database, uid: Long, passwd: String)

    case class UserIsExists(db:Database,uname:String)

    case class QueryActiveUser(db:Database,page:Int,size:Int)

	case class AddQConnUser(db:Database,user: User)

	case class AddSConnUser(db:Database,user: User)

}

class UsersActor extends Actor {
    def receive: Receive = {
        case Init(db: Database, user) =>
            db.withSession (implicit session => sender ! Users.init(user))

        case Find(db: Database, uid: Long) =>
            db.withSession (implicit session => sender ! Users.find(uid))

        case FindByName(db: Database, name: String) =>
            db.withSession (implicit session => sender ! Users.findByName(name))

        case FindByEmail(db: Database, email: String) =>
	        db.withSession (implicit session => sender ! Users.findByEmail(email))

        case FindByPhone(db: Database, phone: String) =>
	        db.withSession (implicit session => sender ! Users.findByPhone(phone))

        case UpdatePassWord(db: Database, uid: Long, passwd: String) =>
            db.withSession (implicit session => sender ! Users.updatePassWord(uid, passwd))

        case UserIsExists(db:Database,uname:String) =>
            db.withSession(implicit session => sender ! Users.userIsExists(uname))

        case QueryActiveUser(db:Database,page:Int,size:Int) =>
            db.withSession( implicit session => sender ! Users.queryActiveUser(page,size))

        case AddQConnUser(db:Database,user:User) =>
	        db.withSession(implicit session => sender ! Users.addQConnUser(user))

        case AddSConnUser(db:Database,user:User) =>
            db.withSession(implicit session => sender ! Users.addSConnUser(user))

    }
}