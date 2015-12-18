package controllers

import models.other.CaptchaInfo
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}
import utils.MD5

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author xjpz
 * @since 2015-09-05 16:28 PM
 * @version 1.0
 */
object Captcha extends Controller {

	def getCaptcha = Action {
		implicit request => {
			val captcha = CaptchaInfo.create()
			Ok.chunked(Enumerator.fromStream(captcha.value)).as("image/png")
				.withSession(request.session + ("captcha" -> MD5.hash(captcha.text.toUpperCase)))
		}
	}

	def getCaptchaNew = Action {
		implicit request => {
			val captcha = CaptchaInfo.create()
			Ok.chunked(Enumerator.fromStream(captcha.value)).as("image/png")
				.withSession(request.session + ("captcha" -> captcha.text))
		}
	}

    def getCaptchaText = Action { request =>
        val captchaText = request.session.get("captcha").get
        Ok(captchaText)
    }

}
