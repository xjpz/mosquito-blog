package controllers

import akka.actor.Props
import models.Replys2ArticleActor

import scala.language.postfixOps

/**
 * Created by xjpz_wz on 2015/9/7.
 */
object Reply2ArticleCtrl extends ReplyCtrlTrait{
    override lazy val rActor = system.actorOf(Props[Replys2ArticleActor])
}
