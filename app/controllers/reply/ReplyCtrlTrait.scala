package controllers

import globals.Global
import models._
import play.api.libs.json._
import play.api.mvc._

// Play cached support

import scala.language.postfixOps

// Akka imports

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by xjpz_wz on 2015/7/21.
 */
trait ReplyCtrlTrait extends Controller with ReplyJSONTrait {
    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val rActor = system.actorOf(Props(new Replys2ArticleActor()))

    def init = Action { request =>
        val reqJson = request.body.asJson.get

        var ret = None: Option[ReplyWrapper]

        val aid = (reqJson \ "aid").asOpt[Long]
        val uid = (reqJson \ "uid").asOpt[Long]
        val name = (reqJson \ "name").asOpt[String]
        val url = (reqJson \ "url").asOpt[String]
        val email = (reqJson \ "email").asOpt[String]
        val content = (reqJson \ "content").asOpt[String]

        val quote = (reqJson \ "quote").asOpt[Long]

        val reply = Reply(
            aid,
            uid,
            name,
            url,
            email,
            content,
            quote

        )

        val future = rActor ? ReplysActor.Init(Global.db, reply)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def query(aid: Long) = Action {
        var ret = None: Option[ReplyListWrapper]
        val future = rActor ? ReplysActor.Query(Global.db, aid: Long)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyListWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def queryChild(rid: Long) = Action {
        var ret = None: Option[ReplyListWrapper]
        val future = rActor ? ReplysActor.QueryChild(Global.db, rid: Long)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyListWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def retrieve(rid:Long) = Action {
        var ret = None: Option[ReplyWrapper]

        val future = rActor ? ReplysActor.Retrieve(Global.db, rid: Long)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def smile(rid:Long) = Action {
        var ret = None: Option[ReplyWrapper]

        val future = rActor ? ReplysActor.Smile(Global.db, rid: Long)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

}
