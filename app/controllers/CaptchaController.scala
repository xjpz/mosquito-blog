package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import models.CaptchaInfo
import play.api.libs.Codecs
import play.api.mvc.{AbstractController, ControllerComponents}

/**
  * @author xring
  * @since 2015-08-23 6:28 PM
  * @version 1.0
  */

@Singleton
class CaptchaController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getCaptcha(w: Int, h: Int) = Action { implicit request =>
    val captcha = CaptchaInfo.create(w, h)

    val data = captcha.value
    val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => data)
    Ok.chunked(dataContent).withSession(request.session + ("captcha" -> Codecs.sha1(captcha.text)))
  }

}
