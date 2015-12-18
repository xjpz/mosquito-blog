package controllers


import models.news.News2MessagesActor

// Akka imports

import akka.actor.Props

import scala.language.postfixOps


object News2MessageCtrl extends NewsCtrlTrait {
    override lazy val newsActor = system.actorOf(Props[News2MessagesActor])
}
