package controllers

import javax.inject.Inject

import models.{JsFormat, Moods}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.ResultStatus
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xjpz on 2016/6/8.
  */

class MoodController @Inject()(moods: Moods) extends Controller with JsFormat {

  def retrieve(id: Long) = Action.async {
    for {
          Some(mood) <- moods.retrieve(id)
    } yield Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(mood), "des" -> ResultStatus.status_1))
  }

}
