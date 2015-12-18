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

/**
 * Created by xjpz_wz on 2015/10/5.
 */
object UcustomCtrl extends Controller with CustomJSONTrait{

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val ucustomActor = system.actorOf(Props [CustomsActor])

    def init = Action { request =>
        val reqJson = request.body.asJson.get
        var ret = None: Option[CustomWrapper]
        val updtime = Option(System.currentTimeMillis() / 1000L)

        val uid = (reqJson \ "uid").asOpt[Long]
        val descrp = (reqJson \ "descrp").asOpt[String]
        val top =  (reqJson \ "top").asOpt[String]
        val right =  (reqJson \ "right").asOpt[String]
        val left =  (reqJson \ "left").asOpt[String]
        val bottom =  (reqJson \ "bottom").asOpt[String]
        val style =  (reqJson \ "style").asOpt[String]
        val javascript =  (reqJson \ "javascript").asOpt[String]

        val custom = Custom(
            uid,
            descrp,
            top,
            right,
            left,
            bottom,
            style,
            javascript,
            updtime,
            updtime,
            Option(0)
        )

        val future = ucustomActor ? CustomsActor.Init(Global.db, custom)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[CustomWrapper]]

        if(ret.isDefined){
            Ok(Json.toJson(ret))
        }else{
            InternalServerError(JsNull)
        }
    }

    def retrieve(uid:Long) = Action{
        var ret = None: Option[CustomWrapper]

        val future = ucustomActor ? CustomsActor.Retrieve(Global.db, uid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[CustomWrapper]]

        if(ret.isDefined){
            Ok(Json.toJson(ret))
        }else{
            InternalServerError(JsNull)
        }
    }

}
