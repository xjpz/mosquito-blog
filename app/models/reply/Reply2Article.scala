package models.reply

import slick.driver.MySQLDriver.api._

import scala.language.postfixOps

/**
  * Created by wenzh on 2016/5/29.
  */

object Reply2Article extends Replys{
  override val table = TableQuery[ReplysTable](
    (tag: Tag) => new ReplysTable(tag, "reply2article")
  )
}
