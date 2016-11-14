package org.springfield.lou.controllers.apps.interactivevideo;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.fs.FsTimeLine;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelBindEvent;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class InteractiveVideoController extends Html5Controller {
	
	FsTimeLine timeline;

	public InteractiveVideoController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		screen.loadStyleSheet("interactivevideo/interactivevideo.css");
		
		String path = model.getProperty("/screen/exhibitionpath");

		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			data.put("url",stationnode.getProperty("url"));
			screen.get(selector).render(data);
			
		}
		System.out.println("SET AUTOPLAY!");
		model.setProperty("@videoid","1");
		screen.get("#interactivevideo_video").autoplay(true);
		screen.get("#interactivevideo_video").track("currentTime","onCurrentTime", this);
		model.onTimeLineNotify("@video","/shared['mupop']/videotimers['1']/currenttime","starttime","duration","onTimeLineEvent",this);
	}
	
	public void onTimeLineEvent(ModelEvent e) {
		if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_ENTER) {
			System.out.println("VIDEO ENTERED BLOCK ("+e.eventtype+") ="+e.getTargetFsNode().getId());
		} else if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_LEAVE) {
			System.out.println("VIDEO LEFT BLOCK ("+e.eventtype+") ="+e.getTargetFsNode().getId());
		}
	}

	
	public void onCurrentTime(Screen s,JSONObject data) {
		Double ct = 0.0;
		try {
			ct = (Double)data.get("currentTime");
		} catch(Exception e) {
			Long t = (Long)data.get("currentTime");
			ct = t.doubleValue();
		}
		model.setProperty("/shared['mupop']/videotimers['1']/currenttime",""+ct);
	}
	
 	 
}
