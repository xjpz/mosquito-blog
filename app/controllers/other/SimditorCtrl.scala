package controllers

import play.api.mvc._

object SimditorCtrl extends Controller{

	def toEdit = Action {
		Ok(views.html.simditor2Html())
	}

	def text2Html = Action {request =>
		val reqFormDate = request.body.asFormUrlEncoded.get
		val textListBuffer = reqFormDate.get("textStr")

		val htmlStr = if(textListBuffer.isDefined && textListBuffer.get.nonEmpty){val textStr = textListBuffer.get.head ;textStr} else {""}

		Ok(htmlStr).as(HTML)
	}
}
