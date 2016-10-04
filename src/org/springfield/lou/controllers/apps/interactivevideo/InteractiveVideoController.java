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
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class InteractiveVideoController extends Html5Controller {
	
	String sharedspace;

	public InteractiveVideoController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		sharedspace = model.getProperty("/screen/sharedspace");
		screen.loadStyleSheet("interactivevideo/interactivevideo.css");
		
		String path = model.getProperty("/screen/exhibitionpath");

		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			data.put("url",stationnode.getProperty("url"));
			screen.get(selector).parsehtml(data);
			
		}
	}
	
 	 
}
