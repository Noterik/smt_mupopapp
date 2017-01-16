package org.springfield.lou.controllers.apps.interactivevideo;

import org.json.simple.JSONObject;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;

public class QuestionScreenController extends Html5Controller  {
	
	public QuestionScreenController() {
	}
	
	public void attach(String sel) {
		selector = sel; 		
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		JSONObject data= new JSONObject(); 
 		data.put("question_image", "http://images3.noterik.com/mupop/questionscreen_image.png");
 		data.put("question_text", "");
 		screen.get(selector).render(data);
	}
	
}
