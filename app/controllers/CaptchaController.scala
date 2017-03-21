package controllers

import javax.inject.{Inject, Singleton}

import models.CaptchaInfo
import play.api.libs.Codecs
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author xring
  * @since 2015-08-23 6:28 PM
  * @version 1.0
  */

@Singleton
class CaptchaController @Inject() extends Controller {

  def getCaptcha(w: Int, h: Int) = Action { implicit request => {
    val captcha = CaptchaInfo.create(w, h)

    Ok.chunked(Enumerator.fromStream(captcha.value)).as("image/png")
      .withSession(request.session + ("captcha" -> Codecs.sha1(captcha.text)))
  }
  }

}
