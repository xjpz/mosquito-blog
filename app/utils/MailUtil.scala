package utils

import javax.inject.Inject

import play.api.libs.mailer._


/**
  * Created by xjpz9 on 2016/5/30.
  */


class MailUtil @Inject()(mailerClient: MailerClient) {

  def send(email: Email) = mailerClient.send(email)

  def send(form: String, subject: String, to: Seq[String],
           bodyText: Option[String], bodyHtml: Option[String], charset: Option[String]) = {

    val email = Email(
      subject,
      form,
      to,
      bodyText,
      bodyHtml,
      charset)

    mailerClient.send(email)
  }
}
