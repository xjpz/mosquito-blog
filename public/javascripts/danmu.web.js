/*------------首页---------------- */


var texts1 = {
    1: [{ "text": "如何和土壕做朋友？", "color": "#666", "size": "1", "position": "0" }],
    3: [{ "text": "家里的猫抑郁了", "color": "#666", "size": "1", "position": "0" }
    ],
    4: [{ "text": "失恋了，茶饭不思", "color": "#666", "size": "1", "position": "0" }
    ],
    6: [{ "text": "未来一周的天气怎么样", "color": "#666", "size": "1", "position": "0" }
    ],
    40: [{ "text": "陈妍希，我宣你，可是我不知道怎么告诉你", "color": "#666", "size": "1", "position": "0" }
    ],
    50: [{ "text": "我要听笑话", "color": "#666", "size": "1", "position": "0" }
    ],
    60: [{ "text": "寂寞空虚寒冷无助~~", "color": "#666", "size": "1", "position": "0" }
    ],
    70: [{ "text": "和女朋友吵架怎么办?", "color": "#666", "size": "1", "position": "0" }
    ],
    80: [{ "text": "想找本校同学一起自习", "color": "#666", "size": "1", "position": "0" }
    ],
    90: [{ "text": "想蹭课，其他大学课表找不着", "color": "#666", "size": "1", "position": "0" }
    ],
    100: [{ "text": "如何判断自己喜欢的男生是不是直的", "color": "#666", "size": "1", "position": "0" }
    ],
    110: [{ "text": "在北京买不起房咋整啊？", "color": "#666", "size": "1", "position": "0" }
    ],
    120: [{ "text": "看上男神不知道怎么追", "color": "#666", "size": "1", "position": "0" }
    ],
    130: [{ "text": "亲人生病了在医院，想找人陪护", "color": "#666", "size": "1", "position": "0" }
    ],
    140: [{ "text": "饿了么", "color": "#666", "size": "1", "position": "0" }
    ],
    150: [{ "text": "便宜又营养地定餐", "color": "#666", "size": "1", "position": "0" }
    ],
    160: [{ "text": "一个人生病在医院想有个人照顾", "color": "#666", "size": "1", "position": "0" }
    ],
    170: [{ "text": "渴了吗", "color": "#666", "size": "1", "position": "0" }
    ],
    180: [{ "text": "想看话剧买不到理想价位", "color": "#666", "size": "1", "position": "0" }
    ],
    190: [{ "text": "一个人在西班牙旅游想找人拼桌吃美食", "color": "#666", "size": "1", "position": "0" }
    ],
    200: [{ "text": "问了好多人应该换哪个女朋友", "color": "#666", "size": "1", "position": "0" }
    ],
    210: [{ "text": "想听演唱会买不到票", "color": "#666", "size": "1", "position": "0" }
    ],
    220: [{ "text": "我想去看NBA", "color": "#666", "size": "1", "position": "0" }
    ],
    230: [{ "text": "面试不知道怎么准备", "color": "#666", "size": "1", "position": "0" }
    ],
    240: [{ "text": "想找人帮我写实验报告", "color": "#666", "size": "1", "position": "0" }
    ],
    250: [{ "text": "想知道去年这门课考了什么", "color": "#666", "size": "1", "position": "0" }
    ],
    260: [{ "text": "怎么买到特价机票呢？", "color": "#666", "size": "1", "position": "0" }
    ],
    270: [{ "text": "健身哪家强", "color": "#666", "size": "1", "position": "0" }
    ],
    280: [{ "text": "难过时，想找人倾诉想找个树洞", "color": "#666", "size": "1", "position": "0" }
    ],
    290: [{ "text": "豆瓣同城活动推荐", "color": "#666", "size": "1", "position": "0" }
    ],
    300: [{ "text": "明天早上订了闹钟，怕起不来怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    310: [{ "text": "如何在帝都这样的大城市省钱过好日子", "color": "#666", "size": "1", "position": "0" }
    ],
    320: [{ "text": "十一假期去哪里玩？", "color": "#666", "size": "1", "position": "0" }
    ],
    330: [{ "text": "鼻子一直不透气怎么办？", "color": "#666", "size": "1", "position": "0" }
    ],
    340: [{ "text": "天天看电脑怎么保护眼睛？", "color": "#666", "size": "1", "position": "0" }
    ],
    350: [{ "text": "机票比价", "color": "#666", "size": "1", "position": "0" }
    ],
    360: [{ "text": "帮忙抢回家的火车票啊！太懒太忙！", "color": "#666", "size": "1", "position": "0" }
    ],
    370: [{ "text": "淘宝上买东西被骗了怎么办？", "color": "#666", "size": "1", "position": "0" }
    ],
    380: [{ "text": "明天考试好紧张。。", "color": "#666", "size": "1", "position": "0" }
    ],
    390: [{ "text": "挣钱了给父母买点儿什么呢", "color": "#666", "size": "1", "position": "0" }
    ],
    400: [{ "text": "我要出任CEO，迎娶白富美，走向人生巅峰！", "color": "#666", "size": "1", "position": "0" }
    ],
    410: [{ "text": "怎样才能参加球星中国行", "color": "#666", "size": "1", "position": "0" }
    ],
    420: [{ "text": "出去旅游买不到机票怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    430: [{ "text": "过敏难受怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    440: [{ "text": "去听歌剧但是欣赏无能怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    450: [{ "text": "家里的电视坏了怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    460: [{ "text": "去高大上的西餐厅b格不够怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    470: [{ "text": "作业好难怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    480: [{ "text": "想蹭宣讲会／讲座／活动，活动安排找不着", "color": "#666", "size": "1", "position": "0" }
    ],
    490: [{ "text": "出门不想挤公交怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    500: [{ "text": "想拍毕业照", "color": "#666", "size": "1", "position": "0" }
    ],
    510: [{ "text": "搞不定老板布置的任务", "color": "#666", "size": "1", "position": "0" }
    ],
    520: [{ "text": "想换老板了怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    530: [{ "text": "订不到周杰伦的演唱会门票", "color": "#666", "size": "1", "position": "0" }
    ],
    540: [{ "text": "找不到租房子的地方怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    550: [{ "text": "搞不定家里的熊孩子", "color": "#666", "size": "1", "position": "0" }
    ],
    560: [{ "text": "任性", "color": "#666", "size": "1", "position": "0" }
    ],
    570: [{ "text": "七夕没人陪", "color": "#666", "size": "1", "position": "0" }
    ],
    580: [{ "text": "大姨妈拜访洗不了衣服", "color": "#666", "size": "1", "position": "0" }
    ],
    590: [{ "text": "蓄谋表白送礼", "color": "#666", "size": "1", "position": "0" }
    ],
    600: [{ "text": "求调戏求抱抱的", "color": "#666", "size": "1", "position": "0" }
    ],
    610: [{ "text": "找男友、找女友、找基友、找闺蜜", "color": "#666", "size": "1", "position": "0" }
    ],
    620: [{ "text": "不想out", "color": "#666", "size": "1", "position": "0" }
    ],
    630: [{ "text": "想约吴彦祖", "color": "#666", "size": "1", "position": "0" }
    ],
    640: [{ "text": "想陪逛街", "color": "#666", "size": "1", "position": "0" }
    ],
    650: [{ "text": "想每天给女友送惊喜", "color": "#666", "size": "1", "position": "0" }
    ],
    660: [{ "text": "请假的理由已经用完了", "color": "#666", "size": "1", "position": "0" }
    ],
    670: [{ "text": "想减肥又不想停止吃", "color": "#666", "size": "1", "position": "0" }
    ],
    680: [{ "text": "想让白日梦实现", "color": "#666", "size": "1", "position": "0" }
    ],
    690: [{ "text": "昨天逛街看上了一个帅哥，感觉他和我对视了两个密西西比，不过现在找不到他了", "color": "#666", "size": "1", "position": "0" }
    ],
    700: [{ "text": "问了好多人应该换哪个女朋友", "color": "#666", "size": "1", "position": "0" }
    ],
    710: [{ "text": "想找人代替去上班", "color": "#666", "size": "1", "position": "0" }
    ],
    720: [{ "text": "女朋友天天嚷嚷过节，送礼物已经疲软，实在不知道送什么好", "color": "#666", "size": "1", "position": "0" }
    ],
    730: [{ "text": "朋友聚会，老公想让不会下厨房的我做一桌子美食招呼大家，我接近崩溃边缘", "color": "#666", "size": "1", "position": "0" }
    ],
    740: [{ "text": "给儿子取名字", "color": "#666", "size": "1", "position": "0" }
    ],
    750: [{ "text": "单身汪族求女友撑场面", "color": "#666", "size": "1", "position": "0" }
    ],
    760: [{ "text": "请客不知道去哪吃饭", "color": "#666", "size": "1", "position": "0" }
    ],
    770: [{ "text": "星座运程哪家强", "color": "#666", "size": "1", "position": "0" }
    ],
    780: [{ "text": "空虚么？寂寞么？", "color": "#666", "size": "1", "position": "0" }
    ],
    790: [{ "text": "MMGG们都去哪了", "color": "#666", "size": "1", "position": "0" }
    ],
    800: [{ "text": "想要跟男神面对面", "color": "#666", "size": "1", "position": "0" }
    ],
    810: [{ "text": "昨天晚上我想喝咖啡，结果他们说超过了配送时间，这个时候", "color": "#666", "size": "1", "position": "0" }
    ],
    820: [{ "text": "中午到了好饿不知道吃啥", "color": "#666", "size": "1", "position": "0" }
    ],
    830: [{ "text": "点了饭结果过了一个小时还不到，怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    840: [{ "text": "想要按摩，又不想出去", "color": "#666", "size": "1", "position": "0" }
    ],
    850: [{ "text": "宁泽涛，你这个大帅逼，我想给你生猴子", "color": "#666", "size": "1", "position": "0" }
    ],
    860: [{ "text": "找IOS开发", "color": "#666", "size": "1", "position": "0" }
    ],
    870: [{ "text": "想找工作", "color": "#666", "size": "1", "position": "0" }
    ],
    880: [{ "text": "想吃龙虾", "color": "#666", "size": "1", "position": "0" }
    ],
    890: [{ "text": "宠物洗澡", "color": "#666", "size": "1", "position": "0" }
    ],
    900: [{ "text": "累了没人按摩", "color": "#666", "size": "1", "position": "0" }
    ],
    910: [{ "text": "想让自己的朋友圈获得更多的赞", "color": "#666", "size": "1", "position": "0" }
    ],
    920: [{ "text": "男朋友不会拍照", "color": "#666", "size": "1", "position": "0" }
    ],
    930: [{ "text": "想找驴友", "color": "#666", "size": "1", "position": "0" }
    ],
    940: [{ "text": "女神不喜欢你", "color": "#666", "size": "1", "position": "0" }
    ],
    950: [{ "text": "找人聊聊", "color": "#666", "size": "1", "position": "0" }
    ],
    960: [{ "text": "想知道如何创业", "color": "#666", "size": "1", "position": "0" }
    ],
    970: [{ "text": "听幽默段子", "color": "#666", "size": "1", "position": "0" }
    ],
    980: [{ "text": "招不到人", "color": "#666", "size": "1", "position": "0" }
    ],
    983: [{ "text": "想做SPA", "color": "#666", "size": "1", "position": "0" }
    ],
    985: [{ "text": "半夜打不到车", "color": "#666", "size": "1", "position": "0" }
    ],
    990: [{ "text": "想跟男神/女神表白却不好意思", "color": "#666", "size": "1", "position": "0" }
    ],
    995: [{ "text": "喝咖啡", "color": "#666", "size": "1", "position": "0" }
    ],
    1000: [{ "text": "找美食", "color": "#666", "size": "1", "position": "0" }
    ],
    1010: [{ "text": "团队订巴士", "color": "#666", "size": "1", "position": "0" }
    ],
    1015: [{ "text": "买衣服", "color": "#666", "size": "1", "position": "0" }
    ],
    1020: [{ "text": "送快递", "color": "#666", "size": "1", "position": "0" }
    ],
    1024: [{ "text": "订旅游计划", "color": "#666", "size": "1", "position": "0" }
    ],
    1026: [{ "text": "找男/女朋友", "color": "#666", "size": "1", "position": "0" }
    ],
    1030: [{ "text": "解决心理问题", "color": "#666", "size": "1", "position": "0" }
    ],
    1040: [{ "text": "理财计划", "color": "#666", "size": "1", "position": "0" }
    ],
    1045: [{ "text": "买电器", "color": "#666", "size": "1", "position": "0" }
    ],
    1050: [{ "text": "女神在对面，装逼没头脑", "color": "#666", "size": "1", "position": "0" }
    ],
    1070: [{ "text": "招不到人", "color": "#666", "size": "1", "position": "0" }
    ],
    1080: [{ "text": "动作片都被女朋友删了", "color": "#666", "size": "1", "position": "0" }
    ],
    1100: [{ "text": "男盆友出去鬼混了", "color": "#666", "size": "1", "position": "0" }
    ],
    1120: [{ "text": "老板丢来大计划", "color": "#666", "size": "1", "position": "0" }
    ],
    1150: [{ "text": "上卫生间没带纸", "color": "#666", "size": "1", "position": "0" }
    ],
    1170: [{ "text": "发泄/陪聊", "color": "#666", "size": "1", "position": "0" }
    ],
    1190: [{ "text": "费脑筋想送什么礼物", "color": "#666", "size": "1", "position": "0" }
    ],
    1200: [{ "text": "心情不好", "color": "#666", "size": "1", "position": "0" }
    ],
    1220: [{ "text": "男盆友出去鬼混了", "color": "#666", "size": "1", "position": "0" }
    ],
    1240: [{ "text": "想出国旅游", "color": "#666", "size": "1", "position": "0" }
    ],
    1245: [{ "text": "想算卦", "color": "#666", "size": "1", "position": "0" }
    ],
    1250: [{ "text": "生病没药", "color": "#666", "size": "1", "position": "0" }
    ],
    1258: [{ "text": "父母生日分身乏术", "color": "#666", "size": "1", "position": "0" }
    ],
    1265: [{ "text": "不想动嘴就吃上饭", "color": "#666", "size": "1", "position": "0" }
    ],
    1285: [{ "text": "想学吉他", "color": "#666", "size": "1", "position": "0" }
    ],
    1300: [{ "text": "开心时，想找人分享", "color": "#666", "size": "1", "position": "0" }
    ],
    1310: [{ "text": "电话卡套餐选择", "color": "#666", "size": "1", "position": "0" }
    ],
    1320: [{ "text": "不想写PPT不想周末加班", "color": "#666", "size": "1", "position": "0" }
    ],
    1330: [{ "text": "甲方改了无数稿，依然不知道怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    1340: [{ "text": "火车票排队改签太麻烦", "color": "#666", "size": "1", "position": "0" }
    ],
    1350: [{ "text": "想让自己的朋友圈获得更多的赞", "color": "#666", "size": "1", "position": "0" }
    ],
    1360: [{ "text": "想装逼又不想要人知道", "color": "#666", "size": "1", "position": "0" }
    ],
    1370: [{ "text": "夏天想要自己瘦成闪电", "color": "#666", "size": "1", "position": "0" }
    ],
    1380: [{ "text": "程序员说弹幕不好搞，谁想出来的主意", "color": "#666", "size": "1", "position": "0" }
    ],
    1390: [{ "text": "其实这个主意是一个叫做Alex想的，你们不要打他", "color": "#666", "size": "1", "position": "0" }
    ],
    1400: [{ "text": "每天外卖太多选择不知道怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    1410: [{ "text": "今天有人说可以去Get上找女票我不太相信", "color": "#666", "size": "1", "position": "0" }
    ],
    1420: [{ "text": "点了饭结果过了一个小时还不到，怎么办", "color": "#666", "size": "1", "position": "0" }
    ],
    1430: [{ "text": "昨天逛街看上了一个帅哥，感觉他和我对视了两个密西西比，不过现在找不到他了", "color": "#666", "size": "1", "position": "0" }
    ],
    1440: [{ "text": "孤单寂寞需要一个萌宠来陪", "color": "#666", "size": "1", "position": "0" }
    ],
    1450: [{ "text": "女同事在和别的男同事搭讪，不理我好难过", "color": "#666", "size": "1", "position": "0" }
    ],
    1460: [{ "text": "学霸找不大工作", "color": "#666", "size": "1", "position": "0" }
    ],
    1470: [{ "text": "IOS招聘", "color": "#666", "size": "1", "position": "0" }
    ],
    1480: [{ "text": "需要招运营", "color": "#666", "size": "1", "position": "0" }
    ],
    1490: [{ "text": "气氛如何", "color": "#666", "size": "1", "position": "0" }
    ],
    1500: [{ "text": "大Boss是谁", "color": "#666", "size": "1", "position": "0" }
    ]
};


//var texts="";
//
//function loadXMLDoc() {
//    var xmlhttp;
//    if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
//        xmlhttp = new XMLHttpRequest();
//    }
//    else {// code for IE6, IE5
//        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
//    }
//    xmlhttp.onreadystatechange = function () {
//        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
//            texts = xmlhttp.responseText;
//            //xmlhttp.send();
//        }
//    };
//    xmlhttp.open("GET", "http://127.0.0.1:9000/test3", true);
//    xmlhttp.send();
//}
//
////texts = loadXMLDoc();
//alert(texts);

var texts ={
    1: [{ "text": "如何和土壕做朋友？", "color": "#666", "size": "1", "position": "0" }],
    100: [{ "text": "家里的猫抑郁了", "color": "#666", "size": "1", "position": "0" }
    ],
    500: [{ "text": "失恋了，茶饭不思", "color": "#666", "size": "1", "position": "0" }
    ],
    1490: [{ "text": "未来一周的天气怎么样", "color": "#666", "size": "1", "position": "0" }
    ]};

function getResponData(){
    $.get("http://127.0.0.1:9000/test3",function(data,status){
//    alert("数据：" + data + "\n状态：" + status);
        if(status=="success"){
            texts =data ;
        }
    });
}



function main_index_init() {

    $("#danmu").danmu({
        left: 0,    //区域的起始位置x坐标
        top: 0,  //区域的起始位置y坐标
        height: "100%", //区域的高度
        width: "90%", //区域的宽度
        zindex: 100, //div的css样式zindex
        speed: 26000, //弹幕速度，飞过区域的毫秒数
        sumtime: 1500, //弹幕运行总时间
        danmuss: texts, //danmuss对象，运行时的弹幕内容
        default_font_color: "#FFFFFF", //弹幕默认字体颜色
        font_size_small: "x-large", //小号弹幕的字体大小,注意此属性值只能是整数
        font_size_big: 48, //大号弹幕的字体大小
        opacity: "0.9", //弹幕默认透明度
        top_botton_danmu_time: 6000 //顶端底端弹幕持续时间
    });

    $('#danmu').danmu('danmu_start');

}

function share_sina_sel() {
    window.open("http://weibo.com/");
}

function share_wechat_sel() {

    if ($("#share_wechat_show").is(":hidden")) {
        $("#share_wechat_show")
        .css({
            "top": ($("#share_wechat").offset().top + 10) + "px",
            "left": ($("#share_wechat").offset().left - 210) + "px"
        }).show("fast");   //设置x坐标和y坐标，并且显示
    }
    else {
        $("#share_wechat_show").hide();
    }
}

function main_index_download() {

    var text = $.trim($('#text').val());
    if (text == "") {
        $('#danmu_submit').html("请填写内容");
        document.getElementById("text").focus();
        return;
    }
    if ($.trim($('#danmu_submit').html()) != "") {

        var text = $.trim($('#text').val());
        color = "#2196F3";
        position = "0";
        var time = $('#danmu').data("nowtime") + 5;
        var size = "1";


        //var text_obj = '{ "text":"' + text + '","color":"' + color + '","size":"' + size + '","position":"' + position + '","time":' + time + '}';
        //$.post("stone.php", { danmu: text_obj });
        var text_obj = '{ "text":"' + text + '","color":"' + color + '","size":"' + size + '","position":"' + position + '","time":' + time + ',"isnew":""}';
        var new_obj = eval('(' + text_obj + ')');
        $('#danmu').danmu("add_danmu", new_obj);

    //    $('#danmu_submit').hide();
    //    $('#danmu_qr').show();
    //    $('#danmu_submit').html("");
		$('#text').val("");
    }

	

}

function main_index_send() {
    if (event.keyCode == 13) {

        if ($.trim($('#danmu_submit').html()) != "") {
            main_index_download();
        }
        else {
            var text = document.getElementById('text').value;
            color = "#2196F3";
            position = "0";
            var time = $('#danmu').data("nowtime") + 5;
            var size = "1";


            //var text_obj = '{ "text":"' + text + '","color":"' + color + '","size":"' + size + '","position":"' + position + '","time":' + time + '}';
            //$.post("stone.php", { danmu: text_obj });
            var text_obj = '{ "text":"' + text + '","color":"' + color + '","size":"' + size + '","position":"' + position + '","time":' + time + ',"isnew":""}';
            var new_obj = eval('(' + text_obj + ')');
            $('#danmu').danmu("add_danmu", new_obj);
            $('#text').val("");
        }
    }
}