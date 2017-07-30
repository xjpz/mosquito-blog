package models

import java.security.MessageDigest
import javax.inject.Inject

import org.apache.commons.codec.binary.Base64
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xjpz on 2016/5/26.
  */

case class User(
                 var name: Option[String] = None,
                 var password: Option[String] = None,
                 var email: Option[String] = None,
                 var phone: Option[String] = None,
                 var descrp: Option[String] = None,
                 var utype: Option[Int] = Option(0),
                 var status: Option[Int] = Option(0),
                 var qopenid: Option[String] = None,
                 var qtoken: Option[String] = None,
                 var sopenid: Option[String] = None,
                 var stoken: Option[String] = None,
                 var inittime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                 var updtime: Option[Long] = Option(System.currentTimeMillis() / 1000L),
                 var tombstone: Option[Int] = Option(0),
                 var uid: Option[Long] = None) {

  def patch(user: User): User = {
    this.copy(
      this.name,
      user.password.orElse(this.password),
      this.email,
      this.phone,
      user.descrp.orElse(this.descrp),
      user.utype.orElse(this.utype),
      user.status.orElse(this.status),
      user.qopenid.orElse(this.qopenid),
      user.qtoken.orElse(this.qtoken),
      user.sopenid.orElse(this.sopenid),
      user.stoken.orElse(this.stoken),
      this.inittime,
      Option(System.currentTimeMillis() / 1000L),
      user.tombstone.orElse(this.tombstone),
      this.uid
    )
  }

  def pack(user: User) = {
    val password = user.password
    if (password.isDefined) {
      user.password = Option(Base64.encodeBase64String(MessageDigest.getInstance("SHA-1").digest(password.get.getBytes)))
    }
  }
}

case class UserListWrapper(users: List[User], count: Int)

object User {
  implicit val UserJSONFormat = Json.format[User]
}

object UserListWrapper {
  implicit val UserListWrapperFormat = Json.format[UserListWrapper]
}

class Users @Inject()(articles: Articles)(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private class UsersTable(tag: Tag) extends Table[User](tag, "user") {

    def uid = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def name = column[Option[String]]("name")

    def password = column[Option[String]]("password")

    def email = column[Option[String]]("email")

    def phone = column[Option[String]]("phone")

    def descrp = column[Option[String]]("descrp")

    def utype = column[Option[Int]]("type")

    def status = column[Option[Int]]("status")

    def qopenid = column[Option[String]]("qopenid")

    def qtoken = column[Option[String]]("qtoken")

    def sopenid = column[Option[String]]("sopenid")

    def stoken = column[Option[String]]("stoken")

    def tombstone = column[Option[Int]]("tombstone")

    def inittime = column[Option[Long]]("init_time")

    def updtime = column[Option[Long]]("update_time")

    def * = (name, password, email, phone, descrp, utype, status, qopenid, qtoken,
      sopenid, stoken, inittime, updtime, tombstone, uid) <> ((User.apply _).tupled, User.unapply)
  }

  private val table = TableQuery[UsersTable]

  def queryById(id: Long): DBIO[Option[User]] = table.filter(_.uid === id).result.headOption

  def _queryByName(uname: String): DBIO[Option[User]] = table.filter(_.name === uname).result.headOption

  def _queryByEmail(email: String): DBIO[Option[User]] = table.filter(_.email === email).result.headOption

  def _queryByPhone(phone: String): DBIO[Option[User]] = table.filter(_.phone === phone).result.headOption

  def _query: DBIO[Seq[User]] = table.filter(_.tombstone === 0).result

  def retrieve(uid: Long): Future[Option[User]] = {
    db.run(queryById(uid))
  }

  def init(user: User): Future[Option[Long]] = {
    db.run((table returning table.map(_.uid)) += user)
  }

  def query: Future[Seq[User]] = db.run(_query)

  def queryByName(name: String): Future[Option[User]] = {
    db.run(_queryByName(name))
  }

  def queryByEmail(email: String): Future[Option[User]] = {
    db.run(_queryByEmail(email))
  }

  def queryByPhone(phone: String): Future[Option[User]] = {
    db.run(_queryByPhone(phone))
  }

  def queryByOpenid(openid: String): Future[Option[User]] = {
    db.run(table.filter(_.qopenid === openid).filter(_.tombstone === 0).result.headOption)
  }

  def update(user: User): Future[Int] = {
    val query = table.filter(_.uid === user.uid)

    val update = query.result.head.flatMap { user =>
      query.update(user.patch(user))
    }
    db.run(update)
  }

  def updatePasswd(uid: Long, passwd: String): Future[Int] = {
    db.run(table.filter(_.uid === uid).filter(_.tombstone === 0)
      .map(row => (row.password, row.updtime)).update((Option(passwd), Option(System.currentTimeMillis() / 1000L))))
  }

  def userIsExists(uname: String): Future[Boolean] = {
    val ret = for {
      unameExists <- _queryByName(uname)
      emailExists <- _queryByEmail(uname)
      phoneExists <- _queryByPhone(uname)
    } yield {
      unameExists.isDefined || emailExists.isDefined || phoneExists.isDefined
    }
    db.run(ret.transactionally)
  }

  def queryArticleListJoinUser: Future[Seq[(Article, User)]] = {
    val query = (articles.table.filter(_.tombstone === 0) join
      table.filter(_.tombstone === 0) on (_.uid === _.uid)).sorted(_._1.aid.desc).result
    db.run(query)
  }

  def queryArticleByCatalogJoinUser(catalog: String): Future[Seq[(Article, User)]] = {
    val query = (articles.table.filter(_.catalog like "%" + catalog + "%").sortBy(_.aid.desc) join
      table.filter(_.tombstone === 0) on (_.uid === _.uid)).result
    db.run(query)
  }

}

