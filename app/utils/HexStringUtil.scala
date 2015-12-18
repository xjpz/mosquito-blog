package utils

/**
 * Created by Administrator on 2015/4/23.
 */
object HexStringUtil {

    // convert normal string to hex bytes string
    def string2hex(strOpt: Option[String]): Option[String] = {
        var retOpt = None: Option[String]
        if (strOpt.isDefined) {
            val str = strOpt.get
            retOpt = Option(str.toList.map(_.toInt.toHexString).mkString)
        }
        retOpt
    }

    // convert hex bytes string to normal string
    def hex2string(hexOpt: Option[String]): Option[String] = {
        var retOpt = None: Option[String]
        if (hexOpt.isDefined) {
            val hex = hexOpt.get
            retOpt = Option(hex.trim.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toChar).mkString)
        }
        retOpt
    }

}
