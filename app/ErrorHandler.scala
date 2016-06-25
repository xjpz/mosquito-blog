
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._

/**
  * Created by xjpz on 2016/5/30.
  */

class ErrorHandler extends HttpErrorHandler {

    def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
        Future.successful(
            Ok(views.html.error40x(statusCode,message))
        )
    }

    def onServerError(request: RequestHeader, exception: Throwable) = {
        Future.successful(
            Ok(views.html.error50x(exception.getMessage))
        )
    }
}
