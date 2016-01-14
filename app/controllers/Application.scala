package controllers

import java.text.SimpleDateFormat
import java.util.Date

import globals.Global
import models.other.{Mail, MailJsonTrait, Mails}
import models.{Reply, ReplyWrapper, ReplysActor, _}
import play.api.Play
import play.api.libs.json.Json
import utils.MD5
//play.Cache
import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc._

import scala.language.postfixOps

// Akka imports
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.apache.commons.codec.binary.Base64

import scala.concurrent.Await
import scala.concurrent.duration._

object Application extends Controller with ArticleJSONTrait with MailJsonTrait {

    implicit val timeout = Timeout(10 seconds)
    implicit lazy val system = ActorSystem()
    implicit lazy val aActor = system.actorOf(Props(new ArticlesActor()))
    implicit lazy val uActor = system.actorOf(Props(new UsersActor()))
    implicit lazy val reply2ArticleActor = system.actorOf(Props(new Replys2ArticleActor()))
    implicit lazy val reply2MessageActor = system.actorOf(Props(new Replys2MessageActor()))
//    implicit lazy val messageActor = system.actorOf(Props(new News2MessagesActor))

    private def config = Play.current.configuration
    private val adminEmailOpt = config.getString("site_admin_email")
    private val adminEmailPwdOpt = config.getString("site_admin_email_pwd")

    //清除缓存的接口
    def removeCache(action:String) = Action{
        action match {
            case "all" =>
                Cache.remove("new2MoodHead")
                Cache.remove("new2Mood:List")

                Cache.remove("news2MessageHead")
                Cache.remove("news2Message:List")

                Cache.remove("articleList")
                Cache.remove("articleUIDlist")
                Cache.remove("articleCatalogList")
                Cache.remove("articleActionRank:read")
                Cache.remove("articleActionRank:smile")
                Cache.remove("articleActionRank:reply")
                Cache.remove("articleActionRank:default")

            case _ =>
                Cache.remove(action)

        }
        Ok(Json.obj("code"->"200","message"->"success"))
    }

    //主页index
    def index(pageOpt: Option[Int]) = Action { request =>

        val uid = request.session.get("uid").getOrElse("0").toLong

        val page = if (pageOpt.getOrElse(1) < 1) 0 else pageOpt.getOrElse(1) - 1

        Ok(views.html.index.render(uid, page))
    }

    //去登录页面
    def toLogin = Action { request =>
        Ok(views.html.login.render(request))
    }

    //去注册页面
    def toReg = Action { request =>
        Ok(views.html.login.render(request))
    }

    //关于本站页面
    def about = Action{
        Ok(views.html.about.render())
    }

    //联系本站页面
    def contactus = Action {
        Ok(views.html.contactus.render())
    }

    //去个人简历页面
    def toResume = Action{ request =>
        Ok(views.html.toResume.render(request))
    }

//    def resume = Action {
//        Ok(views.html.resume.render())
//    }

    //去留言板页面
    def toMessage = Action { request =>
        val uid = request.session.get("uid").getOrElse("0").toLong
        Ok(views.html.message.render(uid,request))
    }

    //TODO
    def unfinished = Action{
        Ok(views.html.unfinished.render())
    }

    //去后台管理页面
    def toAdmin = Action{ request =>
        Ok(views.html.toAdmin.render(request))
    }

    //登出
    def logOut = Action { request =>
        val returnUrl = request.getQueryString("url")
        Redirect(returnUrl.getOrElse("/")).withSession(request.session - "uid")

    }

    //去修改密码页面
    def toResetPassword = Action{ request =>
        Ok(views.html.updatePasswd.render(request))
    }

    //404、500错误页面
    def error40x(code:Int) = Action{

        code match {
            case 400 => BadRequest(views.html.error40x(code))
            case 404 => NotFound(views.html.error40x(code))
            case _ => InternalServerError(views.html.error50x("InternalServerError"))
        }

    }

    //动弹一下
    def tweet = Action {
        Ok(views.html.tweets.render())
    }

    //qqConnect Test.....................

    //QQ授权登录回调页面
    def testQcback = Action { request =>
        Ok(views.html.qcback.render(request))
    }

	//根据文章Aid获取文章信息
    def toArticle(aid: Long) = Action { request =>

        val uid = request.session.get("uid").getOrElse("0").toLong

        val articleFuture = aActor ? ArticlesActor.Retrieve(Global.db, aid)
        val articleOpt = Await.result(articleFuture, timeout.duration)
            .asInstanceOf[Option[ArticleWrapper]]

        articleOpt match {
            case x if x.isDefined =>
                val article = articleOpt.get

                val userFuture = uActor ? UsersActor.Find(Global.db, article.uid.get)
                val userOpt = Await.result(userFuture, timeout.duration)
                    .asInstanceOf[Option[UserWrapper]]

                Ok(views.html.article.render(userOpt.get, article, uid,request))

            case _ => NotFound(views.html.error40x(404))
        }

    }

	//去发表文章页面
    def toNewArticle = Action { request =>
        val uid = request.session.get("uid").getOrElse("0").toLong

	    uid match {
		    case 0 => Ok(views.html.login.render(request))
		    case _ => Ok(views.html.article_new.render(uid,request))
	    }

    }

    //登录Form
    def loginForm = Action { request =>
        val reqJson = request.body.asFormUrlEncoded
        val isAjax = if(request.headers.get("X-Requested-With")==Option("XMLHttpRequest")) true else false
        var flag = 1
        var ret = false
        var uid = 0L
        val emailPattern = """[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?""".r

        val usernameListBuff = reqJson.get("username")
        val passwdListBuff = reqJson.get("password")
        if (isAjax && usernameListBuff.nonEmpty && passwdListBuff.nonEmpty) {
            val loginAction = usernameListBuff.head
            val passwdINForm = passwdListBuff.head
            val passwordBase64ForSHA = Option(Base64.encodeBase64String(Global.shaEncoder.digest(passwdINForm.getBytes)))

            val userOpt = emailPattern.findFirstIn(loginAction) match {
                case Some(action) =>
                    //Email
                    val future = uActor ? UsersActor.FindByEmail(Global.db, loginAction)
                    Await.result(future, timeout.duration)
                        .asInstanceOf[Option[UserWrapper]]

                case _ =>
                    //phone or uname
                    if (loginAction.length == 11 && loginAction.startsWith("1")) {
                        val future = uActor ? UsersActor.FindByPhone(Global.db, loginAction)
                        Await.result(future, timeout.duration)
                            .asInstanceOf[Option[UserWrapper]]
                    } else {
                        val future = uActor ? UsersActor.FindByName(Global.db, loginAction)
                        Await.result(future, timeout.duration)
                            .asInstanceOf[Option[UserWrapper]]
                    }

            }

            if (userOpt.isDefined) {
                if (passwordBase64ForSHA == userOpt.get.password) {
                    ret = true
                    uid = userOpt.get.uid.get
                    val userKey = "user:" + uid
                    Cache.set(userKey, uid, 1800)
                }else{
                    flag = 2
                }
            }else{
                flag = 1
            }
        }

        if (ret) {
            Redirect("/").withSession("uid" -> uid.toString)
        } else {
            Ok(Json.obj("flag" -> flag))
        }
    }

    //注册Form
    def registerForm = Action { request =>
        val reqJson = request.body.asFormUrlEncoded
        val isAjax = if(request.headers.get("X-Requested-With")==Option("XMLHttpRequest")) true else false
        var ret = false
        var flag = 1
        var uid = 0L

        val captchaListBuff = reqJson.get("captcha")
        val usernameListBuff = reqJson.get("username")
        val passwdListBuff = reqJson.get("password")
        val emailListBuff = reqJson.get("email")
        val phoneListBuff = reqJson.get("phone")

        val captchaText = request.session.get("captcha").get

	    request.session.-("captcha")

        if (isAjax && captchaText == MD5.hash(captchaListBuff.head.toUpperCase) ) {

            if (usernameListBuff.nonEmpty && passwdListBuff.nonEmpty &&
                emailListBuff.nonEmpty && phoneListBuff.nonEmpty) {

                val username = usernameListBuff.head
                val passwd = passwdListBuff.head
                val email = emailListBuff.head
                val phone = phoneListBuff.head
                val updtime = Option(System.currentTimeMillis() / 1000L)

                Global.db.withSession { implicit session =>
                    flag match {
                        case x if models.Users.userIsExists(username) => flag = 2
                        case x if models.Users.userIsExists(phone) => flag = 3
                        case x if models.Users.userIsExists(email) => flag = 4
                        case _ =>
                            val user = User(Option(username), Option(passwd), Option(email),
                                Option(phone), None, Option(1), Option(0), None, None, None, None, updtime, updtime)
                            //*************
                            //邮件通知   *
                            //*************
                            //邮件正文
                            val mailcontent =
                                s"""$username 您好！欢迎注册！

                    您注册的的帐号:$username
                    密码是:$passwd
                    请您妥善保管!

祝您使用愉快！"""
                            val mail = Mail(adminEmailOpt, Option(email),
                                Option("欢迎注册【心尖偏左Blog】"), Option(mailcontent), adminEmailOpt, adminEmailPwdOpt)

                            Mails.send(mail)

                            val future = uActor ? UsersActor.Init(Global.db, user)
                            val userOpt = Await.result(future, timeout.duration).asInstanceOf[Option[UserWrapper]]

                            if (userOpt.isDefined) {
                                uid = userOpt.get.uid.get
                                ret = true
                            }

                    }
                }
            }
        }

        if (ret) {
            Redirect("/").withSession("uid" -> uid.toString)
        } else {
            Ok(Json.obj("flag" -> flag))
        }
    }

    //发表文章Form
    def addArticleForm() = Action { request =>
        val reqJson = request.body.asFormUrlEncoded.get
        var ret = false
        val uid = request.session.get("uid").getOrElse("0").toLong

        if (uid != 0) {
            val titleListBuff = reqJson("title")
            val contentListBuff = reqJson("content")
            val catalogListBuffer = reqJson("catalog")
            val atypeListBuffer = reqJson("type")

            if (contentListBuff.nonEmpty && titleListBuff.head.trim!="") {
                val title = titleListBuff.head
                val content = contentListBuff.head

                val catalog = if (catalogListBuffer.isEmpty || catalogListBuffer.head.trim== "") None else Option(catalogListBuffer.head)
                val atype = if (atypeListBuffer.isEmpty) 1 else atypeListBuffer.head.toInt

                val article = Article(
                    Option(title),
                    Option(content),
                    catalog,
                    Option(uid),
                    Option(0),
                    Option(atype)
                )

                val future = aActor ? ArticlesActor.Init(Global.db, article)
                val articleOpt = Await.result(future, timeout.duration)
                    .asInstanceOf[Option[ArticleWrapper]]

                if (articleOpt.isDefined) {
                    ret = true
                }
            }
        }

        if (ret) {
            Redirect("/")
        } else {
            InternalServerError(views.html.error50x("发表失败！请重试，如无法解决请联系管理员。"))
        }
    }

    //发表文章评论Form
    def addReplyForm() = Action { request =>
        val reqJson = request.body.asFormUrlEncoded

        val uid = request.session.get("uid").getOrElse("0").toLong

        var ret = false
        var resultAid = 0L

        val captchaText = request.session.get("captcha")
        val captchaBuff = reqJson.get("captcha")
        val nameListBuffer = reqJson.get("name")
        val emailListBuffer = reqJson.get("email")
        request.session.-("captcha")

        val geustAuth = Option(MD5.hash(captchaBuff.head.toUpperCase))==captchaText && nameListBuffer.nonEmpty && emailListBuffer.nonEmpty

        if(uid !=0L || geustAuth ){

            val aidListBuff = reqJson.get("aid")
            val urlListBuffer = reqJson.get("url")
            val contentListBuff = reqJson.get("content")
            val quoteListBuffer = reqJson.get("quote")

            if (aidListBuff.nonEmpty && contentListBuff.nonEmpty && quoteListBuffer.nonEmpty) {

                val name = if (nameListBuffer.isEmpty) null else nameListBuffer.head
                val url = if (urlListBuffer.isEmpty) null else urlListBuffer.head
                val email = if (emailListBuffer.isEmpty) null else emailListBuffer.head
                val aid = aidListBuff.head
                val content = contentListBuff.head
                val quote = quoteListBuffer.head
                val contentFormat = if(quote!="0") content.substring(content.indexOf(":")+1) else content

                val reply = Reply(
                    Option(aid.toLong),
                    Option(uid),
                    Option(name),
                    Option(url),
                    Option(email),
                    Option(contentFormat),
                    Option(quote.toLong)
                )

                val future = reply2ArticleActor ? ReplysActor.Init(Global.db, reply)
                val replyOpt = Await.result(future, timeout.duration).asInstanceOf[Option[ReplyWrapper]]

                if (replyOpt.isDefined) {
                    ret = true
                    resultAid = aid.toLong
                }
            }
        }
        if (ret) {
            Redirect("/blog/to/article/"+resultAid+"#article_comment")
        } else {
            InternalServerError(views.html.error50x("评论失败！请重试，如无法解决请联系管理员。"))
        }
    }

    //文章点赞
    def smile(aid: Long) = Action { request =>
        //判断是否为ajax请求
        val isAjax = if(request.headers.get("X-Requested-With")==Option("XMLHttpRequest")) true else false
        val uid = request.session.get("uid").getOrElse("0").toLong
        if(isAjax ){
            aActor ? ArticlesActor.SmileCount(Global.db, aid)
        }
        Ok(views.html.index.render(uid, 0))
    }

    //留言评论
    def addMessageReplyForm() = Action { request =>
        val reqJson = request.body.asFormUrlEncoded

        val uid = request.session.get("uid").getOrElse("0").toLong
        var ret = false
        var resultAid = 0L

        val captchaText = request.session.get("captcha")
        val captchaBuff = reqJson.get("captcha")
        val nameListBuffer = reqJson.get("name")
        val emailListBuffer = reqJson.get("email")
        request.session.-("captcha")

        val geustAuth = Option(MD5.hash(captchaBuff.head.toUpperCase))==captchaText && nameListBuffer.nonEmpty && emailListBuffer.nonEmpty

        if(uid !=0L || geustAuth ){
            val aidListBuff = reqJson.get("aid")
            val urlListBuffer = reqJson.get("url")
            val contentListBuff = reqJson.get("content")
            val quoteListBuffer = reqJson.get("quote")

            if (aidListBuff.nonEmpty && contentListBuff.nonEmpty &&
                quoteListBuffer.nonEmpty) {

                val name = if (nameListBuffer.isEmpty) null else nameListBuffer.head
                val url = if (urlListBuffer.isEmpty) null else urlListBuffer.head
                val email = if (emailListBuffer.isEmpty) null else emailListBuffer.head

                val aid = aidListBuff.head
                val content = contentListBuff.head
                val quote = quoteListBuffer.head

                val contentFormat = if(quote!="0") content.substring(content.indexOf(":")+1) else content

                val reply = Reply(
                    Option(aid.toLong),
                    Option(uid),
                    Option(name),
                    Option(url),
                    Option(email),
                    Option(contentFormat),
                    Option(quote.toLong)
                )

                val future = reply2MessageActor ? ReplysActor.Init(Global.db, reply)
                val replyOpt = Await.result(future, timeout.duration).asInstanceOf[Option[ReplyWrapper]]

                if (replyOpt.isDefined) {
                    ret = true
                    resultAid = aid.toLong
                }
            }
        }

        if (ret) {
            Redirect(routes.Application.toMessage())
        } else {
            InternalServerError(views.html.error50x("留言失败！请重试，如无法解决请联系管理员。"))
        }
    }

    //查询标签
    def toCatalogArticle(catalog: String) = Action { request =>

        val uid = request.session.get("uid").getOrElse("0").toLong

        val articleFuture = aActor ? ArticlesActor.QueryWithCatalog(Global.db, catalog, 0, 10)
        val articleOpt = Await.result(articleFuture, timeout.duration)
            .asInstanceOf[Option[ArticleListWrapper]]

        val article = articleOpt.get.articles.get.head

        val userFuture = uActor ? UsersActor.Find(Global.db, article.uid.get)
        val userOpt = Await.result(userFuture, timeout.duration)
            .asInstanceOf[Option[UserWrapper]]

        if(userOpt.isDefined){
            Ok(views.html.article.render(userOpt.get, article, uid,request))
        }else{
            NotFound(views.html.error40x(404))
        }

    }

    //获取用户文章列表
    def queryWithUid(uid: Long,pageOpt:Option[Int]) = Action { request =>

        val loginUID = request.session.get("uid").getOrElse("0").toLong

        val page = if (pageOpt.getOrElse(1) < 1) 0 else pageOpt.getOrElse(1) - 1

        Ok(views.html.userArticleList.render(uid,loginUID, page))
    }

    //修改用户密码Form
    def updatePasswordForm() = Action { request =>
        val reqJson = request.body.asFormUrlEncoded

        var ret = false

        val captchaListBuffer = reqJson.get("captcha")

        if(captchaListBuffer.nonEmpty){
            val captchaForm = captchaListBuffer.head
            val captchaText = request.session.get("captcha").get

	        request.session.-("captcha")

            if(captchaText == MD5.hash(captchaForm.toUpperCase)){
                val nameListBuffer = reqJson.get("name")
                val emailListBuff = reqJson.get("email")
                val passwdListBuff = reqJson.get("password")

                if (nameListBuffer.nonEmpty && emailListBuff.nonEmpty && passwdListBuff.nonEmpty) {

                    val name = nameListBuffer.head
                    val email = emailListBuff.head

                    val passwdINForm = passwdListBuff.head
                    val passwordBase64ForSHA = Base64.encodeBase64String(Global.shaEncoder.digest(passwdINForm.getBytes))

                    val future = uActor ? UsersActor.FindByEmail(Global.db, email)
                    val userOpt = Await.result(future, timeout.duration).asInstanceOf[Option[UserWrapper]]

                    if(userOpt.isDefined){
                        val user = userOpt.get
                        val queryEmail = user.email.getOrElse("")
                        val queryName = user.name.getOrElse("")
                        if(name== queryName && email.toUpperCase == queryEmail.toUpperCase){
                            val updPasswdFuture = uActor ? UsersActor.UpdatePassWord(Global.db, user.uid.get, passwordBase64ForSHA)

                            //*************
                            //邮件通知   *
                            //*************

                            val username = user.name.get
                            val mailcontent =
                                s"""$username 您好！你的 【心尖偏左Blog】 密码已重置

                                如非本人操作，请立即更改密码，并联系管理员。

祝你使用愉快!"""
                            val mail = Mail(
                                adminEmailOpt,
                                Option(email),
                                Option("你的密码已重置【心尖偏左个人博客】"),
                                Option(mailcontent),
                                adminEmailOpt,
                                adminEmailPwdOpt
                            )

                            Mails.send(mail)

                            ret = true
                        }
                    }
                }
            }
        }

        if(ret){
            Redirect("/blog/to/login")
        }else{
            InternalServerError(views.html.error50x("密码重置失败！请重试，如无法解决请联系管理员。"))
        }
    }

    //去修改文章页面
    def toUpdateArticle(aid:Long) = Action{ request =>

        val uid = request.session.get("uid").getOrElse("0").toLong
        val articleFuture = aActor ? ArticlesActor.Retrieve(Global.db, aid)
        val articleOpt = Await.result(articleFuture, timeout.duration).asInstanceOf[Option[ArticleWrapper]]

        if(articleOpt.isDefined && articleOpt.get.uid.getOrElse(0) == uid){
            Ok(views.html.article_update.render(uid,articleOpt.get,request))
        }else{
            Ok(views.html.error40x(404))
        }
    }

    //修改文章Form
    def updateArticleForm() = Action { request =>
        val reqJson = request.body.asFormUrlEncoded.get

        val aidListBuff = reqJson("aid")
        val titleListBuff = reqJson("title")
        val contentListBuff = reqJson("content")
        val catalogListBuffer = reqJson("catalog")
        val statusListBuffer = reqJson("status")

        val aid = aidListBuff.head.toLong
        val title = titleListBuff.head
        val content = contentListBuff.head
        val catalog = catalogListBuffer.head
        val status = statusListBuffer.head.toInt
        val uid = request.session.get("uid").getOrElse("0").toLong

        val article = Article(
            Option(title),
            Option(content),
            Option(catalog),
            Option(uid),
            Option(status),
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            Option(aid)
        )

        val future = aActor ? ArticlesActor.Update(Global.db, article)
        val updateArticleOpt = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ArticleWrapper]]

	    updateArticleOpt match {
		    case x if updateArticleOpt.isDefined => Redirect("/")
		    case _ => Ok(views.html.error40x(404))
	    }
    }

    //验证简历密码Form
    def checkResumeForm = Action { request =>
        val reqJson = request.body.asFormUrlEncoded.get
        val passwdListBuff = reqJson("password")

	    passwdListBuff match {
		    case x if passwdListBuff.nonEmpty && passwdListBuff.head == new SimpleDateFormat("MMddHH").format(new Date()) => Ok(views.html.resume.render())
		    case _ => Redirect("/blog/to/resume")
	    }

    }

    //验证后台管理密码Form
    def checkAdminForm = Action { request =>
        val reqJson = request.body.asFormUrlEncoded.get

        val unameListBuff = reqJson("uname")
        val passwdListBuff = reqJson("password")
        val codeListBuff = reqJson("code")

        val uname = unameListBuff.head
        val passwd = passwdListBuff.head
        val code = codeListBuff.head

        passwdListBuff match {
            case x if uname=="admin" && codeListBuff.nonEmpty && code == new SimpleDateFormat("MMddHH").format(new Date()) =>
                val future = uActor ? UsersActor.FindByName(Global.db, uname)
                val userOpt = Await.result(future, timeout.duration).asInstanceOf[Option[UserWrapper]]
                userOpt match {
                    case x: Option[UserWrapper] if x.isDefined && passwd == userOpt.get.password.get => Ok(views.html.admin.render())
                    case _ => Redirect("/blog/to/admin")
                }

            case _ => Redirect("/blog/to/admin")
        }
    }

    //获取登录用户Uid
    def user = Action { request =>
        val uid = request.session.get("uid").getOrElse("0").toLong

        uid match {
            case x if x!=0 => Ok(views.html.user.render(uid))
            case _ => Redirect("/blog/to/login")
        }
    }

    //文章归档
    def archives = Action { request =>
            val uid = request.session.get("uid").getOrElse("0").toLong
        Ok(views.html.archives.render(uid))
    }

    //创建互联授权账户
    def addConnUserForm() = Action { request =>
        val reqJson = request.body.asFormUrlEncoded
        var retOpt = None:Option[UserWrapper]

        val nameListBuff = reqJson.get("name")
        val openidListBuff = reqJson.get("openid")
        val tokenListBuff = reqJson.get("token")
        val otypeListBuff = reqJson.get("otype")

        val updtime = Option(System.currentTimeMillis() / 1000L)
        val name = nameListBuff.head
        val otype = otypeListBuff.head
        val openid = openidListBuff.head
        val token = tokenListBuff.head

        val user = User(
            Option(name),
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            updtime,
            updtime
        )
            Option(otype) match{
                case Some("1") =>
                    //初始化user的qq openid
                    user.qopenid = Option(openid)
                    user.qtoken = Option(token)

                    val future = uActor ? UsersActor.AddQConnUser(Global.db,user)
                    retOpt = Await.result(future, timeout.duration)
                        .asInstanceOf[Option[UserWrapper]]

                case Some("2") =>
                    //初始化user的sina openid
                    user.sopenid = Option(openid)
                    user.stoken = Option(token)
                    val future = uActor ? UsersActor.AddSConnUser(Global.db,user)
                    retOpt = Await.result(future, timeout.duration)
                        .asInstanceOf[Option[UserWrapper]]

                case _ =>
            }

        if(retOpt.isDefined){
            Ok("/").withSession("uid" -> retOpt.get.uid.getOrElse(0L).toString)
        }else{
            InternalServerError(views.html.error50x("登录失败！请重试，如无法解决请联系管理员。"))
        }
    }

    //删除文章
    def deleteArticle(aid:Long) = Action{ request =>
        val uid = request.session.get("uid").getOrElse("0").toLong
        var ret = None: Option[ArticleWrapper]

        if(uid!=0){
            val future = aActor ? ArticlesActor.Delete(Global.db,aid, uid)
            ret = Await.result(future, timeout.duration)
                .asInstanceOf[Option[ArticleWrapper]]
        }

        if (ret.isDefined) {
            Redirect("/blog/to/user/article/"+uid)
        } else {
            InternalServerError(views.html.error50x("删除失败！请重试，如无法解决请联系管理员。"))
        }
    }

    //文章评论点赞

    def smileReply2Article(rid:Long) = Action {
        var ret = None: Option[ReplyWrapper]

        val future = reply2ArticleActor ? ReplysActor.Smile(Global.db, rid: Long)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyWrapper]]

        if (ret.isDefined) {
            Redirect("/blog/to/article/"+ret.get.aid.get+"#article_comment")
        } else {
            InternalServerError(views.html.error50x("点赞失败！，请重试，如无法解决请联系管理员。"))
        }
    }

    def smileReply2Message(rid:Long) = Action {
        var ret = None: Option[ReplyWrapper]

        val future = reply2MessageActor ? ReplysActor.Smile(Global.db, rid: Long)
        ret = Await.result(future, timeout.duration)
            .asInstanceOf[Option[ReplyWrapper]]

        if (ret.isDefined) {
            Redirect("/blog/to/message")
        } else {
            InternalServerError(views.html.error50x("点赞失败！，请重试，如无法解决请联系管理员。"))
        }
    }

}
