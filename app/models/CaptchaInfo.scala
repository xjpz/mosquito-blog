package models

import java.io.InputStream

import utils.CaptchaGenerator

case class CaptchaInfo(text: String, value: InputStream)

object CaptchaInfo {

  def create(w: Int, h: Int): CaptchaInfo = {
    val captcha = CaptchaGenerator.getCpatcha(w: Int, h: Int)
    CaptchaInfo(captcha._1, captcha._2)
  }

}
