var InteractiveVideoController = function(options) {}; // needed for detection


InteractiveVideoController.update = function(vars, data){
	// get out targetid from our local vars
	var targetid = data['target'];
	$("#"+targetid).attr('autoplay',data['autoplay']);
	$("#"+targetid).attr('controls',data['controls']);
	// $("#" + targetid).parent().append("<div id=\"notificationbox\" style=\"position: absolute; top: 0;\"></div>");

	var action = data['action'];
	//console.log(data['action']);
	//console.log("c="+data+" c="+$("#"+targetid)[0].currentTime*1000);	
	switch (action) {
	case "pause":
		$("#"+targetid)[0].pause();
		break;
	case "play":
		//console.log("play action triggered");
		$("#"+targetid).trigger("play");
		break;
	case "wantedtime":
	    var wt = data['wantedtime'].split(',');
	    var realtime = wt[0];
	    var streamtime = wt[1];
		var audiotime = $("#"+targetid)[0].currentTime*1000;

 		var curtime = (new Date()).getTime();
		curtime = curtime - eddie.getTimeOffset(); 
		realtime = parseInt(realtime);
		streamtime = parseInt(streamtime);
		curtime = parseInt(curtime);
		// so in realtime-curtime we want to be at streamtime !
		var timegap = realtime-curtime;
		// where will we really be ?
		var expectedtime = audiotime + timegap;

		
		// so how far are we off ?
		var delta = streamtime - expectedtime;
		
		//console.log('RT='+realtime+' ST='+streamtime+' CT='+curtime+' AT='+audiotime+' TG='+timegap+' ET='+expectedtime+" DELTA="+delta);
		
		// lets act on it 
		if (delta<-1000 || delta>1000) {
			var newtime = ((audiotime+delta)+200)/1000;
			//console.log('seekto='+newtime);
			$("#"+targetid)[0].currentTime = newtime;
		} else {
		    var speedup = 1;
		    if (delta<0) {
		    	//console.log('neg='+delta);
		    	if (delta<-200) {
					speedup = 0.97;
				} else if (delta<-100) {
					speedup = 0.98;
				} else if (delta<-50) {
					speedup = 0.99;
				} else {
					speedup = 1;
				}
		    } else {    
		    	//console.log('pos='+delta);
				if (delta<50) {
					speedup = 1.00;
				} else if (delta<200) {
					speedup = 1.01;
				} else if (delta<400) {
					speedup = 1.02;
				} else {
					speedup = 1.03;
				}
			}
			
			//console.log('speedup='+speedup);
		   // so if are within 500ms lets speedup and see if we can catch it
		   $("#"+targetid)[0].playbackRate = speedup;
		   
		}		
		break;
	case "seek":
		//console.log('s='+data['seekingvalue'] / 1000);
		$("#"+targetid)[0].currentTime = data['seekingvalue'] / 1000;
		break;
	case "autoplay":
		$("#"+targetid)[0].autoplay = true;
		break;
	case "volumechange":
		$("#"+targetid)[0].volume  = data['volume'] / 100;
		break;
	case "newvideo":
		$("#videosrc").attr("src","http://ixxxxxx.noterik.com/videoremote/"+data['mp4']+".mp4");
		$("#"+targetid)[0].load();
		//console.log('mp4='+data['mp4']);
		break;
	default:
		break;
	}
};

map = {};
eddie.putLou("","event(InteractiveVideoController/JSReady,"+JSON.stringify(map)+")");

