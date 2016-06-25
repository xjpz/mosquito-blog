package controllers

import java.io.File
import javax.inject.Inject

import play.Logger
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, Controller}

/**
  * Created by wenzh on 2016/6/3.
  */
class ResourceController @Inject()(configuration:Configuration) extends Controller{
  val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r
  lazy val FILESAVEPATH = configuration.getString("file.resource").getOrElse("/data/blog/resource/") //File Save Path

  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>

    val fileUrlDeCode = java.net.URLDecoder.decode(file,"utf-8")

    val fileToServe = rootPath.replace("/","\\") match {
      case AbsolutePath(_) => new File(rootPath, fileUrlDeCode)
      //			case _ => new File(Play.application.getFile(rootPath), fileUrlDeCode)
      case _ => new File(FILESAVEPATH , fileUrlDeCode)
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
