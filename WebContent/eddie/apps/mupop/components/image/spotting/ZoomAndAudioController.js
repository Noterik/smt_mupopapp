var ZoomAndAudioController = function(options) {}; // needed for detection

ZoomAndAudioController.update = function(vars, data){
	console.log(data);
	
	var command = data['command'];
	var targetId = '#'+data['targetid']; 
	
	if (command == "update") {
		var audioPlayer = $(targetId+" > audio");
		
		$($(targetId+" > audio > source")[0]).attr("src", data['src']);
		audioPlayer[0].pause();
		audioPlayer[0].load();
		
		audioPlayer[0].oncanplaythrough = audioPlayer[0].play();
	}
};