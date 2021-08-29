package controllers

import helpers.WordFilter

import javax.inject.{Inject, Singleton}
import models.{Article, ArticleListWrapper, Articles}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.{RequestHelper, ResultCode, ResultStatus, ResultUtil}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by xjpz on 2016/5/28.
  */

@Singleton
class ArticleController @Inject()(articles: Articles)(cc: ControllerComponents) extends AbstractController(cc) {

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
    articles.retrieve(aid).map {
      case Some(article) => Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(article), "des" -> ResultStatus.status_1))
    }
  }

  def query(page: Int, size: Int) = Action.async {
    articles.query.map { x =>
      val articleListWrapper = ArticleListWrapper(
        x.toList.slice(size * page, size * page + size),
        x.length
      )
      Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(articleListWrapper), "des" -> ResultStatus.status_1))
    }
  }

  def queryByCatalog(catalog: String, page: Int, size: Int) = Action.async {
    articles.queryByCatalog(catalog).map { x =>
      val articleListWrapper = ArticleListWrapper(
        x.toList.slice(size * page, size * page + size),
        x.length
      )
      Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(articleListWrapper), "des" -> ResultStatus.status_1))
    }
  }

  def create = Action.async { implicit request =>
    val articleFormTuple = articleForm.bindFromRequest().get
    val uid = RequestHelper.getUid
    val content = articleFormTuple._2
    if(WordFilter.isContains(content)){
      throw new RuntimeException("发布的内容不合法")
    }

    if (uid != 0) {
      val article = Article(
        Option(articleFormTuple._1),
        Option(content),
        articleFormTuple._3,
        Option(uid),
        Option(0),
        Option(articleFormTuple._4)
      )
      articles.init(article).map { aid =>
        article.aid = aid
        ResultUtil.success
      }
    } else {
      Future(ResultUtil.failure(ResultCode.NOT_PERMISSION))
    }
  }

  def update = Action.async { implicit request =>
    val articleFormTuple = articleForm.bindFromRequest().get
    val uid = RequestHelper.getUid
    val aid = articleFormTuple._5

    if (uid != 0) {
      val updArticle = Article(Option(articleFormTuple._1), Option(articleFormTuple._2), articleFormTuple._3, None,
        None, Option(articleFormTuple._4), None, None, None, None, None, None, None, aid)

      articles.retrieve(aid.get).flatMap { case Some(x) =>
        if (x.aid == updArticle.aid) {
          articles.update(updArticle).map(_ => Redirect("/"))
        } else {
          Future(Ok(Json.obj("ret" -> 3, "con" -> JsNull, "des" -> ResultStatus.status_3)))
        }
      }
    } else {
      Future(Ok(Json.obj("ret" -> 3, "con" -> JsNull, "des" -> ResultStatus.status_3)))
    }
  }

  def updateSmileCount(aid: Long) = Action.async {
    articles.retrieve(aid).flatMap {
      case Some(x) =>
        articles.updateSmileCount(aid, x.smile.getOrElse(0) + 1).map(p =>
          Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(p), "des" -> ResultStatus.status_1))
        )
      case _ => Future(Ok(Json.obj("ret" -> 0, "con" -> JsNull, "des" -> ResultStatus.status_0)))
    }
  }

  def revoke(aid: Long) = Action.async { implicit request =>
    val uid = RequestHelper.getUid

    articles.retrieve(aid).flatMap {
      case Some(x) =>
        if (Option(uid) == x.uid) {
          articles.revoke(aid).map(p => Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(p), "des" -> ResultStatus.status_1)))
        } else {
          Future(Ok(Json.obj("ret" -> 3, "con" -> JsNull, "des" -> ResultStatus.status_3)))
        }
      case _ => Future(Ok(Json.obj("ret" -> 0, "con" -> JsNull, "des" -> ResultStatus.status_0)))
    }
  }

  def checkContent = Action.async(parse.json) { implicit request =>
    val reqJson = request.body
    val content = (reqJson \ "content").asOpt[String].getOrElse("")
    if (WordFilter.isContains(content)) {
      Future(ResultUtil.failure("发布内容不合法"))
    } else {
      Future(ResultUtil.success())
    }
  }

}
