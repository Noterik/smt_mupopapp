var SelectionMapController = function(options) {}; // needed for detection

var width;
var height;

SelectionMapController.update = function(vars, data){
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		
		//resize once the image is loaded
		$("#selectionmapimage").on('load', resize).each(function() {
			if (this.complete) {
				$(this).trigger('load');
			}
		});
	}
}

function resize(){
	var wrapperWidth = $("#selectionmap-container").width();
	var wrapperHeight = $("#selectionmap-container").height();
	
	var imgWidth = $("#selectionmapimage").get(0).naturalWidth;
	var imgHeight = $("#selectionmapimage").get(0).naturalHeight;
	
	var widthFactor = wrapperWidth / imgWidth;
	var heightFactor = wrapperHeight / imgHeight;
	
	if (widthFactor < heightFactor) {
		width = wrapperWidth;
		height = widthFactor * imgHeight;
	} else {
		width = heightFactor * imgWidth;
		height = wrapperHeight;
	}
	
	$("#selectionmaptrackarea").css({"width": width, "height" : height});
	$(".selectionmapmask").css({"width": width, "height" : height});
	$("#selectionmapspot_holder").css({"width": width, "height" : height});
}

jQuery(window).on('resize', resize);