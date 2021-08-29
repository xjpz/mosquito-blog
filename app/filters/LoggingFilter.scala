package filters

/**
  * Created by xjpz on 2016/5/30.
  */

import javax.inject.{Inject, Singleton}
import akka.stream.Materializer
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoggingFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis

    nextFilter(requestHeader).map { result =>

      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime

      requestHeader.method match {
        case "POST" =>
          result.body.consumeData(mat).map { bodyByteString =>
            val body = Json.parse(bodyByteString.decodeString("UTF-8"))
            play.Logger.of("access").info(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status} body ${Json.prettyPrint(body)}")
          }
        case _ =>  play.Logger.of("access").info(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status}")
      }
      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}
