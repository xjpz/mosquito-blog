package controllers

import java.io.File
import javax.inject.Inject

import play.Logger
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.Codecs
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{Action, AnyContent, Controller}

/**
  * Created by wenzh on 2016/6/3.
  */
class ResourceController @Inject()(configuration:Configuration) extends Controller{

  lazy val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r
  lazy val FILESAVEPATH = configuration.getString("file.resource").getOrElse("/data/blog/resource/") //File Save Path

  val pathForm = Form(
    mapping(
      "path" -> default(text,"blog")
    )(Tuple1.apply)(Tuple1.unapply)
  )

  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>

    val fileUrlDeCode = java.net.URLDecoder.decode(file,"utf-8")

    val fileToServe = rootPath.replace("/","\\") match {
      case AbsolutePath(_) => new File(rootPath, fileUrlDeCode)
      case _ => new File(FILESAVEPATH , fileUrlDeCode)
    }

    if (fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true)
    } else {
      Logger.error("Photos controller failed to serve photo: " + fileUrlDeCode)
      NotFound
    }
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    val selectPath = pathForm.bindFromRequest().get
    request.body.file("fileDataFileName").map { file =>

      val filename = file.filename
      val fileType = filename.substring(filename.lastIndexOf("."))
      val fileNameFinal = s"${selectPath._1}/" + Codecs.sha1(file.ref.file.toString)+fileType
      val filaPathFinal = FILESAVEPATH + fileNameFinal
      file.ref.moveTo(new File(filaPathFinal))
      Ok(Json.obj("success"->"true","file_path" -> s"http://${request.host}/blog/resource/$fileNameFinal"))
    }.getOrElse {
      Ok(Json.obj("success"->"false","message" ->"fail","file_path" -> JsNull))
    }
  }
}
