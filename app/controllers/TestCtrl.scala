package controllers

import globals.Global
import models.{UserWrapper, UsersActor}
import org.apache.commons.codec.binary.Base64
import play.api.libs.json._
import play.api.mvc.{Controller, _}
import utils.HexStringUtil

import scala.language.postfixOps
// Akka imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
/**
 * Created by xjpz_wz on 2015/10/5.
 */
object TestCtrl extends Controller{

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val uActor = system.actorOf(Props(new UsersActor()))

    def test1 = Action { request =>
        val isAjax = if(request.headers.get("X-Requested-With")==Option("XMLHttpRequest")) true else false
//        val token = request.headers
//        val address = request.remoteAddress
//        val url = request.path
//        println(page)
//        println(token)
//        println(address)
        println(isAjax)

        Ok("test")
    }

    def test2 = Action{

        Ok(views.html.test.render())
    }

    def main (args: Array[String]) {
//        var str:String =_
    }

    def test3 = Action{

        val j = Json.obj("text" -> "如何和土壕做朋友？","color"->"#666" ,"size"->1,"position"->1)

        val k = Json.obj("text" -> "未来一周的天气怎么样？","color"->"#666" ,"size"->1,"position"->1)

        val list = List(j,k)
        val jsonFinal = Json.obj("1"->list)

        Ok(jsonFinal)
    }

//    def test4(startNun:Int,endNum:Int) = Action{
//        for(i <- startNun to endNum){
//            val future = uActor ? UsersActor.Find(Global.db, i)
//            val queryUser = Await.result(future, timeout.duration).asInstanceOf[Option[UserWrapper]]
//            if(queryUser.isDefined){
//                val user = queryUser.get
//                val uid = user.uid.get
//                val passwdHex2StringOpt = HexStringUtil.hex2string(user.password)
//                if(passwdHex2StringOpt.isDefined){
//                    val passwordFinal = Base64.encodeBase64String(Global.shaEncoder.digest(passwdHex2StringOpt.get.getBytes))
//                    val updateFuture = uActor ? UsersActor.UpdatePassWord(Global.db, uid, passwordFinal)
//                    println(passwdHex2StringOpt)
//                    println(passwordFinal)
//                }
//            }
//        }
//
//        Ok("success")
//    }
}
