var PhotoInfoSpotsController = function(options) {}; // needed for detection

var width;
var height;

PhotoInfoSpotsController.update = function(vars, data){
	console.log('PhotoInfoSpotsController.update(', vars , ', ', data , ')');
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		
		//resize once the image is loaded
		$("#zoomandaudio_image").on('load', resize).each(function() {
			if (this.complete) {
				$(this).trigger('load');
			}
		});
	}
	
	var command = data['command'];
	
	if (command == "spot_move") {		
		var x = ((data['x'] / 100) * width);
		var y = ((data['y'] / 100) * height);

		$(data['spotid']).css('transform','translate('+x+'px,'+y+'px)');
	}
};

function resize(){
	var wrapperWidth = $("#zoomandaudio-image-container").width();
	var wrapperHeight = $("#zoomandaudio-image-container").height();
	
	var imgWidth = $("#zoomandaudio_image").get(0).naturalWidth;
	var imgHeight = $("#zoomandaudio_image").get(0).naturalHeight;
	
	var widthFactor = wrapperWidth / imgWidth;
	var heightFactor = wrapperHeight / imgHeight;
	
	if (widthFactor < heightFactor) {
		width = wrapperWidth;
		height = widthFactor * imgHeight;
	} else {
		width = heightFactor * imgWidth;
		height = wrapperHeight;
	}
	
	$("#image-wrapper").css({"width": width, "height" : height});
	$(".zoomandaudio_layer").css({"width": width, "height" : height});
	$("#zoomandaudio_spots_holder").css({"width": width, "height" : height});
}

jQuery(window).on('resize', resize);