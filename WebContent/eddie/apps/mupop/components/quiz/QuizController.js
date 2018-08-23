var QuizController = function(options) {}; // needed for detection

QuizController.update = function(vars, data){
	//init - this is also handled when returning on a page
	if (!vars["loaded"]) {	
		vars["loaded"] = true;
		
	}
	//set counter
	var command = data['command'];
	var targetId = '#'+data['targetid']; 
		
	console.log(command);
	console.log(data);
		
		
	switch (command) {
	case "timer": 
		$(".timer-lt").css({"animation": data['timeout']+"s linear 1 forwards timer-slide-lt"});
		$(".timer-rt").css({"animation": data['timeout']+"s linear 1 forwards timer-slide-rt"});
	break;
	}
}