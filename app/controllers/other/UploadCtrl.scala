package controllers.other

import java.io.File

import play.api.Play.current
import play.api._
import play.api.libs.json._
import play.api.mvc._
import utils.MD5

import scala.collection.mutable.ListBuffer

object UploadCtrl extends Controller{
	val fileNameListBuffer = new ListBuffer[String]

	def upload = Action(parse.multipartFormData) { request =>
		request.body.file("fileDataFileName").map { file =>

			val filename = file.filename
			val fileType = filename.substring(filename.lastIndexOf("."))
			val fileNameFinal = MD5.hash(file.ref.file.toString)+fileType
			file.ref.moveTo(new File("/data/resource/"+fileNameFinal))

//			file.ref.moveTo(new File("D:\\mosquito-project\\github\\upload\\"+fileNameFinal))

			Ok(Json.obj("success"->"true","file_path" -> s"http://${request.host}/resource/$fileNameFinal"))
		}.getOrElse {
			Ok(Json.obj("success"->"false","message" ->"fail","file_path" -> ""))
		}
	}

//	def toDownload = Action{
//		val path = "res/"
//		getName(path,fileNameListBuffer)
//		Ok(views.html.download(fileNameListBuffer.toList))
//	}
//
//	def download(file_path:String) = Action {
//		Ok.sendFile(new java.io.File(s"res/$file_path"))
//	}
//
//	def getName(path:String,fileNameListBuffer:ListBuffer[String]): Unit  =  {
//
//		val file = Play.getFile(path)
//		if(file.isDirectory){
//			val files = file.listFiles()
//			if(files.nonEmpty){
//				for(f <- files){
//					if(f.isDirectory){
//						getName(f.getAbsolutePath,fileNameListBuffer)
//					}else{
//						fileNameListBuffer += f.getName
//					}
//				}
//			}
//		}
//	}

}
