package controllers

import models.other.{Mail, MailJsonTrait, MailsActor}
import play.api.libs.json.Json
import play.api.mvc._

// Play cached support
import scala.language.postfixOps

// Akka imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

object MailCtrl extends Controller with MailJsonTrait{

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val mailActor = system.actorOf(Props(new MailsActor()))

    def send = Action { request =>
        val reqJson = request.body.asJson.get

        val from = (reqJson \"from").asOpt[String]
        val to = (reqJson \"to").asOpt[String]
        val subject = (reqJson \"subject").asOpt[String]
        val content = (reqJson \"content").asOpt[String]
        val username = (reqJson \"from").asOpt[String]
        val password = (reqJson \"password").asOpt[String]

        val mail = Mail(
            from,
            to,
            subject,
            content,
            username,
            password
        )

        val future = mailActor ? MailsActor.Send(mail)
        val ret = Await.result(future, timeout.duration)
            .asInstanceOf[Boolean]

        if(ret){
            Ok(Json.obj("code" -> 200,"status" -> "Success"))
        }else{
            InternalServerError(Json.obj("code" -> 500,"status" -> "Fail"))
        }

    }

    def recv = Action {request =>
        val reqJson = request.body.asJson.get

        val from = (reqJson \"from").asOpt[String]
        val to = (reqJson \"to").asOpt[String]
        val subject = (reqJson \"subject").asOpt[String]
        val content = (reqJson \"content").asOpt[String]
        val username = (reqJson \"username").asOpt[String]
        val password = (reqJson \"password").asOpt[String]

        val mail = Mail(
            from,
            to,
            subject,
            content,
            username,
            password
        )

        var ret= None:Option[Mail]
        val future = mailActor ? models.other.MailsActor.Recv(mail)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[Mail]]

        if(ret.isDefined){
            Ok(Json.obj("code" -> 200,"status" -> "Success","mails" -> ret.get))
        }else{
            InternalServerError(Json.obj("code" -> 500,"status" -> "Fail"))
        }

    }

}
