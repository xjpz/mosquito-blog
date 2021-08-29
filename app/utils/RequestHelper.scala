package utils

import play.api.mvc.RequestHeader

/**
  * Created by xjpz on 2021/08/10.
  */
object RequestHelper {

  def isLogin(implicit request: RequestHeader): Boolean = {
    request.session.get("uid").nonEmpty
  }

  def getUidOpt(implicit request: RequestHeader): Option[Long] = {
    request.session.get("uid").map(_.toLong)
  }

  def getUid(implicit request: RequestHeader): Long = {
    request.session.get("uid").getOrElse("0").toLong
  }

  def getNameOpt(implicit request: RequestHeader): Option[String] = {
    request.session.get("name")
  }

  def getName(implicit request: RequestHeader): String = {
    request.session.get("name").getOrElse("游客")
  }

  def getAdminUid(implicit request: RequestHeader):Long = {
    request.session.get("admin").getOrElse("0").toLong
  }

  def getAdminUidOpt(implicit request: RequestHeader):Option[Long] = {
    request.session.get("admin").map(_.toLong)
  }

  def getAdminName(implicit request: RequestHeader):String = {
    request.session.get("adminName").getOrElse("游客")
  }


}
