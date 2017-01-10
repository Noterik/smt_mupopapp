var StaticEntryScreenController = function(options) {}; // needed for detection

StaticEntryScreenController.update = function(vars, data) {
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		
		//resize once the image is loaded
		$("#entryimage").on('load', resizeStaticEntryScreen).each(function() {
			if (this.complete) {
				$(this).trigger('load');
			}
		});
	}
};

function resizeStaticEntryScreen(){
	var wrapperWidth = $("#image-container").width();
	var wrapperHeight = $("#image-container").height();
	
	var imgWidth = $("#entryimage").get(0).naturalWidth;
	var imgHeight = $("#entryimage").get(0).naturalHeight;
	
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
	
	if (wrapperHeight > height) {
		$("#image-wrapper").css({"margin-top": (wrapperHeight-height) / 2});
	}
}

jQuery(window).on('resize', resizeStaticEntryScreen);