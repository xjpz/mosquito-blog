package models.other

import java.io.InputStream

import utils.CaptchaGenerator

case class CaptchaInfo(text: String, value: InputStream)

object CaptchaInfo {

	def create(): CaptchaInfo = {
		val captcha = CaptchaGenerator.getCpatcha
		CaptchaInfo(captcha._1, captcha._2)
	}

}
