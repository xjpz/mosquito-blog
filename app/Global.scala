package globals

import java.security.MessageDigest

import filters.{CORSFilter, LoggingFilter}
import play.api.Play.current
import play.api._
import play.api.db.DB
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import play.filters.gzip.GzipFilter

import scala.concurrent.Future
import scala.slick.driver.MySQLDriver.simple._


object Global extends WithFilters(LoggingFilter,new GzipFilter()) with GlobalSettings {

    implicit lazy val shaEncoder = MessageDigest.getInstance("SHA-1")
    implicit lazy val db = Database.forDataSource(DB.getDataSource("default"))

    def wrap(raw: Option[JsValue]): Option[String] = {
        var wrapper = None: Option[String]
        if (raw.isDefined) {
            wrapper = Option(raw.get.toString())
        }
        wrapper
    }

    override def onStart(app: Application) {
        Logger.info("mosquito service has started.")
    }

    override def onStop(app: Application) {
        Logger.info("mosquito service has stopped.")
    }

    override def onError(request: RequestHeader, ex: Throwable) = {
        Future.successful(InternalServerError(
            views.html.error50x(ex.getLocalizedMessage)
        ))
    }

    override def onHandlerNotFound(request: RequestHeader) = {
        Future.successful(
            NotFound(views.html.error40x(404))
        )
    }

    override def onBadRequest(request: RequestHeader, error: String) = {
        Future.successful(
            BadRequest(views.html.error40x(400))
        )
    }


}