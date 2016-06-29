package controllers

import javax.inject.Inject

import models.{JsFormat, LinkListWrapper, Links}
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{Action, Controller}
import utils.ResultStatus

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xjpz on 2016/5/29.
  */

class LinkController @Inject()(links: Links) extends Controller with JsFormat{

  def retrieve(lid: Long) = Action.async {
    links.retrieve(lid).map {
      case Some(x) => Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(x), "des" -> ResultStatus.status_1))
      case _ => Ok(Json.obj("ret" -> 0, "con" -> JsNull, "des" -> ResultStatus.status_0))
    }.recover {
      case e: Exception => Ok(Json.obj("ret" -> 2, "con" -> JsNull, "des" -> ResultStatus.status_2))
    }
  }

  def query(page:Int,size:Int) = Action.async{
    {
      for{linkSeq <- links.query} yield linkSeq
    }.map{ x =>
      val articleListWrapper = LinkListWrapper(
        x.toList.slice(size * page, size * page + size),
        x.length
      )
      Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(articleListWrapper), "des" -> ResultStatus.status_1))
    }.recover{
      case e: Exception => Ok(Json.obj("ret" -> 2, "con" -> JsNull, "des" -> ResultStatus.status_2))
    }
  }
  
}
