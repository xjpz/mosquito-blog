package controllers

import play.api.mvc.{Action, Controller}

/**
  * Created by wenzh on 2016/1/14.
  */

object TestCtrl extends Controller {

    def test() = Action { request =>
        import play.filters.csrf.CSRF

        val token = CSRF.getToken(request)
        println(request)
        println(token)
        Ok("Success")
    }

    def toTest = Action{ request =>
        Ok(views.html.test.render(request))
    }
    
}
