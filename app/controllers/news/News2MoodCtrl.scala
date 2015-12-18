package controllers

import models.news._

// Akka imports

import akka.actor.Props

import scala.language.postfixOps

object News2MoodCtrl extends NewsCtrlTrait {
    override lazy val newsActor = system.actorOf(Props[News2MoodsActor])
}
