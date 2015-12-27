# --- !Ups
SET NAMES utf8;
#用户表
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `password` char(50) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `phone` char(11) DEFAULT '' COMMENT '电话',
  `descrp` varchar(255) COMMENT '描述',
  `type` tinyint(2) unsigned DEFAULT '0' COMMENT '类型',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态',
	`qopenid` VARCHAR(100) COMMENT 'QqConnect OpenId' COMMENT 'QQ Openid',
	`qtoken` VARCHAR(255)  COMMENT 'QqConnect token' COMMENT 'QQ Token',
	`sopenid` VARCHAR(100) COMMENT 'sina WEIBO OpenId' COMMENT '微博 Openid',
	`stoken` VARCHAR(255) COMMENT 'Sina WEIBO token' COMMENT '微博 Token',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `email` (`email`),
  KEY `phone` (`phone`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

#博客表
CREATE TABLE `article` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(100) DEFAULT '' COMMENT '标题',
  `content` text COMMENT '内容',
  `catalog` VARCHAR(255) COMMENT '标签',
  `uid` int(10) NOT NULL COMMENT '用户id',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态',
  `type` tinyint(2) DEFAULT NULL COMMENT '类型',
  `read` int(10) DEFAULT NULL COMMENT '阅读次数',
  `smile` int(10) DEFAULT NULL COMMENT '点赞次数',
  `reply` int(10) DEFAULT NULL COMMENT '评论次数',
  `descrp` varchar(100) COMMENT '描述',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0' COMMENT '假删除字段',
  PRIMARY KEY (`id`),
  KEY `uid` (`uid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

#用户记录表
CREATE TABLE `urecord` (
  `uid` int(10) NOT NULL COMMENT '用户ID',
  `records` text COMMENT '记录详情Json',
  `update_time` int(10) unsigned NOT NULL DEFAULT '0',
  `init_time` int(10) unsigned NOT NULL DEFAULT '0',
  `tombstone` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#留言表
CREATE TABLE `reply2message` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `aid` int(10) NOT NULL COMMENT '留言id',
  `uid` int(10) NOT NULL COMMENT '留言用户id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `url` varchar(100)  COMMENT 'url' COMMENT '游客留言url',
  `email` varchar(100)  COMMENT 'email' COMMENT '游客留言email',
  `content` text NOT NULL COMMENT '评论内容',
  `quote` int(10) NOT NULL COMMENT '0：未引用 非0：引用楼层id',
  `smile` int(10) DEFAULT NULL COMMENT '点赞',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0' COMMENT '假删除字段',
  PRIMARY KEY (`id`),
  KEY `aid` (`aid`),
  KEY `uid` (`uid`),
  KEY `quote` (`quote`),
  KEY `email` (`email`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

#文章评论表
CREATE TABLE `reply2article` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `aid` int(10) NOT NULL COMMENT '文章id',
  `uid` int(10) NOT NULL COMMENT '评论用户id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `url` varchar(100)  COMMENT 'url' COMMENT '游客url',
  `email` varchar(100)  COMMENT 'email' COMMENT '游客email',
  `content` text NOT NULL COMMENT '评论内容',
  `quote` int(10) NOT NULL COMMENT '0：未引用 非0：引用楼层id',
  `smile` int(10) DEFAULT NULL COMMENT '点赞',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0' COMMENT '假删除字段',
  PRIMARY KEY (`id`),
  KEY `aid` (`aid`),
  KEY `uid` (`uid`),
  KEY `quote` (`quote`),
  KEY `email` (`email`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

#每日心情表
CREATE TABLE `news2mood` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL COMMENT '内容',
  `uid` int(10) NOT NULL COMMENT '用户id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0' COMMENT '假删除字段',
  PRIMARY KEY (`id`),
  KEY `uid` (`uid`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

#友情链接
CREATE TABLE `link` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '链接id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `author` varchar(100) COMMENT '作者',
  `content` varchar(100)  COMMENT '链接地址',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `author` (`author`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

#用户信息表
CREATE TABLE `userinfo` (
  `uid` int(11) unsigned NOT NULL,
	`rname` varchar(100)  COMMENT '真实姓名',
	`descrp` text COMMENT '描述',
	`gender` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '性别',
	`birthday` varchar(100) COMMENT '出生日期',
	`area` varchar(100)  COMMENT '地区',
  `reg_ip` char(15) DEFAULT '' COMMENT '注册ip',
	`last_ip` char(15) DEFAULT '' COMMENT '最后一次登录ip',
  `last_time` int(10) DEFAULT '0' COMMENT '最后一次登录时间',
	`credits` int(10) unsigned DEFAULT NULL COMMENT '积分',
  `level` int(10) unsigned DEFAULT NULL COMMENT '等级',
	`honor` varchar(100) COMMENT '荣誉',
	`photo` varchar(100) COMMENT '头像',
  `init_time` int(10) unsigned NOT NULL DEFAULT '0',
  `update_time` int(10) unsigned NOT NULL DEFAULT '0',
  `tombstone` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

#自定义样式表
CREATE TABLE `ucustom` (
  `uid` int(11) unsigned NOT NULL,
  `descrp` text COMMENT '描述',
  `top` text COMMENT '头部与页眉',
  `right` text COMMENT '右侧',
  `left` text COMMENT '左侧',
  `bottom` text COMMENT '底部与页脚',
  `style` text COMMENT '样式',
  `javascript` text COMMENT 'js脚本',
  `init_time` int(10) unsigned NOT NULL DEFAULT '0',
  `update_time` int(10) unsigned NOT NULL DEFAULT '0',
  `tombstone` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

# --- !Downs
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS urecord;
DROP TABLE IF EXISTS reply;
