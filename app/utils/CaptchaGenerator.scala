package utils

import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.{Color, Font, RenderingHints}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.util.Random
import javax.imageio.ImageIO

/**
 * @author xring
 * @since 2015-08-23 6:21 PM
 * @version 1.0
 */
object CaptchaGenerator {

	// 验证码组成元素
	lazy val SOURCE = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ"

	// 验证码组成元素长度
	lazy val sourceLength = SOURCE.length

	lazy val random = new Random(System.currentTimeMillis)

	//验证码图片宽度
	lazy val width = 80

	//验证码图片宽度
	lazy val height = 30


	def getImage() = {
		val captchaText = generateCaptchaText(4)
		val image = generateCaptchaImage(width, height, captchaText)
		val os = new ByteArrayOutputStream()
		ImageIO.write(image, "gif", os)
		new ByteArrayInputStream(os.toByteArray)
	}

	def getCpatcha: (String, InputStream) = {
		val captchaText = generateCaptchaText(4)
		val image = generateCaptchaImage(width, height, captchaText)
		val os = new ByteArrayOutputStream()
		ImageIO.write(image, "gif", os)
		val captchaValue = new ByteArrayInputStream(os.toByteArray)
		(captchaText, captchaValue)
	}

	//	/**
	//	 * 扭曲图片
	//	 * @param g 图片
	//	 * @param width 宽度
	//	 * @param height 高度
	//	 * @param color 颜色
	//	 */
	//	def warp(g: Graphics, width: Int, height: Int, color: Color): Unit = {
	//		warpX(g, width, height, color)
	//		warpY(g, width, height, color)
	//	}

	//	/**
	//	 * 扭曲图片X方向（竖向）
	//	 * @param g 图片
	//	 * @param width 宽度
	//	 * @param height 高度
	//	 * @param color 颜色
	//	 */
	//	def warpX(g: Graphics, width: Int, height: Int, color: Color): Unit = {
	//		val period = random.nextInt(2)
	//		val borderGap = true
	//		val frames = 1
	//		val phase = random.nextInt(2)
	//		for (i <- 0 until width) {
	//			val d = (period >> 1) * Math.sin(i.toDouble / period + (6.2831853071795862D * phase) / frames)
	//			g.copyArea(0, i, width, 1, d.toInt, 0)
	//			if (borderGap) {
	//				g.setColor(color)
	//				g.drawLine(d.toInt, i, 0, i)
	//				g.drawLine(d.toInt + width, i, width, i)
	//			}
	//		}
	//	}
	//
	//	/**
	//	 * 扭曲图片Y方向（横向）
	//	 * @param g 图片
	//	 * @param width 宽度
	//	 * @param height 高度
	//	 * @param color 颜色
	//	 */
	//	def warpY(g: Graphics, width: Int, height: Int, color: Color): Unit = {
	//		val period = random.nextInt(40) + 10
	//		val borderGap = true
	//		val frames = 20
	//		val phase = 7
	//		for (i <- 0 until height) {
	//			val d = (period >> 1) * Math.sin(i.toDouble / period + (6.2831853071795862D * phase) / frames)
	//			g.copyArea(i, 0, 1, height, 0, d.toInt)
	//			if (borderGap) {
	//				g.setColor(color)
	//				g.drawLine(i, d.toInt, i, 0)
	//				g.drawLine(i, d.toInt + height, i, height)
	//			}
	//		}
	//	}

	/**
	 * 生成随机验证码
	 * @param captchaSize 验证码长度
	 * @return 长度为captchaSize的随机字符串
	 */
	def generateCaptchaText(captchaSize: Int): String = {
		(1 to captchaSize).map(_ => SOURCE.charAt(random.nextInt(sourceLength - 1))).mkString("")
	}

	/**
	 * 获取随机颜色
	 * @param foregroundColor 前景色
	 * @param backgroundColor 背景色
	 * @return 随机颜色
	 */
	def getRandomColor(foregroundColor: Int, backgroundColor: Int): Color = {
		val r: Int = foregroundColor + random.nextInt(backgroundColor - foregroundColor)
		val g: Int = foregroundColor + random.nextInt(backgroundColor - foregroundColor)
		val b: Int = foregroundColor + random.nextInt(backgroundColor - foregroundColor)
		new Color(r, g, b)
	}

	/**
	 * 获取随机Int颜色
	 * @return 随机Int颜色
	 */
	def getRandomIntColor: Int = {
		val rgb = getRandomRGB
		var color = 0
		for (single <- rgb) {
			color = color << 8
			color = color | single
		}
		color
	}

	/**
	 * 获取随机RGB颜色
	 * @return 随机RGB颜色
	 */
	def getRandomRGB: Seq[Int] = {
		(1 to 3).map(_ => random.nextInt(255))
	}

	def generateCaptchaImage(w: Int, h: Int, captchaText: String) = {
		val verifySize = captchaText.length
		val image: BufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
		val g2 = image.createGraphics
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

		// 设置边框色
		//		g2.setColor(Color.GRAY)
		//		g2.fillRect(0, 0, w, h)

		// 设置背景色
		val c = getRandomColor(200, 250)
		g2.setColor(c)
		g2.fillRect(0, 0, w, h)

		//绘制干扰线
		val random: Random = new Random
		g2.setColor(getRandomColor(160, 200))
		for (i <- 0 until 20) {
			val x = random.nextInt(w - 1)
			val y = random.nextInt(h - 1)
			val xl = random.nextInt(6) + 1
			val yl = random.nextInt(12) + 1
			g2.drawLine(x, y, x + xl + 40, y + yl + 20)
		}

		//		 添加噪点
		val yawpRate = 0.05f // 噪声率
		val area = (yawpRate * w * h).toInt
		for (i <- 0 until area) {
			val x = random.nextInt(w)
			val y = random.nextInt(h)
			val rgb = getRandomIntColor
			image.setRGB(x, y, rgb)
		}

		// 使图片扭曲
		//		warp(g2, w, h, c)

		g2.setColor(getRandomColor(100, 160))
		val fontSize = h - 2
		val font = new Font("Algerian", Font.ITALIC, fontSize)
		g2.setFont(font)
		val chars = captchaText.toCharArray
		for (i <- 0 until verifySize) {
			val affine = new AffineTransform
			affine.setToRotation(Math.PI / 4 * random.nextDouble * (if (random.nextBoolean) 1 else -1), (w / verifySize) * i + fontSize / 2, h / 2)
			g2.setTransform(affine)
			g2.drawChars(chars, i, 1, ((w - 10) / verifySize) * i + 5, h / 2 + fontSize / 2 - 10)
		}

		g2.dispose()
		image
	}

}
