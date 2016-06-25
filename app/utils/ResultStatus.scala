package utils

/**
  * Created by xjpz on 2016/5/28.
  */

object ResultStatus extends Enumeration {

  val status_0 = Value("UnKnow Error/Data NotFound")
  val status_1 = Value("Success")
  val status_2 = Value("InternalServerError")
  val status_3 = Value("UnAuthorized")
  val status_4 = Value("NotFound")
  val status_5 = Value("BadRequest")
  val status_6 = Value("Verification Code Error")
  val status_7 = Value("Verification code Invalid")
  val status_8 = Value("Name or Password Error")
  val status_9 = Value("User Does Not Exist")
  val status_10 = Value("User Already Exist")
  val status_11 = Value("Name Already Exist")
  val status_12 = Value("Email Already Exist")

  //...
}
