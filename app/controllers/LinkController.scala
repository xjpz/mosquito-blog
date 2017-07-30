package controllers

import javax.inject.Inject

import models.{LinkListWrapper, Links}
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.ResultStatus

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xjpz on 2016/5/29.
  */

class LinkController @Inject()(links: Links)(cc: ControllerComponents) extends AbstractController(cc) {

  def retrieve(lid: Long) = Action.async {
    links.retrieve(lid).map {
      case Some(x) => Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(x), "des" -> ResultStatus.status_1))
      case _ => Ok(Json.obj("ret" -> 0, "con" -> JsNull, "des" -> ResultStatus.status_0))
    }
  }

  def query(page: Int, size: Int) = Action.async {
    links.query.map { x =>
      val articleListWrapper = LinkListWrapper(
        x.toList.slice(size * page, size * page + size),
        x.length
      )
      Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(articleListWrapper), "des" -> ResultStatus.status_1))
    }
  }

}
