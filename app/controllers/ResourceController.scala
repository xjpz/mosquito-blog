package controllers

import java.io.File
import javax.inject.Inject
import play.Logger
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.Codecs
import play.api.libs.json.Json
import play.api.mvc._
import utils.ResultUtil.{OPERATE_FAIL, OPERATE_OK}
import utils.ResultUtil

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wenzh on 2016/6/3.
  */
class ResourceController @Inject()(configuration: Configuration)(cc: ControllerComponents) extends AbstractController(cc) {

  lazy val ABSOLUTE_PATH = """^(/|[a-zA-Z]:\\).*""".r
  lazy val FILE_SAVE_PATH = configuration.get[Option[String]]("file.resource").getOrElse("/data/blog/resource/") //File Save Path

  val pathForm = Form(
    mapping(
      "path" -> default(text, "blog")
    )(Tuple1.apply)(Tuple1.unapply)
  )

  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>

    val fileUrlDeCode = java.net.URLDecoder.decode(file, "utf-8")

    val fileToServe = rootPath.replace("/", "\\") match {
      case ABSOLUTE_PATH(_) => new File(rootPath, fileUrlDeCode)
      case _ => new File(FILE_SAVE_PATH, fileUrlDeCode)
    }

    if (fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true)
    } else {
      Logger.of(this.getClass).error("Photos controller failed to serve photo: " + fileUrlDeCode)
      NotFound
    }
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    val selectPath = pathForm.bindFromRequest().get
    request.body.file("fileDataFileName").map { file =>

      val filename = file.filename
      val fileType = filename.substring(filename.lastIndexOf("."))
      val fileNameFinal = s"${selectPath._1}/" + Codecs.sha1(file.ref.path.toString) + fileType
      val filePathFinal = FILE_SAVE_PATH + fileNameFinal
      file.ref.moveFileTo(new File(filePathFinal))

      ResultUtil.success(OPERATE_OK,"file_path",Json.toJson(s"http://${request.host}/blog/resource/$fileNameFinal"))
    }.getOrElse {
      ResultUtil.failure(OPERATE_FAIL)
    }
  }
}
