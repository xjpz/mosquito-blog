# --- !Ups

CREATE TABLE `article` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(100) DEFAULT '' COMMENT '标题',
  `content` text COMMENT '内容',
  `catalog` text COMMENT '标签',
  `uid` int(1) NOT NULL COMMENT '用户id',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态',
  `type` tinyint(2) DEFAULT NULL COMMENT '类型',
  `read` int(10) DEFAULT NULL COMMENT '阅读次数',
  `smile` int(10) DEFAULT NULL COMMENT '点赞次数',
  `reply` int(10) DEFAULT NULL COMMENT '评论次数',
  `descrp` text COMMENT '评论',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0' COMMENT '假删除字段',
  PRIMARY KEY (`id`),
  KEY `uid` (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `link` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '链接id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `author` varchar(100) DEFAULT NULL COMMENT '密码',
  `content` varchar(100) NOT NULL COMMENT '密码',
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `author` (`author`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

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
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `reply2article` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `aid` int(10) NOT NULL COMMENT '话题id',
  `uid` int(10) NOT NULL COMMENT '评论用户id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `url` varchar(100) DEFAULT NULL COMMENT 'url',
  `email` varchar(100) DEFAULT NULL COMMENT 'email',
  `content` text NOT NULL COMMENT '评论内容',
  `quote` int(10) NOT NULL COMMENT '0：未引用 非0：引用楼层id',
  `smile` int(10) DEFAULT NULL,
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0' COMMENT '假删除字段',
  PRIMARY KEY (`id`),
  KEY `aid` (`aid`),
  KEY `uid` (`uid`),
  KEY `quote` (`quote`),
  KEY `email` (`email`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `reply2message` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `aid` int(10) NOT NULL COMMENT '话题id',
  `uid` int(10) NOT NULL COMMENT '评论用户id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `url` varchar(100) DEFAULT NULL COMMENT 'url',
  `email` varchar(100) DEFAULT NULL COMMENT 'email',
  `content` text NOT NULL COMMENT '评论内容',
  `quote` int(10) NOT NULL COMMENT '0：未引用 非0：引用楼层id',
  `smile` int(10) DEFAULT NULL,
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0' COMMENT '假删除字段',
  PRIMARY KEY (`id`),
  KEY `aid` (`aid`),
  KEY `uid` (`uid`),
  KEY `quote` (`quote`),
  KEY `email` (`email`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `ucustom` (
  `uid` int(10) unsigned NOT NULL,
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

CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `name` varchar(100) NOT NULL COMMENT '用户名',
  `password` char(50) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `phone` char(11) DEFAULT '' COMMENT '电话',
  `descrp` varchar(255) DEFAULT NULL COMMENT '描述',
  `type` tinyint(1) unsigned DEFAULT '0',
  `status` tinyint(1) DEFAULT '0',
  `qopenid` varchar(100) DEFAULT NULL,
  `qtoken` varchar(255) DEFAULT NULL,
  `sopenid` varchar(100) DEFAULT NULL,
  `stoken` varchar(255) DEFAULT NULL,
  `init_time` int(10) unsigned DEFAULT '0',
  `update_time` int(10) unsigned DEFAULT '0',
  `tombstone` tinyint(1) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `email` (`email`),
  KEY `phone` (`phone`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

# --- !Downs
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS ucustom;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS link;
DROP TABLE IF EXISTS news2mood;
DROP TABLE IF EXISTS reply2article;
DROP TABLE IF EXISTS reply2message;
