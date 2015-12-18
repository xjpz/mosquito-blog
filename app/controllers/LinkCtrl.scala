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
 * Created by xjpz_wz on 2015/9/23.
 */

object LinkCtrl extends Controller with LinkJSONTrait {

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val linkActor = system.actorOf(Props(new LinksActor()))

    def init = Action{ request =>
        val reqJson = request.body.asJson.get
        var ret = None:Option[LinkWrapper]

        val updtime = Option(System.currentTimeMillis() / 1000L)
        val name = (reqJson \"name").asOpt[String]
        val author = (reqJson \"author").asOpt[String]
        val content = (reqJson \"content").asOpt[String]

        val link = Link(
            name,
            author,
            content,
            updtime,
            updtime,
            Option(0)
        )

        val future = linkActor ? LinksActor.Init(Global.db, link)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[LinkWrapper]]

        if(ret.isDefined){
            Ok(Json.toJson(ret))
        }else{
            InternalServerError(JsNull)
        }

    }

    def query(page:Option[Int],size:Option[Int]) = Action {
        var ret = None:Option[LinkListWrapper]
        val future = linkActor ? LinksActor.Query(Global.db,page.getOrElse(0),size.getOrElse(10))
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[LinkListWrapper]]

        if(ret.isDefined){
            Ok(Json.toJson(ret))
        }else{
            InternalServerError(JsNull)
        }
    }

    def remove(lid:Long) = Action {
        var ret = None:Option[LinkWrapper]

        val future = linkActor ? LinksActor.Remove(Global.db, lid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[LinkWrapper]]

        if(ret.isDefined){
            Ok(Json.toJson(ret))
        }else{
            InternalServerError(JsNull)
        }
    }

    def update = Action{ request =>
        val reqJson = request.body.asJson.get
        var ret = None:Option[LinkWrapper]

        val name = (reqJson \"name").asOpt[String]
        val author = (reqJson \"author").asOpt[String]
        val content = (reqJson \"content").asOpt[String]

        val link = Link(
            name,
            author,
            content
        )

        val future = linkActor ? LinksActor.Update(Global.db, link)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[LinkWrapper]]

        if(ret.isDefined){
            Ok(Json.toJson(ret))
        }else{
            InternalServerError(JsNull)
        }
    }

}
