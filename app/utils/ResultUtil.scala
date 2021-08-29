package utils

import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.mvc.{Result, Results}

/**
  * Created by xjpz on 2016/7/31.
  */

object ResultUtil {

  val QUERY_OK = "查询成功"
  val ADD_OK = "添加成功"
  val UPDATE_OK = "更新成功"
  val DELETE_OK = "删除成功"
  val OPERATE_OK = "操作成功"
  val OPERATE_FAIL = "操作失败！"

  def success(msg: String, data: JsValue): Result = {
    val ret = Json.obj(
      "success" -> true,
      "code" -> 200,
      "msg" -> msg,
      "data" -> data
    )
    Results.Ok(ret)
  }

  def success(msg: String = OPERATE_OK, data: Option[JsValue] = None): Result = {
    success(msg, data.getOrElse(JsNull))
  }

  def success(msg: String, key: String, data: JsValue): Result = success(msg, Json.obj(key -> data))

  def success(msg: String): Result = success(msg, JsNull)

  //  def success(key:String,data:JsValue) :JsValue = success("操作成功！",key,data)

  def success: Result = success(OPERATE_OK)

  def success(resultCode: ResultCode): Result = {
    val ret = Json.obj(
      "success" -> true,
      "code" -> resultCode.getCode,
      "msg" -> resultCode.getCode
    )
    Results.Ok(ret)
  }
  def failure(code: Int, msg: String): Result = {
    val ret = Json.obj(
      "success" -> false,
      "code" -> code,
      "msg" -> msg
    )
    Results.Ok(ret)
  }

  def failure(msg: String): Result = failure(400, msg)

  def failure: Result = failure(OPERATE_FAIL)

  def failure(resultCode: ResultCode): Result = {
    val ret = Json.obj(
      "success" -> false,
      "code" -> resultCode.getCode,
      "msg" -> resultCode.getCode
    )
    Results.Ok(ret)
  }

  def failureInvalidParameter: Result = failure(404, "参数错误")

}
