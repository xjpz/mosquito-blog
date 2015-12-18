package controllers

import akka.actor.Props
import models.Replys2MessageActor

/**
 * Created by xjpz_wz on 2015/9/7.
 */
object Replay2MessageCtrl extends ReplyCtrlTrait{
    override lazy val rActor = system.actorOf(Props[Replys2MessageActor])
}
