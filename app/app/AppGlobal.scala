package app

/**
  * Created by xjpz on 2021/8/10.
  */
object AppGlobal {

  val siteName = "心尖偏左"

  val siteKeyword = "心尖偏左,xjpz,xjpz.cc,心尖偏左博客,xjpzblog,心尖偏左Blog,心尖依然偏左,scala+play,Scala满足了我对编程语言的所有幻想."

  val siteDescription = "心尖偏左Blog是一个用Scala+Play搭建的以分享知识、技术交流为目的的博客系统。博主心尖偏左是一个Scala语言的忠实粉丝，热爱Scala。"

  val siteDomain = "xjpz.cc"

  val siteHost = "https://" + siteDomain

  val siteEmail = "764613916@qq.com"

  val siteCopyright = "Copyright ©2021 心尖偏左"

  val qqConnectAppId = "101964847"

  val qqAuthUrl = s"https://graph.qq.com/oauth2.0/authorize?client_id=$qqConnectAppId&response_type=token&scope=get_user_info&redirect_uri=https%3A%2F%2F$siteDomain%2Fqclogin"

  val beianCode = "鄂ICP备2021013794号"

  val siteWangBeiCode = "42011602000971"

  val qcCode2AccessTokenUrl = (secret:String,code:String) => s"https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=$qqConnectAppId&client_secret=$secret&code=$code&redirect_uri=$siteHost&fmt=json"

  val qcToken2OpenIdUrl = (token:String) => s"https://graph.qq.com/oauth2.0/me?access_token=$token&fmt=json"
}
