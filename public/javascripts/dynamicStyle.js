; (function ($, document, undefined) {

	/*
	* 队列进行简单的封装
	*/
	$.fn.will = function (callback, type) {
		//这里的this，表示jQuery对象
		this.queue(type || "fx", function (next) {  // fx 表示默认的队列
			//这里的this，是原生的对象
			callback && typeof callback == "function" && callback.call($(this)); //使用call，方便回调函数使用this
			next();
		});
		return this; //返回this，方便进行链式调用
	};

	/*
	* 字符串格式化扩展
	*/
	String.prototype.format = function () {
		var args = arguments;
		var reg = /\{(\d+)\}/g;
		return this.replace(reg, function (g0, g1) {
			return args[+g1];
		});
	};


	/*
	* 插件开始..
	*/
	$.fn.dynamicStyle = function (options) {
		var t = $.type(options);
		if (t === "object") {  //使用object初始化
			var defaults = {
				delay: 200,   // 时间间隔
				content: "",
				queueName: "dynamic",  // 所用队列名称，不占用动画队列 “fx”			
				//默认文本所用class的名称
				classLine: "dynamicStyle-line", // 每一行样式所用的class
				className: "dynamicStyle-class-name",  // 样式-class 的名称的class
				classKey: "dynamicStyle-class-key",    // 样式-class-样式名的class
				classValue: "dynamicStyle-class-value", // 样式-class-样式值的class
				notesContent: "dynamicStyle-notes",     // 样式-注释 的class
				//默认文本所用class的名称 end
				contentNotify: function () { }, //每添加一次节点文字的回调,一般用于调整scroll
				notify: function () { },  //添加完一个模块的通知回调
				complete: function () { }  //完成回调
			};
			return this.each(function () {
				var opt = $.extend({}, defaults, options, { wrap: $(this) });
				$(this).data("dynamicStyle", new DynamicStyle(opt));
			});
		}
		else if (t === "string") { //使用string来控制
			return this.each(function () {
				var obj = $(this).data("dynamicStyle");
				var events = {  // 目前没有想到有什么方法供调用
				};
				events[options] && events[options].call(obj);
			});
		}
		else console.log("args error!")
	}


	function DynamicStyle(opt) {
		this.opt = opt;
		this.inIt();
	};

	DynamicStyle.prototype = {
		inIt: function () {
			this.loadStyle(this.opt.content);
			this.start();
		},
		infos: [],
		loadStyle: function (content) {  //加载css文件
			this.infos = splitStyles(content);
		},
		start: function () {
			var self = this;
			$.each(self.infos, function (index, item) {
				//把添加项的操作放入队列
				var writeFunc = item.type == 1 ? writeNotes : writeStyles;
				writeFunc(item, self.opt);
			
				// 添加完一项之后执行回调
				self.opt.wrap.will(function () {
					self.opt.notify(item);
				}, self.opt.queueName);
			});
			//结束的回调
			self.opt.wrap.will(function () {
				self.opt.complete();
			}, self.opt.queueName);
			//执行队列
			self.opt.wrap.dequeue(self.opt.queueName);
		},
		stop: function () {
			this.opt.wrap.stop(true, false);
		},
		notify: function () {
			this.opt.notify.apply(this, arguments);
		},
		complete: function () {
			this.opt.complete.apply(this, arguments);
		}
	};


	/*
	* 将样式文件文本，用正则抽离分隔出样式和注释
	*/
	function splitStyles(content) {               //从文字获取注释和样式
		var styleReg = /\/\*([\s\S]*?)\*\/|([^\/]+?)\{([\s\S]+?)\}/g, //获取样式和注释的正则
			arr = [],
			m = styleReg.exec(content);
		for (; m; m = styleReg.exec(content)) {
			arr.push(m);
		}
		return arr.map(function (item, index) {   // ......渣代码开始 0.0
			return item[1] ? {
				type: 1,  // type 是保留字？ 但是我真的很想用它... 类型、种类，就该是type
				content: item[0],  //原文字
				lines: $.trim(item[1]).split(/\s*\n\s*|\\n/g)
			} : (function () {
				var obj = {};
				$.trim(item[3]).split(/\s*;\s*/g).forEach(function (line) {   // 将 KeyValuePair 填充到 obj 
					var pair = line.split(":");
					var k = $.trim(pair[0]), v = $.trim(pair[1]);
					k && v && (function () { obj[k] = v; })();
				});
				return {
					type: 2,
					content: item[0],  //原文字
					names: $.trim(item[2]).split(/\s*,\s*/g),
					styles: obj
				};
			})();
		});
	}

	/*
	* 动态写入效果，content-内容，delay-延时，wrap-作为队列的统一拥有者，ele-追加的元素 queueName-队列名称
	*/
	function dynamicWrite(content, opt, ele) {
		var delay = opt.delay,
			wrap = opt.wrap,
			queueName = opt.queueName,
			callback = opt.contentNotify;
		for (var i = 0, len = content.length; i < len; i++) {
			(function (word) {
				wrap.will(function () {
					ele.html(ele.html() + word);
				}, queueName).delay(delay, queueName);
			})(content[i]);
			!!i && wrap.will(function () {
				callback(content);
			}, queueName);
		}
	}

	/*
	* 书写样式, wrap-容器，lines-内容，opt-配置
	*/
	function writeNotes(notesObj, opt, callback) {
		var lines = notesObj.lines;
		dynamicWrite("/*",                                                             //写入  /*
			opt,
			$('<span class="{0}"></span>'.format(opt.notesContent)).appendTo(
				$('<div class="{0}"></div>'.format(opt.classLine)).appendTo(opt.wrap))
			);
		$.each(lines, function (index, line) {                                                //写入注释正文内容
			dynamicWrite("* " + line,
				opt,
				$('<span class="{0}"></span>'.format(opt.notesContent)).appendTo(
					$('<div class="{0}"></div>'.format(opt.classLine)).appendTo(opt.wrap))
				);
			//注释换行延时
			opt.wrap.delay(opt.delay * 10, opt.queueName);
		});
		dynamicWrite("*/",                                                             //写入  */
			opt,
			$('<span class="{0}"></span>'.format(opt.notesContent)).appendTo(
				$('<div class="{0}"></div>'.format(opt.classLine)).appendTo(opt.wrap))
			);
	}


	function writeStyles(stylesObj, opt) {
		var names = stylesObj.names;
		//写入类名称
		$.each(names, function (index, name) {
			var ifLast = index + 1 >= names.length;  //是否最后一个类名
			name += ifLast ? "" : ",";
			var lineWrap = $('<div class="{0}"></div>'.format(opt.classLine)).appendTo(opt.wrap);
			dynamicWrite(name,           //写入类名
				opt,
				$('<span class="{0}"></span>'.format(opt.className)).appendTo(lineWrap));
			ifLast && dynamicWrite("{",  // 如果最后一个类名，追加 {
				opt,
				$('<span class="{0}"></span>'.format(opt.classValue)).appendTo(lineWrap)
				);
		});
		//写入样式的 KeyValuePair
		var pairLen = Object.getOwnPropertyNames(stylesObj.styles).length,
			pairNow = 1;   //当前第几个

		$.each(stylesObj.styles, function (k, v) {
			v = ":" + v + (pairNow >= pairLen ? "" : ";");  // 对值进行处理
			var lineWrap = $('<div class="{0}"></div>'.format(opt.classLine)).appendTo(opt.wrap); //行
		
			//添加 key
			dynamicWrite(
				k,
				opt,
				$('<span class="{0}"></span>'.format(opt.classKey)).appendTo(lineWrap)
				);
			//添加 value
			dynamicWrite(
				v,
				opt,
				$('<span class="{0}"></span>'.format(opt.classValue)).appendTo(lineWrap)
				);

			pairNow++;
		});
		//写入 }
		dynamicWrite(
			"}",
			opt,
			$('<span class="{0}"></span>'.format(opt.classValue)).appendTo(
				$('<div class="{0}"></div>'.format(opt.classLine)).appendTo(opt.wrap)
				)
			);
	}
})(jQuery, document, undefined);