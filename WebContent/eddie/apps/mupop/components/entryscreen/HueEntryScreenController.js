var HueEntryScreenController = function(options) {}; // needed for detection
H
ueEntryScreenController.update = function(vars, data) {
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		
		//resize once the image is loaded
		//We now use object-fit: cover to avoid borders
		/*$("#entryimage").on('load', resizeStaticEntryScreen).each(function() {
			if (this.complete) {
				$(this).trigger('load');
			}
			
		});*/
		
		
	}
};

	if($("#entryimage")[0]){
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
}

//jQuery(window).on('resize', resizeStaticEntryScreen);