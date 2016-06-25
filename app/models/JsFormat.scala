package models

import models.reply.{Reply, ReplyListTree, ReplyListWrapper}
import play.api.libs.json.Json

/**
  * Created by xjpz on 2016/5/28.
  */

trait JsFormat {

  implicit val UserJSONForma = Json.format[User]
  implicit val UserListWrapperFormat = Json.format[UserListWrapper]


  implicit val ArticleJSONForma = Json.format[Article]
  implicit val ArticleListWrapperFormat = Json.format[ArticleListWrapper]

  implicit val ReplyJSONFormat = Json.format[Reply]
  implicit val ReplyListWrapperJSONFormat = Json.format[ReplyListWrapper]

  implicit val LinkJSONFormat = Json.format[Link]
  implicit val LinkListWrapperJSONFormat = Json.format[LinkListWrapper]
  implicit val ReplyListTreeJSONFormat = Json.format[ReplyListTree]

  implicit val MoodJSONFormat = Json.format[Mood]
}
