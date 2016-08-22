package org.springfield.lou.controllers.apps.photoexplore;


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

public class PhotoExploreController extends Html5Controller {
	
	String sharedspace;

	public PhotoExploreController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		sharedspace = model.getProperty("/screen/sharedspace");
		screen.loadStyleSheet("photoexplore/photoexplore.css");
		
		String path = model.getProperty("/screen/exhibitionpath");

		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			data.put("url",stationnode.getProperty("url"));
			screen.get(selector).parsehtml(data);
			
			// i think we should work with /shared/ name space, Pieter need to talk about this
			//	model.onPropertiesUpdate(sharedspace+"/station/1","onViewPortChange",this);
		}
	}
	
	public void onViewPortChange(ModelEvent e) {
		FsPropertySet set = (FsPropertySet)e.target;
		screen.get("#photoexplore_image").css("height",set.getProperty("scale")+"%");
		screen.get("#photoexplore_image").css("left",set.getProperty("x")+"px");
		screen.get("#photoexplore_image").css("top",set.getProperty("y")+"px");
	}
 	 
}
