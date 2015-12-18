package utils

object MD5 {

  /**
   * *
   * @param s 待加密字符串
   * @return 加密结果
   */
  def hash(s: String): String = {
    val m = java.security.MessageDigest.getInstance("MD5")
    val b = s.getBytes("UTF-8")
    m.update(b, 0, b.length)
    new java.math.BigInteger(1, m.digest()).toString(16)
  }

}
