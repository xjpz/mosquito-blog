package services

import app.AppGlobal
import models.{User, Users}
import org.apache.commons.codec.binary.Base64
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import java.security.MessageDigest
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class QQService @Inject()(implicit ec: ExecutionContext, config: Configuration, ws: WSClient, users: Users) {

  val logger = Logger("rpc")

  implicit lazy val shaEncoder = MessageDigest.getInstance("SHA-1")

  private val qqAppSecret = config.get[String]("qqConnectAppSecret")

  val headers = Seq("Accept" -> "application/json", "Content-Type" -> "application/json")

  def getAccessToken(code: String): Future[String] = {
    val startTime = System.currentTimeMillis()
    val url = AppGlobal.qcCode2AccessTokenUrl(qqAppSecret, code)
    ws.url(url).withHttpHeaders(headers: _*).get().map { response =>
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime
      logger.info("getAccessToken url:" + url + " ,request.body:" + "" + " ,request.time:" + requestTime + "毫秒 ,response.body:" + response.body)
      (response.json \ "access_token").asOpt[String].getOrElse("")
    }
  }

  def getOpenId(token: String): Future[String] = {
    val startTime = System.currentTimeMillis()
    val url = AppGlobal.qcToken2OpenIdUrl(token)
    ws.url(url).get().map { response =>
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime
      logger.info("getOpenId url:" + url + " ,request.body:" + "" + " ,request.time:" + requestTime + "毫秒 ,response.body:" + response.body)
      response.json
    }.map { respBody =>
      (respBody \ "openid").asOpt[String].getOrElse("")
    }
  }

  /**
    * 获取用户信息。首先判断是否在数据库中已存在，否则调用腾讯接口读取。
    *
    * @param accessToken
    * @param openId
    * @return
    */
  def getUserInfo(accessToken: String, openId: String): Future[Option[User]] = {
    users.queryByOpenid(openId).flatMap {
      case Some(u) => Future.successful(Some(u))
      case None =>
        ws.url("https://graph.qq.com/user/get_user_info")
          .withHttpHeaders(headers: _*)
          .addQueryStringParameters(
            "access_token" -> accessToken,
            "oauth_consumer_key" -> AppGlobal.qqConnectAppId,
            "openid" -> openId,
            "format" -> "json"
          ).get().flatMap { resp =>
          logger.info("QQLogin getUserInfo: " + resp.body)

          val respBody = resp.json
          val ret = respBody("ret").as[Int]
          val msg = respBody("msg").asOpt[String].getOrElse("")

          if (ret < 0) {
            Future.successful(None)
          } else {
            // 取头像策略：QQ100大头像 > QQ空间100大头像 > 40小头像 > 系统默认头像
            //              val headImg =
            //                respBody("figureurl_qq_2").asOpt[String] orElse
            //                  respBody("figureurl_2").asOpt[String] orElse
            //                  respBody("figureurl_qq_1").asOpt[String] getOrElse
            //                "/assets/images/head.png"

            val name = respBody("nickname").as[String]
            //              val province = respBody("province").asOpt[String].getOrElse("")
            //              val city = respBody("city").asOpt[String].getOrElse("")
            //默认是男
            //              var sex = if (respBody("gender").asOpt[String].getOrElse("") == "女") "female" else "male"

            val password = Base64.encodeBase64String(shaEncoder.digest(Random.shuffle((0 to 8).toList).toString.getBytes()))
            val user = User(Option(name), Option(password), None, None, None, None, Some(0), Option(openId), Option(accessToken))

            users.queryByName(name).flatMap { userOpt =>
              if (userOpt.isDefined) {
                val nameFinal = name + Random.shuffle((0 to 9).toList).mkString("").take(4)
                user.name = Some(nameFinal)
              }
              users.init(user).map { uid =>
                user.uid = uid
                Some(user)
              }
            }
          }
        }
    }
  }
}
