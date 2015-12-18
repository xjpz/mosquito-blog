(function($){
	/*
		simpler music player
	*/
	var _playstatus = false;
	var _palyInterval;
	var n=0;
	$.fn.simplemusic=function(settings){
		$(this).css({"z-index":"999","width":"44px","height":"44px","position":"fixed","bottom":"10px","right":"10px","cursor":"pointer","background":"url(http://xjpz.me/assets/images/o_music_note_big.png)"}).bind("click",function(){
			if(_playstatus){
				pause(this);
			}else{
				play(this);
			}
		});
		var audio = document.createElement("audio");
		$(audio).attr("id","audioPlayer").css("display","none").prependTo(this);
		
		if(settings.loop && settings.loop==true){ $(audio).attr("loop","loop");}
		if(settings.urls && settings.urls instanceof Array){
			for(var i in settings.urls){
				var source = document.createElement("source");
				$(source).attr("src",settings.urls[i]).appendTo($(audio));
			}
		}
		if(settings.autoplay&&settings.autoplay == true){$(this).click();}
	};
	function play(e){
		_palyInterval = setInterval(function(){
			startRotate(e);
		},10);
		$("#audioPlayer")[0].play();
		_playstatus=true;
	}
	function pause(){
		$("#audioPlayer")[0].pause();
		clearInterval(_palyInterval);
		_playstatus=false;
	}
	function startRotate(e){
		n=n+1;
		e.style.transform="rotate(" + n + "deg)";
		e.style.webkitTransform="rotate(" + n + "deg)";
		e.style.OTransform="rotate(" + n + "deg)";
		e.style.MozTransform="rotate(" + n + "deg)";
		e.style.opacity = Math.abs(n/360-0.5)+0.5+"";
		if (n==360){n=0}
		
	}
})(jQuery);