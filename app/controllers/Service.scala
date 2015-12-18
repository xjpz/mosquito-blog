package controllers

import globals.Global
import models._
import models.news.NewsActor.{FindHead, Query}
import models.news.{News2MoodsActor, NewsWrapper, News2MessagesActor, NewsListWrapper}

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

// Akka imports

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by xjpz on 2015/7/18.
 */
object Service extends UserJSONTrait with ReplyJSONTrait {

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val uActor = system.actorOf(Props(new UsersActor()))
    implicit lazy val aActor = system.actorOf(Props(new ArticlesActor()))

    implicit lazy val reply2ArticleActor = system.actorOf(Props(new Replys2ArticleActor()))
    implicit lazy val reply2MessageActor = system.actorOf(Props(new Replys2MessageActor()))

    implicit lazy val news2messageActor = system.actorOf(Props(new News2MessagesActor))
    implicit lazy val news2moodActor = system.actorOf(Props(new News2MoodsActor))

    implicit lazy val linkActor = system.actorOf(Props(new LinksActor))
    implicit lazy val ucustomActor = system.actorOf(Props [CustomsActor])


    //获取今日签名
    def findNews2MoodHead = {
        var ret = None:Option[String]

        val future = news2moodActor ? FindHead(Global.db)
        val queryNews2moodOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[NewsWrapper]]
        if(queryNews2moodOpt.isDefined){
            ret = queryNews2moodOpt.get.content
        }

        ret
    }

    //获取用户个性签名
    def findUserDescrp(uid:Long) = {
        var ret = None: Option[String]

        val future = uActor ? UsersActor.Find(Global.db, uid)
        val UserOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[UserWrapper]]
        if (UserOpt.isDefined) {
            ret = UserOpt.get.descrp
        }
        ret
    }

	//获取用户名
    def getUname(uid: Long) = {
        var ret = None: Option[String]

        val future = uActor ? UsersActor.Find(Global.db, uid)
        val UserOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[UserWrapper]]
        if (UserOpt.isDefined) {
            ret = UserOpt.get.name
        }
        ret
    }

	//获取文章评论
    def queryArticleReply(aid: Long) = {
        val ret = new ListBuffer[ReplyWrapper]

        val future = reply2ArticleActor ? ReplysActor.Query(Global.db, aid: Long)
        val replyListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyListWrapper]]
        if (replyListWrapperOpt.isDefined) {
            val repiyList = replyListWrapperOpt.get.replys
            if (repiyList.nonEmpty) {
                ret ++= repiyList.get
            }
        }

        ret
    }

	//获取文章（评论）子评论
    def queryArticleReplyChild(rid: Long) = {
        val ret = new ListBuffer[ReplyWrapper]

        val future = reply2ArticleActor ? ReplysActor.QueryChild(Global.db, rid: Long)
        val replyListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyListWrapper]]
        if (replyListWrapperOpt.isDefined) {
            val repiyList = replyListWrapperOpt.get.replys
            if (repiyList.nonEmpty) {
                ret ++= repiyList.get
            }
        }

        ret
    }

	//获取留言
    def queryMessageReply(mid:Long) = {
        val ret = new ListBuffer[ReplyWrapper]
        val future = reply2MessageActor ? ReplysActor.Query(Global.db, mid: Long)
        val replyListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyListWrapper]]
        if (replyListWrapperOpt.isDefined) {
            val repiyList = replyListWrapperOpt.get.replys
            if (repiyList.nonEmpty) {
                ret ++= repiyList.get
            }
        }
        ret
    }

	//获取留言评论
    def queryMessageReplyChild(rid: Long) = {
        val ret = new ListBuffer[ReplyWrapper]

        val future = reply2MessageActor ? ReplysActor.QueryChild(Global.db, rid: Long)
        val replyListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyListWrapper]]
        if (replyListWrapperOpt.isDefined) {
            val repiyList = replyListWrapperOpt.get.replys
            if (repiyList.nonEmpty) {
                ret ++= repiyList.get
            }
        }

        ret
    }

	//获取留言用户名（包括匿名）
    def findReply2MessageUName(rid:Long) = {
        var ret = None:Option[String]
        val future = reply2MessageActor ? ReplysActor.Retrieve(Global.db, rid: Long)
        val replyWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyWrapper]]
        if(replyWrapperOpt.isDefined){
            ret = replyWrapperOpt.get.name
        }
        ret
    }

	//获取文章评论用户名（包括匿名）
    def findReply2ArticleUName(rid:Long) = {
        var ret = None:Option[String]
        val future = reply2ArticleActor ? ReplysActor.Retrieve(Global.db, rid: Long)
        val replyWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyWrapper]]
        if(replyWrapperOpt.isDefined){
            ret = replyWrapperOpt.get.name
        }
        ret
    }

	//获取文章评论数
    def queryReplyCount(aid: Long) = {
        var ret = 0
        val future = reply2ArticleActor ? ReplysActor.Query(Global.db, aid: Long)
        val replyListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyListWrapper]]
        if (replyListWrapperOpt.isDefined) {
            val repiyList = replyListWrapperOpt.get.replys
            if (repiyList.nonEmpty) {
                ret = repiyList.get.length
            }
        }
        ret
    }

	//获取文章列表
    def articleList(page:Int) = {
        var ret: ArticleListWrapper = null

        val future = aActor ? ArticlesActor.Query(Global.db, page, 20)
        val articleListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]
        if (articleListWrapperOpt.isDefined) {
            ret = articleListWrapperOpt.get
        }
        ret
    }

	//已废弃。。。
    def queryNews2Message = {
        val future = news2messageActor ? Query(Global.db, 0, 20)
        val messageListOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[NewsListWrapper]]
        messageListOpt.get
    }

	//根据Uid获取文章列表
    def articleUIDList(uid: Long,page:Int) = {
        var ret: ArticleListWrapper = null

        uid match {
            case 0 => {
                val future = aActor ? ArticlesActor.Query(Global.db, page, 20)
                val articleListWrapperOpt = Await.result(future, timeout.duration)
                    .asInstanceOf[Option[ArticleListWrapper]]
                if (articleListWrapperOpt.isDefined) {
                    ret = articleListWrapperOpt.get
                }
            }
            case _ => {
                val future = aActor ? ArticlesActor.QueryWithUid(Global.db, uid, 0, 20)
                val articleListWrapperOpt = Await.result(future, timeout.duration)
                    .asInstanceOf[Option[ArticleListWrapper]]
                if (articleListWrapperOpt.isDefined) {
                    ret = articleListWrapperOpt.get
                }
            }
        }
        ret
    }

	//获取标签列表
    def queryCatalog = {
        var ret = None:Option[CatalogList]
        val future = aActor ? ArticlesActor.QueryCatalog(Global.db)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[CatalogList]]
        ret
    }

	//文章排行
	// action -> read(阅读排行)
	// action -> reply(评论排行)
    // action -> smile(推荐排行)
	// action -> default(默认，即根据文章id)
    def queryArticleAction(action:String,page: Option[Int], size: Option[Int]) = {
        var ret: ArticleListWrapper = null

        val future = aActor ? ArticlesActor.QueryAction(Global.db,action, page.getOrElse(0), size.getOrElse(5))
        val articleListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]
        if (articleListWrapperOpt.isDefined) {
            ret = articleListWrapperOpt.get
        }

        ret
    }

	//计算总页数
    def  articleCurrentPage = {
        var pageTotal = 1

        val future = aActor ? ArticlesActor.Query(Global.db, 0, 20)
        val articleListWrapperOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]

        if(articleListWrapperOpt.isDefined){
            val count:Int = articleListWrapperOpt.get.count.getOrElse(0)
            pageTotal = if(count%20==0) count/20 else count/20+1

        }
        pageTotal
    }

	//判断用户是否存在
    def userIsExists(uname:String) =  {
        var ret = false

        val future = uActor ? UsersActor.UserIsExists(Global.db,uname)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Boolean]

        ret
    }

	//获取活跃用户
    def queryActiveUser() =  {

        val future = uActor ? UsersActor.QueryActiveUser(Global.db,0,100)
        val ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[UserListWrapper]]
        ret
    }

	//获取友情链接
    def queryLink() = {
        val future = linkActor ? LinksActor.Query(Global.db,0,10)
        val ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[LinkListWrapper]]
        ret
    }

	//获取自定义样式
    def findCustom(uid:Long) = {
        var ret = None: Option[CustomWrapper]

        val future = ucustomActor ? CustomsActor.Retrieve(Global.db, uid)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[CustomWrapper]]

        ret
    }

    //获取最新的心情说明
    def queryNewsMood(page: Option[Int], size: Option[Int]) =  {

        var ret = None: Option[NewsWrapper]

        val future = news2moodActor ? Query(Global.db, page.getOrElse(0), size.getOrElse(999999999))
        val queryMoodListOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[NewsListWrapper]]

        if(queryMoodListOpt.isDefined){
            ret = Option(queryMoodListOpt.get.news.get.last)
        }
    }

}
