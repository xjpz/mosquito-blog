package controllers

import java.io.File

import play.api.mvc.{Action, AnyContent, Controller}
import play.{Logger, Play}

object ResourceCtrl extends Controller{

	val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

	def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>

		val fileUrlDeCode = java.net.URLDecoder.decode(file,"utf-8")

		val fileToServe = rootPath.replace("/","\\") match {
			case AbsolutePath(_) => new File(rootPath, fileUrlDeCode)
//			case _ => new File(Play.application.getFile(rootPath), fileUrlDeCode)
			case _ => new File("/data/resource/" , fileUrlDeCode)
//			case _ => new File("D:\\mosquito-project\\github\\upload\\" , fileUrlDeCode)
		}

		if (fileToServe.exists) {
			Ok.sendFile(fileToServe, inline = true)
		} else {
			Logger.error("Photos controller failed to serve photo: " + fileUrlDeCode)
			NotFound
		}
	}
}
