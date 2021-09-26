package controllers

import javax.inject.Inject
import models.{LinkListWrapper, Links}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.ResultUtil.QUERY_OK
import utils.{ResultCode, ResultUtil}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xjpz on 2016/5/29.
  */

class LinkController @Inject()(links: Links)(cc: ControllerComponents) extends AbstractController(cc) {

  def retrieve(lid: Long) = Action.async {
    links.retrieve(lid).map {
      case Some(x) => ResultUtil.success(QUERY_OK,Json.toJson(x))
      case _ => ResultUtil.failure(ResultCode.DATA_NOT_EXIST)
    }
  }

  def query(page: Int, size: Int) = Action.async {
    links.query.map { x =>
      val articleListWrapper = LinkListWrapper(
        x.toList.slice(size * page, size * page + size),
        x.length
      )
      ResultUtil.success(QUERY_OK,Json.toJson(articleListWrapper))
    }
  }

}
