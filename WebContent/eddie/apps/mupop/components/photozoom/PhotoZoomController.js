var PhotoZoomController = function(options) {}; // needed for detection

/*
 * TODO: This is a workaround to the problem of poluting the window namespace. 
 * This should not be used in the future. Frontend in MuPop should be refactored. 
 */
PhotoZoomController.instance = (function(window, $) {
	//SECTION: Cached jQuery objects. Caching jQuery objects instead of calling $() everytime you need an element improves performance. 
	var $imageContainer = $("#zoomandaudio-image-container"),
		$image = $("#zoomandaudio_image"),
		$imageWrapper = $('#image-wrapper'),
		$layer = $(".zoomandaudio_layer"),
		$spotsHolder = $("#zoomandaudio_spots_holder");

	//SECTION: Raw HTML elements, not usage of cached $image jQuery object.
	var imageContainer = $imageContainer[0],
		image = $image[0];
	
	//SECTION: Generic instance variables
	var zoomedImage = new window.ntk.Zoomable(image, {
		defaultZoomRate: 2
	}, imageContainer);

	//SECTION: Private functions
	function resize(){
		var wrapperWidth = $imageContainer.width();
		var wrapperHeight = $imageContainer.height();
		
		var imgWidth = $image.get(0).naturalWidth;
		var imgHeight = $image.get(0).naturalHeight;
		
		var widthFactor = wrapperWidth / imgWidth;
		var heightFactor = wrapperHeight / imgHeight;
		
		if (widthFactor < heightFactor) {
			width = wrapperWidth;
			height = widthFactor * imgHeight;
		} else {
			width = heightFactor * imgWidth;
			height = wrapperHeight;
		}
		
		$imageWrapper.css({"width": width, "height" : height});
		$layer.css({"width": width, "height" : height});
		$spotsHolder.css({"width": width, "height" : height});
		zoomedImage.refresh();
	}
	
	//SECTION constructor
	$(window).on('resize', resize);
	
	//SECTION: Public functions
	return {
		update: function(vars, data){
			//init - this is also handled when returning on a page
			if (!vars["loaded"]) {	
				vars["loaded"] = true;
				
				//resize once the image is loaded
				$image.on('load', resize).each(function() {
					console.log('resized!');
					if (this.complete) {
						$image.trigger('load');
					}
				});
			}
			
			var command = data['command'];
			
			if (command == "spot_move") {		
				var x = ((data['x'] / 100) * width);
				var y = ((data['y'] / 100) * height);
				
				zoomedImage.setPosition(data['spotid'], x, y);

				//$(data['spotid']).css('transform','translate('+x+'px,'+y+'px)');
			}
		}
	}
})(window, jQuery); // needed for detection

PhotoZoomController.update = PhotoZoomController.instance.update;