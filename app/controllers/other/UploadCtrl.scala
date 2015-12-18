package controllers

import java.io.File

import play.api.Play.current
import play.api._
import play.api.libs.json._
import play.api.mvc._

import scala.collection.mutable.ListBuffer

object UploadCtrl extends Controller{
	val fileNameListBuffer = new ListBuffer[String]

	def toUpload = Action{
		Ok(views.html.upload())
	}

	def upload = Action(parse.multipartFormData) { request =>
		request.body.file("fileDataFileName").map { picture =>

			val filename = picture.filename
			val contentType = picture.contentType
			println(Play.current.path)
			picture.ref.moveTo(new File(Play.current.path+s"/res/$filename"))
			//此处是存储在项目根目录下的public目录下,不能以/(如:/public)开头
			Ok(Json.obj("success"->"true","file_path" -> s"http://xjpz.me/resource/res/$filename"))
		}.getOrElse {
			Ok(Json.obj("success"->"false","message" ->"fail","file_path" -> ""))
		}
	}

	def toDownload = Action{
		val path = "res/"
		getName(path,fileNameListBuffer)
		Ok(views.html.download(fileNameListBuffer.toList))
	}

	def download(file_path:String) = Action {
		Ok.sendFile(new java.io.File(s"res/$file_path"))
	}

	def getName(path:String,fileNameListBuffer:ListBuffer[String]): Unit  =  {

		val file = Play.getFile(path)
		if(file.isDirectory){
			val files = file.listFiles()
			if(files.nonEmpty){
				for(f <- files){
					if(f.isDirectory){
						getName(f.getAbsolutePath,fileNameListBuffer)
					}else{
						fileNameListBuffer += f.getName
					}
				}
			}
		}
	}

}
