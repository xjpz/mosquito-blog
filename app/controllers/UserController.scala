package controllers

import java.security.MessageDigest
import javax.inject.{Inject, Singleton}

import models.{User, Users}
import org.apache.commons.codec.binary.Base64
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.Codecs
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.ResultStatus

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random


/**
  * Created by xjpz on 2016/5/26.
  */

@Singleton
class UserController @Inject()(users: Users)(cc: ControllerComponents) extends AbstractController(cc) {

  implicit lazy val shaEncoder = MessageDigest.getInstance("SHA-1")

  val loginForm = Form(
    mapping(
      "name" -> text(),
      "password" -> text()
    )(Tuple2.apply)(Tuple2.unapply)
  )

  val regForm = Form(
    mapping(
      "name" -> text(),
      "password" -> text(),
      "email" -> text(),
      "captcha" -> text()

    )(Tuple4.apply)(Tuple4.unapply)
  )

  val loginByQConnForm = Form(
    mapping(
      "name" -> text(),
      "openid" -> text(),
      "token" -> text(),
      "otype" -> number()
    )(Tuple4.apply)(Tuple4.unapply)
  )


  def login = Action.async { implicit request =>
    val loginFormTuple = loginForm.bindFromRequest().get

    val emailPattern = """[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?""".r

    val passwordBase64ForSHA = Base64.encodeBase64String(shaEncoder.digest(loginFormTuple._2.getBytes()))

    val userOpt = emailPattern.findFirstIn(loginFormTuple._1) match {
      case Some(_) => for {user <- users.queryByEmail(loginFormTuple._1)} yield user
      case _ => for {user <- users.queryByName(loginFormTuple._1)} yield user
    }

    userOpt.map {
      case Some(u) =>
        u.password == Option(passwordBase64ForSHA) match {
          case true =>
            Ok(Json.obj("ret" -> 1, "con" -> JsNull, "des" -> ResultStatus.status_1))
              .withSession("uid" -> u.uid.get.toString, "name" -> u.name.get)
          case _ => Ok(Json.obj("ret" -> 8, "con" -> JsNull, "des" -> ResultStatus.status_8))
        }
      case _ => Ok(Json.obj("ret" -> 9, "con" -> JsNull, "des" -> ResultStatus.status_9))
    }
  }

  def reg = Action.async { implicit request =>
    val regFormTuple = regForm.bindFromRequest().get
    val user = User(Option(regFormTuple._1), Option(regFormTuple._2), Option(regFormTuple._3))
    val captchaText = request.session.get("captcha")

    if (captchaText == Option(Codecs.sha1(regFormTuple._4.toUpperCase))) {
      {
        for {
          userByName <- users.queryByName(regFormTuple._1)
          userByEmail <- users.queryByEmail(regFormTuple._3)
        } yield (userByName, userByEmail)
      }.flatMap {
        case (Some(_), _) => Future(Ok(Json.obj("ret" -> 11, "con" -> JsNull, "des" -> ResultStatus.status_11)))
        case (None, Some(_)) => Future(Ok(Json.obj("ret" -> 12, "con" -> JsNull, "des" -> ResultStatus.status_12)))
        case (_, _) =>
          for {
            uid <- users.init(user)
          } yield {
            user.uid = uid
            Ok(Json.obj("ret" -> 1, "con" -> Json.toJson(user), "des" -> ResultStatus.status_1))
              .withSession("uid" -> user.uid.get.toString, "name" -> user.name.get)
          }
      }
    } else {
      Future(Ok(Json.obj("ret" -> 6, "con" -> JsNull, "des" -> ResultStatus.status_6)))
    }
  }

  def loginByQConn = Action.async { implicit request =>
    val loginQConnForm = loginByQConnForm.bindFromRequest().get
    val openid = loginQConnForm._2

    {
      for {
        userByOpenid <- users.queryByOpenid(openid)
        userByName <- users.queryByName(loginQConnForm._1)
      } yield (userByOpenid, userByName)
    }.flatMap { p =>
      if (p._1.isDefined) {
        Future(Ok("/").withSession("uid" -> p._1.get.uid.get.toString, "name" -> p._1.get.name.get))
      } else {
        val name = if (p._2.isDefined) loginQConnForm._1 + Random.shuffle(0 to 9).mkString("").take(4) else loginQConnForm._1
        val password = Base64.encodeBase64String(shaEncoder.digest(Random.shuffle(0 to 8).toString.getBytes()))
        val user = User(Option(name), Option(password), None, None, None, None, Some(0), Option(openid), Option(loginQConnForm._3))
        for {
          uid <- users.init(user)
        } yield {
          Ok("/").withSession("uid" -> uid.get.toString, "name" -> name)
        }
      }
    }
  }

}
