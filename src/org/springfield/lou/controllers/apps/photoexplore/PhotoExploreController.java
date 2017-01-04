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
	
	int timeoutcount = 0;
	int timeoutnoactioncount = 0;
	int maxtimeoutcount = 8; //(check every 10sec)
	int maxtnoactiontimeoutcount = 2; //(check every 10sec)
	String userincontrol;
	
	public PhotoExploreController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		timeoutcount = 0;
		screen.loadStyleSheet("photoexplore/photoexplore.css");
		
		userincontrol = model.getProperty("@fromid");
		
		String path = model.getProperty("/screen/exhibitionpath");

		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			data.put("url",stationnode.getProperty("url"));
			screen.get(selector).parsehtml(data);
			
			// i think we should work with /shared/ name space, Pieter need to talk about this
			model.onPropertiesUpdate("@stationevents","onViewPortChange",this);
		}
		
		model.onNotify("/app['timers']", "onTimeoutChecks", this);
	}
	
	
	public void onViewPortChange(ModelEvent e) {
		FsPropertySet set = (FsPropertySet)e.target;
		screen.get("#photoexplore_image").css("height",set.getProperty("scale")+"%");
		screen.get("#photoexplore_image").css("left",set.getProperty("x")+"px");
		screen.get("#photoexplore_image").css("top",set.getProperty("y")+"px");
		timeoutnoactioncount=0;
	}
	
	public void onTimeoutChecks(ModelEvent e) {
		//System.out.println("TIME OUT CHECKS");
		if (timeoutcount!=-1) {
			timeoutcount++;
			timeoutnoactioncount++;
		}
		//System.out.println("TIME OUT CHECKS : "+timeoutcount+" "+timeoutnoactioncount);
		if (timeoutcount>maxtimeoutcount || timeoutnoactioncount>maxtnoactiontimeoutcount) {
			//System.out.println("APP TIMEOUT RESET WANTED");
			model.setProperty("@fromid",userincontrol);
			//screen.remove(selector);
			screen.get(selector).remove();
			timeoutcount=-1; // how do the remove not remove the notify ?
			timeoutnoactioncount=-1; // how do the remove not remove the notify ?
    		model.setProperty("/screen/state","apptimeout");
		}
	}
	
	
 	 
}
