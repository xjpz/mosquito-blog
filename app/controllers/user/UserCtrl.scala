package controllers

import globals.Global
import models._
import play.api.libs.json._
import play.api.mvc._
import scala.language.postfixOps
// Akka imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

object UserCtrl extends Controller with UserJSONTrait {
	implicit val timeout = Timeout(10 seconds)
	implicit lazy val system = ActorSystem()
	implicit lazy val uActor = system.actorOf(Props(new UsersActor()))

	def init = Action { request =>
		val reqJson = request.body.asJson.get
		var ret = None: Option[UserWrapper]

		val name = (reqJson \ "name").asOpt[String]
		val pwd = (reqJson \ "password").asOpt[String]
		val email = (reqJson \ "email").asOpt[String]
		val phone = (reqJson \ "phone").asOpt[String]
		val updtime = Option(System.currentTimeMillis() / 1000L)

		val user = User(
			name,
			pwd,
			email,
			phone,
			None,
			Option(1),
			Option(0),
			None,
			None,
			None,
			None,
			updtime,
			updtime
		)

		val future = uActor ? UsersActor.Init(Global.db, user)
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Option[UserWrapper]]

		if (ret.isDefined) {
			Ok(Json.toJson(ret.get))
		} else {
			InternalServerError(JsNull)
		}
	}

	def retrieve(uid: Long) = Action {
		var ret = None: Option[UserWrapper]

		val future = uActor ? UsersActor.Find(Global.db, uid)
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Option[UserWrapper]]

		if (ret.isDefined) {
			Ok(Json.toJson(ret.get))
		} else {
			InternalServerError(JsNull)
		}
	}

	def findByName(name: String) = Action {
		var ret = None: Option[UserWrapper]

		val future = uActor ? UsersActor.FindByName(Global.db, name)
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Option[UserWrapper]]

		if (ret.isDefined) {
			Ok(Json.toJson(ret.get))
		} else {
			InternalServerError(JsNull)
		}
	}

	def findByEmail(email:String) = Action {
		var ret = None: Option[UserWrapper]

		val future = uActor ? UsersActor.FindByEmail(Global.db, email)
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Option[UserWrapper]]

		if (ret.isDefined) {
			Ok(Json.toJson(ret.get))
		} else {
			InternalServerError(JsNull)
		}
	}

	def findByPhone(phone:String) = Action {
		var ret = None: Option[UserWrapper]

		val future = uActor ? UsersActor.FindByPhone(Global.db, phone)
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Option[UserWrapper]]

		if (ret.isDefined) {
			Ok(Json.toJson(ret.get))
		} else {
			InternalServerError(JsNull)
		}
	}

	def query(action:String) = Action {
		var ret = None: Option[UserWrapper]

		//        val phonePattern = """\d{3}\d{8}""".r
		val emailPattern = """[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?""".r

		emailPattern.findFirstIn(action) match {
			case Some(action) => {
				//Email
				val future = uActor ? UsersActor.FindByEmail(Global.db, action)
				ret = Await.result(future, timeout.duration)
					.asInstanceOf[Option[UserWrapper]]
			}
			case _ => {
				//phone or uname
				if(action.length==11 && action.startsWith("1")){
					val future = uActor ? UsersActor.FindByPhone(Global.db, action)
					ret = Await.result(future, timeout.duration)
						.asInstanceOf[Option[UserWrapper]]
				}else{
					val future = uActor ? UsersActor.FindByName(Global.db, action)
					ret = Await.result(future, timeout.duration)
						.asInstanceOf[Option[UserWrapper]]
				}
			}
		}

		if (ret.isDefined) {
			Ok(Json.toJson(ret.get))
		} else {
			InternalServerError(JsNull)
		}
	}

	def updatePassWord() = Action { request =>
		val reqJson = request.body.asJson.get
		var ret = false

		val uid = (reqJson \ "uid").asOpt[Long]
		val passwd = (reqJson \ "password").asOpt[String]

		if (uid.isDefined && passwd.isDefined) {
			val future = uActor ? UsersActor.UpdatePassWord(Global.db, uid.get, passwd.get)
			ret = Await.result(future, timeout.duration)
				.asInstanceOf[Boolean]
		}

		if (ret) {
			Ok("")
		} else {
			InternalServerError(JsNull)
		}
	}

	def userIsExists(uname:String) = Action {
		var ret = false

		val future = uActor ? UsersActor.UserIsExists(Global.db,uname)
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Boolean]

		if (ret) {
			Ok(Json.obj("code" -> 200,"exists" -> ret,"uname" -> uname))
		} else {
			NotFound(JsNull)
		}
	}

	def queryActiveUser(page:Option[Int],size:Option[Int]) = Action {
		var ret = None:Option[UserListWrapper]

		val future = uActor ? UsersActor.QueryActiveUser(Global.db,page.getOrElse(0),size.getOrElse(10))
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Option[UserListWrapper]]

		if (ret.isDefined) {
			Ok(Json.toJson(ret.get))
		} else {
			InternalServerError(JsNull)
		}
	}

	def addConnUser = Action {request =>
		val reqJson = request.body.asJson.get
        var retOpt = None:Option[UserWrapper]

		val otype = (reqJson \"otype").asOpt[Int]
		val openid = (reqJson \"openid").asOpt[String]
		val token = (reqJson \"token").asOpt[String]
		val name = (reqJson \"name").asOpt[String]
		val updtime = Option(System.currentTimeMillis() / 1000L)

		val user = User(
			name,
			None,
			None,
			None,
			None,
			None,
			None,
			None,
			None,
			None,
			None,
			updtime,
			updtime
		)

		if(otype.isDefined){
			otype match{
				case Some(1) => {
                    //初始化user的qopenid
                    user.qopenid = openid
                    user.qtoken = token

					val future = uActor ? UsersActor.AddQConnUser(Global.db,user)
					 retOpt = Await.result(future, timeout.duration)
						.asInstanceOf[Option[UserWrapper]]
				}
				case Some(2) => {
                    //初始化user的qopenid
                    user.sopenid = openid
                    user.stoken = token
					val future = uActor ? UsersActor.AddSConnUser(Global.db,user)
                    retOpt = Await.result(future, timeout.duration)
						.asInstanceOf[Option[UserWrapper]]
				}
				case _ =>
			}
		}

        if(retOpt.isDefined){
            Ok(Json.toJson(retOpt.get))
        }else{
            InternalServerError(JsNull)
        }
	}

}
