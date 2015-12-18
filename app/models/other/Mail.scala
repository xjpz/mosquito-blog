package models.other

import java.util.{Date, Properties}
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import akka.actor.Actor
import models.other
import models.other.MailsActor.{Recv, Send}
import play.api.libs.json.Json
import sun.rmi.runtime.Log

case class Mail(
                   var from: Option[String] = None,
                   var to: Option[String] = None,
                   var subject: Option[String] = None,
                   var content: Option[String] = None,
                   var username: Option[String] = None,
                   var password: Option[String] = None)

trait MailJsonTrait {
    implicit lazy val MailJSONFormat = Json.format[Mail]
}

object Mails extends MailJsonTrait {
    private val bodyText = new StringBuffer()
    val isSSL: java.lang.Boolean = true
    val sendPort: java.lang.Integer = 465
    val recvPort: java.lang.Integer = 995
    val isAuth: java.lang.Boolean = true
    val protocol: String = "pop3"

    def getBodyText = {
        bodyText.toString
    }

    def send(mail: Mail): Boolean = {
        var ret = false

        val from = mail.from.get
        val host = "smtp." + from.substring(from.lastIndexOf("@") + 1)

        val props: Properties = new Properties

        props.put("mail.smtp.ssl.enable", isSSL)
        props.put("mail.smtp.host", host)
        props.put("mail.smtp.port", sendPort)
        props.put("mail.smtp.auth", isAuth)

        val session: Session = Session.getInstance(props, new Authenticator() {
            protected override def getPasswordAuthentication: PasswordAuthentication = {
                new PasswordAuthentication(mail.username.get, mail.password.get)
            }
        })

        //邮件昵称
        val nick = javax.mail.internet.MimeUtility.encodeText("心尖偏左博客系统")

        try {
            val message: Message = new MimeMessage(session)
            message.setFrom(new InternetAddress(nick + "<" + mail.from.get + ">"))
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.to.get))
            message.setSubject(mail.subject.get)
            message.setText(mail.content.get)
            Transport.send(message)
            ret = true
        } catch {
            case e: Exception => play.Logger.of("warning").info(e.getMessage)
                ret = false
        }

        ret
    }

    def recv(mail: Mail): Option[Mail] = {
        var ret = None: Option[Mail]

        val props: Properties = new Properties
        val from = mail.from.get
        val host = "pop." + from.substring(from.lastIndexOf("@") + 1)

        props.put("mail.pop3.ssl.enable", isSSL)
        props.put("mail.pop3.host", host)
        props.put("mail.pop3.port", recvPort)

        val session: Session = Session.getInstance(props)
        var store: Store = null
        var folder: Folder = null
        store = session.getStore(protocol)
        store.connect(mail.username.get, mail.password.get)
        folder = store.getFolder("INBOX")
        folder.open(Folder.READ_ONLY)
        val size: Int = folder.getMessageCount
        val message: Message = folder.getMessage(size)

        getMailContent(message)

        val recvfrom: String = message.getFrom.toList.toString()
        val subject: String = message.getSubject
        val date: Date = message.getSentDate
        val content = getBodyText

        val retmail = Mail(
            None,
            Option(recvfrom),
            None,
            Option(subject),
            Option(content)
        )
        ret = Option(retmail)

        if (folder != null) {
            folder.close(false)
        }
        if (store != null) {
            store.close()
        }

        ret
    }

    def getMailContent(part: Part) {
        val contenttype: String = part.getContentType
        val nameindex: Int = contenttype.indexOf("name")
        var conname: Boolean = false
        if (nameindex != -1)
            conname = true

        if (part.isMimeType("text/plain") && !conname) {
            //纯文本右键
            bodyText.append(part.getContent.toString)
        } else if (part.isMimeType("text/html") && !conname) {
            // HTML格式邮件
            bodyText.append(part.getContent.toString)
        } else if (part.isMimeType("multipart/*")) {
            // multipart邮件
            val multipart: Multipart = part.getContent.asInstanceOf[Multipart]
            val counts: Int = multipart.getCount
            for (i <- 0 until counts) {
                getMailContent(multipart.getBodyPart(i))
            }
        } else if (part.isMimeType("message/rfc822")) {
            getMailContent(part.getContent.asInstanceOf[Part])
        }
    }

}

object MailsActor {

    case class Send(mail: Mail)

    case class Recv(mail: Mail)

}

class MailsActor extends Actor {
    def receive: Receive = {
        case Send(mail: Mail) => {
            sender ! Mails.send(mail)
        }
        case Recv(mail: Mail) => {
            sender ! other.Mails.recv(mail)
        }
    }
}