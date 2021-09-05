package utils

import java.text.SimpleDateFormat

/**
  * Created by xjpz on 2017/3/23.
  */
object DateUtil {

  def format(time: Long,pattern:String): String = {
    new SimpleDateFormat(pattern).format(time)
  }

  def format(time: Long): String = {
    format(time,"yyyy-MM-dd HH:mm:ss")
  }

  def format(time: Option[Long] = Some(System.currentTimeMillis())): String = {
    format(time.get)
  }

  def formatPostTime(time:Long):String = {
    val postYear = format(time,"yyyy")
    val nowYear = format(System.currentTimeMillis(),"yyyy")
    if(postYear.equals(nowYear)){
      format(time,"MM-dd HH:mm")
    }else{
      format(time,"yyyy-MM-dd HH:mm")
    }
  }

}
