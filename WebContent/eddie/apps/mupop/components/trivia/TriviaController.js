var TriviaController = function(options) {}; // needed for detection

var width;
var height;

TriviaController.update = function(vars, data){
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		
		//resize once the image is loaded
		$("#trivia-image").on('load', resize).each(function() {
			if (this.complete) {
				$(this).trigger('load');
			}
		});
	}
	
	var command = data['command'];
	
	if (command == "timeupdate") {
		var time = data['time'];
		
		$("#trivia-question-timer > span").text(formatTime(time));
	}
};

function resize(){
	console.log("in resize");
	var wrapperWidth = $("#trivia-image-container").width();
	var wrapperHeight = $("#trivia-image-container").height();
	
	var imgWidth = $("#trivia-image").get(0).naturalWidth;
	var imgHeight = $("#trivia-image").get(0).naturalHeight;
	
	var widthFactor = wrapperWidth / imgWidth;
	var heightFactor = wrapperHeight / imgHeight;
	
	if (widthFactor < heightFactor) {
		width = wrapperWidth;
		height = widthFactor * imgHeight;
	} else {
		width = heightFactor * imgWidth;
		height = wrapperHeight;
	}
	
	$("#trivia-image-wrapper").css({"width": width, "height" : height});
}

function formatTime(time) {
	var minutes = Math.floor(time / 60);
	var seconds = time % 60;
	
	if (minutes < 10) {
		minutes = "0"+minutes;
	}
	
	if (seconds < 10) {
		seconds = "0"+seconds;
	}
	
	return minutes+":"+seconds;
}

jQuery(window).on('resize', resize);