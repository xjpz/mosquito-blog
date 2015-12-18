function showTime() {
    var date = new Date(); //日期对象
	var now = "";
	now = now + date.getHours()+":";
	now = now + date.getMinutes()+":";
	now = now + date.getSeconds();
    document.getElementById("spanTime").innerHTML = now;
}

window.addEventListener('load',showTime,false);
//启动时钟
var timer;
function startClock() {
    timer = window.setInterval(showTime,1000);
}
window.addEventListener('load',startClock,false);

function dateTime() {
    var date = new Date(); //日期对象

    document.getElementById("date1Time").innerHTML = 
                date.toLocaleDateString();
}

window.addEventListener('load',dateTime,false);
//启动时钟
var timer;
function startClock1() {
    timer = window.setInterval(dateTime,1000);
}
window.addEventListener('load',startClock,false);