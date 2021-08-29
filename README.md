# 项目

演示地址：[https://xjpz.cc](https://xjpz.cc "首页")

`mosquito-blog`是一个用`Scala`+`Play`搭建的博客系统。

前端是由`play`模板渲染(`html`模板也是借用play的，向[PlayFramework](https://www.playframework.com/ )致敬！)。

后端是用`play`写的一套完整的`RESTful APIs`。在设计数据结构与实现后端`APIs`时充分考虑了程序的可移植性、可扩展性，因此后端`APIs`可完全独立与其他前端技术搭配实现更友好的用户体验。

## 技术栈 ##

- `Scala` - version 2.12.8
- `Play` - version 2.7
- `Sbt` - version 1.2.8
- `MySql` - version 5.6.17
- `Slick` - version 3.2

## 功能实现 ##

- **User**
    - 注册
    - 登录
    - QQ授权登录
    - 修改资料*
    - 修改密码
    - 权限管理*
    - 操作记录*
    - 注销
- **Article**
	- 发帖
	- 浏览
	- 点赞
	- 评论
	- 修改
	- 标签管理
	- 权限管理*
- **Blog**
	-  标签云
	-  浏览排行
	-  文章归档
	
## TODO ##

- 博客后台
- 每日心情
- 导航栏


## 运行 ##

1. 必须安装有`Java8+`
2. 必须安装有`MySql`（数据库脚本在工程`conf\evolutions`内）
3. 开发模式下可直接热部署运行源码
    - 在工程根目录下执行 `sbt run`
4. 生产模式运行
    - 在工程根目录下执行`sbt dist`
    - 直接在`target\universal`下找到`mosquito-1.0.0.zip`
    - 解压缩
    - 在`bin`目录下运行`Linux/Windows`脚本
6. 在浏览器地址栏输入:[http://localhost:9000/](http://localhost:9000/ "mosquito-blog is running")

