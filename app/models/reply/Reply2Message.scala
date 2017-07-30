package models.reply

import slick.driver.MySQLDriver.api._

import scala.language.postfixOps

/**
  * Created by xjpz on 2016/5/29.
  */

object Reply2Message extends Replys {
  override val table = TableQuery[ReplysTable](
    (tag: Tag) => new ReplysTable(tag, "reply2message")
  )
}
