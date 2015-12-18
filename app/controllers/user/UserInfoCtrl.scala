package controllers

import globals.Global
import models.{UserInfo, UserInfoActor, UserInfoJSONTrait, UserInfoWrapper}
import play.api.libs.json._
import play.api.mvc._

import scala.language.postfixOps
// Akka imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Administrator on 2015/9/26.
 */
object UserInfoCtrl extends Controller with UserInfoJSONTrait{
	implicit val timeout = Timeout(10 seconds)
	implicit lazy val system = ActorSystem()
	implicit lazy val uinfoActor = system.actorOf(Props [UserInfoActor])

	def init = Action { request =>
		val reqJson = request.body.asJson.get
		var ret = None:Option[UserInfoWrapper]
		val updtime = Option(System.currentTimeMillis() / 1000L)

		val uid = (reqJson \"uid").asOpt[Long]
		val rname = (reqJson \"rname").asOpt[String]
		val descrp = (reqJson \"descrp").asOpt[String]
		val gender = (reqJson \"gender").asOpt[Int]
		val birthday = (reqJson \"birthday").asOpt[String]
		val area = (reqJson \"area").asOpt[String]
		val reg_ip = (reqJson \"reg_ip").asOpt[String]

		val userInfo = UserInfo(
			uid,
			rname,
			descrp,
			gender,
			birthday,
			area,
			reg_ip
		)

		val future = uinfoActor ? UserInfoActor.Init(Global.db, userInfo)
		ret = Await.result(future, timeout.duration)
			.asInstanceOf[Option[UserInfoWrapper]]

		if(ret.isDefined){
			Ok(Json.toJson(ret))
		}else{
			InternalServerError(JsNull)
		}
	}
}
