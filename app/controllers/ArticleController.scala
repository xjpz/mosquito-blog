package controllers

import javax.inject.{Inject, Singleton}

import models.{Article, ArticleListWrapper, Articles, JsFormat}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{Action, Controller}
import utils.ResultStatus

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by xjpz on 2016/5/28.
  */

@Singleton
class ArticleController @Inject()(articles: Articles) extends Controller with JsFormat {

  val articleForm = Form(
    mapping(
      "title" -> text(),
      "content" -> text(),
      "catalog" -> optional(text()),
      "type" -> number(),
      "aid" -> optional(longNumber())
    )(Tuple5.apply)(Tuple5.unapply)
  )

  def retrieve(aid: Long) = Action.async {
    for {
      Some(article) <- articles.retrieve(aid)
    } yield Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(article), "des" -> ResultStatus.status_1))
  }

  def query(page: Int, size: Int) = Action.async {
    articles.query.map { x =>
      val articleListWrapper = ArticleListWrapper(
        x.toList.slice(size * page, size * page + size),
        x.length
      )
      Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(articleListWrapper), "des" -> ResultStatus.status_1))
    }.recover {
      case e: Exception => Ok(Json.obj("ret" -> 2, "con" -> JsNull, "des" -> ResultStatus.status_2))
    }
  }

  def queryByCatalog(catalog: String, page: Int, size: Int) = Action.async {
    articles.queryByCatalog(catalog).map { x =>
      val articleListWrapper = ArticleListWrapper(
        x.toList.slice(size * page, size * page + size),
        x.length
      )
      Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(articleListWrapper), "des" -> ResultStatus.status_1))
    }.recover {
      case e: Exception => Ok(Json.obj("ret" -> 2, "con" -> JsNull, "des" -> ResultStatus.status_2))
    }
  }

  def init = Action.async { implicit request =>
    val articleFormTuple = articleForm.bindFromRequest().get
    val uid = request.session.get("uid").getOrElse("0").toLong
    if (uid != 0) {
      val article = Article(
        Option(articleFormTuple._1),
        Option(articleFormTuple._2),
        articleFormTuple._3,
        Option(uid),
        Option(0),
        Option(articleFormTuple._4)
      )
      for {
        aid <- articles.init(article)
      } yield {
        article.aid = aid
        Redirect("/")
      }
    } else {
      Future(Ok(Json.obj("ret" -> 3, "con" -> JsNull, "des" -> ResultStatus.status_3)))
    }
  }

  def update = Action.async { implicit request =>
    val articleFormTuple = articleForm.bindFromRequest().get
    val uid = request.session.get("uid").getOrElse("0").toLong
    val aid = articleFormTuple._5

    if (uid != 0) {
      val updArticle = Article(Option(articleFormTuple._1), Option(articleFormTuple._2), articleFormTuple._3, None,
        None, Option(articleFormTuple._4), None, None, None, None, None, None, None, aid)

      {
        for {
          Some(queryArticle) <- articles.retrieve(aid.get)
        } yield queryArticle
      }.flatMap { x =>
        if (x.aid == updArticle.aid) {
          for {
            updRow <- articles.update(updArticle)
          } yield Redirect("/")
        } else {
          Future(Ok(Json.obj("ret" -> 3, "con" -> JsNull, "des" -> ResultStatus.status_3)))
        }
      }
    } else {
      Future(Ok(Json.obj("ret" -> 3, "con" -> JsNull, "des" -> ResultStatus.status_3)))
    }
  }

  def updateSmileCount(aid: Long) = Action.async {
    {
      for (article <- articles.retrieve(aid)) yield article
    }.flatMap {
      case Some(x) =>
        for (
          updSimle <- articles.updateSmileCount(aid, x.smile.getOrElse(0) + 1)
        ) yield Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(updSimle), "des" -> ResultStatus.status_1))
      case _ => Future(Ok(Json.obj("ret" -> 0, "con" -> JsNull, "des" -> ResultStatus.status_0)))
    }
  }

  def revoke(aid: Long) = Action.async { implicit request =>
    val uid = request.session.get("uid").getOrElse("0").toLong

    {
      for (article <- articles.retrieve(aid)) yield article
    }.flatMap {
      case Some(x) =>
        if (Option(uid) == x.uid) {
          for (
            del <- articles.revoke(aid)
          ) yield Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(del), "des" -> ResultStatus.status_1))
        } else {
          Future(Ok(Json.obj("ret" -> 3, "con" -> JsNull, "des" -> ResultStatus.status_3)))
        }
      case _ => Future(Ok(Json.obj("ret" -> 0, "con" -> JsNull, "des" -> ResultStatus.status_0)))
    }
  }

}
