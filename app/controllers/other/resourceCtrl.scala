package controllers

import java.io.File

import play.api.mvc.{Action, AnyContent, Controller}
import play.{Logger, Play}

object resourceCtrl extends Controller{

	val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

	def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>

		val fileUrlDeCode = java.net.URLDecoder.decode(file,"utf-8");   //中文需要URLDecoder

		val fileToServe = rootPath.replace("/","\\") match {              //不知为何，传入的path与系统获取的不一样
			case AbsolutePath(_) => new File(rootPath, fileUrlDeCode)
			case _ => new File(Play.application.getFile(rootPath), fileUrlDeCode)
		}

		if (fileToServe.exists) {
			Ok.sendFile(fileToServe, inline = true)
		} else {
			Logger.error("Photos controller failed to serve photo: " + fileUrlDeCode)
			NotFound
		}
	}
}
