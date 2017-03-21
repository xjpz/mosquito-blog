package services

import play.api.Application

/**
  * Created by wenzh on 2016/6/24.
  */

object ViewAccessPoint {
  private val myDaoCache = Application.instanceCache[MyDao]

  object Implicits {
    implicit def myDao(implicit application: Application): MyDao = myDaoCache(application)
  }

}
