import javax.inject._

import filters.LoggingFilter
import play.api.http.HttpFilters
import play.api.mvc._
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

/**
 * This class configures filters that run on every request. This
 * class is queried by Play to get a list of filters.
 *
 * Play will automatically use filters from any class called
 * `Filters` that is placed the root package. You can load filters
 * from a different class by adding a `play.http.filters` setting to
 * the `application.conf` configuration file.
 *
 * @param gzip Basic environment settings for the current application.
 * @param log A demonstration filter that adds a header to
 * each response.
 */

@Singleton
class Filters @Inject() (gzip: GzipFilter, log: LoggingFilter,corsFilter: CORSFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(gzip,log,corsFilter)
}
