package filters

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future

object LoggingFilter extends Filter {
    def apply(nextFilter: (RequestHeader) => Future[Result])
             (requestHeader: RequestHeader): Future[Result] = {
        val startTime = System.currentTimeMillis
        //        val ip = requestHeader.host
        nextFilter(requestHeader).map { result =>
            val endTime = System.currentTimeMillis
            val requestTime = endTime - startTime
            val msg = s"${requestHeader.host} ${requestHeader.method} ${requestHeader.uri} " +
                s"took ${requestTime}ms and returned ${result.header.status}"
            play.Logger.of("access").info(msg)
            // Logger.info(msg)
            result.withHeaders("Request-Time" -> requestTime.toString)
        }
    }
}
