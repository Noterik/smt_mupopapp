var PhotoExploreController = function(options) {}; // needed for detection

var width;
var height;
var images;
var currentItem = 0;

PhotoExploreController.update = function(vars, data){
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		currentItem = 0;
		width = -1;
		height = -1;
		images = null;
		
		
		images = data['nodes'];

		$("#image-wrapper"+images[0].id).show();
		vars["ready"] = true;

		//resize once the image is loaded
		$(".zoomandaudio_image").on('load', resizePhotoExplore).each(function() {
			if (this.complete) {
				$(this).trigger('load');
			}
		});
	}
	
	var command = data['command'];
	
	console.log(data);
	
	if (vars["ready"] && vars["ready"] == true) {
	
		switch (command) {
			case "next": 
				if (currentItem < images.length-1) {	
					$("#image-wrapper"+images[currentItem].id).fadeOut();
					currentItem++;
					$("#image-wrapper"+images[currentItem].id).fadeIn();
				}
				break;
			case "prev":
				if (currentItem > 0) {
					$("#image-wrapper"+images[currentItem].id).fadeOut();
					currentItem--;
					$("#image-wrapper"+images[currentItem].id).fadeIn();
				}
				break;
			case "scale": 
				var scaleValue = parseFloat(data['value']);
				if (scaleValue < 1.0) { scaleValue = 1.0 }	//Don't allow going smaller then 1.0
				if (scaleValue > 25.0) { scaleValue = 25.0 } // Dont' allow going bigger then 25.0
				$("#image-wrapper"+images[currentItem].id).css("transform", "scale("+scaleValue+")");
				$("#image-wrapper"+images[currentItem].id).css("transform-origin", data['originX']+"% "+ data['originY'] +"%");
	
				break;
		}
	}
};

function fade() {
	$("#image-wrapper"+images[currentItem].id).fadeOut();
	currentItem++;
	$("#image-wrapper"+images[currentItem].id).fadeIn();
}

function resizePhotoExplore(){	
	var wrapperWidth = $("#photoexplore-image-container").width();
	var wrapperHeight = $("#photoexplore-image-container").height();
	
	$(".image-wrapper").each(function(index) {
		var imgWidth = $("#photoexplore_image-"+images[index].id).get(0).naturalWidth;
		var imgHeight = $("#photoexplore_image-"+images[index].id).get(0).naturalHeight;

		var widthFactor = wrapperWidth / imgWidth;
		var heightFactor = wrapperHeight / imgHeight;
		
		if (widthFactor < heightFactor) {
			width = wrapperWidth;
			height = widthFactor * imgHeight;
		} else {
			width = heightFactor * imgWidth;
			height = wrapperHeight;
		}
		
		$("#image-wrapper"+images[index].id).css({"width": width, "height" : height});
		
		if (wrapperHeight > height) {
			$("#image-wrapper"+images[index].id).css({"margin-top": (wrapperHeight-height) / 2});
		}
	});
}

jQuery(window).on('resize', resizePhotoExplore);