var PhotoExploreController = function(options) {}; // needed for detection

var width;
var height;
var images;
var currentItem = 0;

PhotoExploreController.update = function(vars, data){
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		
		images = data['nodes'];

		$("#image-wrapper"+images[0].id).show();

		//resize once the image is loaded
		$(".zoomandaudio_image").on('load', resizePhotoExplore).each(function() {
			if (this.complete) {
				$(this).trigger('load');
			}
		});
	}
	
	var command = data['command'];
	
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
	});
}

jQuery(window).on('resize', resizePhotoExplore);