package org.springfield.lou.controllers.apps.interactivevideo;

import org.json.simple.JSONObject;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class WaitScreenController extends Html5Controller  {
		
	public WaitScreenController() {
	}
	
	public void attach(String sel) {
		selector = sel; 		
 		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		
		JSONObject data = new JSONObject();
		data.put("exhibition_title","Such an amazing exhibition title");
		data.put("image_source","http://www.hannabasinmuseum.com/uploads/6/5/9/5/6595987/7661052_orig.jpg");
		data.put("exhibition_url","mupop.net/fake");
		screen.get(selector).render(data);
		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/userJoined", "startExhibition", this);
		
	}
	
	
	public void startExhibition(ModelEvent e){
		screen.get("#exhibition").append("div","interactivevideo_app", new InteractiveVideoController());
		screen.get(selector).remove();
	}
	
}
