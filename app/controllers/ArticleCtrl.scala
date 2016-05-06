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
 * Created by xjpz_wz on 2015/7/16.
 */
object ArticleCtrl extends Controller with ArticleJSONTrait {

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val aActor = system.actorOf(Props(new ArticlesActor()))

    def init = Action { request =>
        val reqJson = request.body.asJson.get
        var ret = None: Option[ArticleWrapper]
        val title = (reqJson \ "title").asOpt[String]
        val content = (reqJson \ "content").asOpt[String]
        val uid = (reqJson \ "uid").asOpt[Long]
        val catalog = (reqJson \"catalog").asOpt[String]

        val article = Article(
            title,
            content,
            catalog,
            uid
        )

        val future = aActor ? ArticlesActor.Init(Global.db, article)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def retrieve(aid: Long) = Action {
        var ret = None: Option[ArticleWrapper]

        val future = aActor ? ArticlesActor.Retrieve(Global.db, aid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def query(page: Option[Int], size: Option[Int]) = Action {
        var ret = None: Option[ArticleListWrapper]

        val future = aActor ? ArticlesActor.Query(Global.db, page.getOrElse(0), size.getOrElse(10))
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def readCount(aid: Long) = Action {
        var ret = false

        val future = aActor ? ArticlesActor.ReadCount(Global.db, aid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Boolean]
        if (ret) {
            Ok("")
        } else {
            InternalServerError(JsNull)
        }
    }

    def smileCount(aid: Long) = Action {
        var ret = false

        val future = aActor ? ArticlesActor.SmileCount(Global.db, aid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Boolean]
        if (ret) {
            Ok("")
        } else {
            InternalServerError(JsNull)
        }
    }

    def replyCount(aid: Long) = Action {
        var ret = false

        val future = aActor ? ArticlesActor.ReplyCount(Global.db, aid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Boolean]
        if (ret) {
            Ok("")
        } else {
            InternalServerError(JsNull)
        }
    }


    def articleContent(aid: Long) = Action {

        val future = aActor ? ArticlesActor.Retrieve(Global.db, aid)
        val articleOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleWrapper]]
        val ret = articleOpt.get.content.get
        Ok(ret).as("text/html")
    }

    def queryWithUid(uid: Long,page:Option[Int],size:Option[Int]) = Action {
        var ret = None: Option[ArticleListWrapper]

        val future = aActor ? ArticlesActor.QueryWithUid(Global.db, uid,page.getOrElse(0),size.getOrElse(10))
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }


    def queryArticleAction(action:String,page: Option[Int], size: Option[Int]) = Action {
        var ret = None: Option[ArticleListWrapper]

        val future = aActor ? ArticlesActor.QueryAction(Global.db,action, page.getOrElse(0), size.getOrElse(5))
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def queryCatalog = Action {

        val future = aActor ? ArticlesActor.QueryCatalog(Global.db)
        val ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[CatalogList]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def queryWithCatalog(catalog:String,page: Option[Int], size: Option[Int]) = Action {
        var ret = None: Option[ArticleListWrapper]

        val future = aActor ? ArticlesActor.QueryWithCatalog(Global.db,catalog, page.getOrElse(0), size.getOrElse(5))
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def update = Action { request =>
        val reqJson = request.body.asJson.get

        val uid = request.session.get("uid").getOrElse("0").toLong
        var ret = None: Option[ArticleWrapper]

        val aid = (reqJson \ "aid").asOpt[Long]
        val title = (reqJson \ "title").asOpt[String]
        val content = (reqJson \ "content").asOpt[String]
        val catalog = (reqJson \"catalog").asOpt[String]
        val status = (reqJson \"status").asOpt[Int]

        val article = Article(
            title,
            content,
            catalog,
            Option(uid),
            status,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            aid
        )

        val future = aActor ? ArticlesActor.Update(Global.db, article)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleWrapper]]

        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

    def delete(aid:Long) = Action{ request =>
        val uid = request.session.get("uid").getOrElse("0").toLong
        var ret = None: Option[ArticleWrapper]
        if(uid!=0){
            val future = aActor ? ArticlesActor.Delete(Global.db,aid, uid)
            ret = Await.result(future, timeout.duration)
                .asInstanceOf[Option[ArticleWrapper]]
        }
        if (ret.isDefined) {
            Ok(Json.toJson(ret.get))
        } else {
            InternalServerError(JsNull)
        }
    }

}
