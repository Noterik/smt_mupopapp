package org.springfield.lou.controllers;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.controllers.apps.photoexplore.PhotoExploreController;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class ExhibitionController extends Html5Controller {
	
	private String path;

	public ExhibitionController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		screen.loadStyleSheet("exhibition/exhibition.css");
		JSONObject data = new JSONObject();
		path = model.getProperty("/screen/exhibitionpath");
		data.put("path",path);
		//data.put("element",model.getProperty("/screen/element"));
		System.out.println("exhibition controller called");
 		screen.get(selector).parsehtml(data);
		fillExhibition();
	}
	
	private void fillExhibition() {
		FsNode stationnode = model.getNode(path);
		
		if (stationnode!=null) {
			System.out.println("STATIONNODE="+stationnode.asXML());
			String app =  stationnode.getProperty("app"); // get the app name
			if (app!=null) {
				// should be a case or loaded system
				if (app.equals("photoexplore")) {
    				screen.get("#exhibition").append("div","photoexplore_app",new PhotoExploreController());
				}
			} else {
				// should display error that no app was selected and curator should set it
			}
		} else {
			// should show some illegal station controller with urls to all the valid ones?
		}
	}
 	 
}

