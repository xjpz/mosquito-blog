package controllers

import globals.Global
import models.news.NewsActor.{Delete, FindHead, Insert, Query}
import models.news._
import play.api.libs.json._
import play.api.mvc._

// Play cached support

// Akka imports

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

trait NewsCtrlTrait extends Controller with NewsJSONTrait {

    implicit val timeout = Timeout(5 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val newsActor = system.actorOf(Props(new News2MessagesActor))

    def init = Action { request =>
        val reqJson = request.body.asJson.get
        var ret = None: Option[NewsWrapper]

        val name = (reqJson \ "name").asOpt[String]
        val content = (reqJson \ "content").asOpt[String]
        val uidOpt = (reqJson \ "uid").asOpt[Long]
        val uid = if (uidOpt.isDefined) uidOpt.get else 0L

        if (name.isDefined && content.isDefined) {
            val insertNews = News(
                content,
                Option(uid),
                name
            )
            val future = newsActor ? Insert(Global.db, insertNews)
            ret = Await.result(future, timeout.duration)
                .asInstanceOf[Option[NewsWrapper]]
        }

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def query(page: Option[Int], size: Option[Int]) = Action {

        var ret = None: Option[NewsListWrapper]

        val future = newsActor ? Query(Global.db, page.getOrElse(0), size.getOrElse(10))
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[NewsListWrapper]]
        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def findHead = Action {
        var ret = None: Option[NewsWrapper]

        val future = newsActor ? FindHead(Global.db)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[NewsWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def delete(nid:Long) = Action {
        var ret = None: Option[NewsWrapper]

        val future = newsActor ? Delete(Global.db,nid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[NewsWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }
}
