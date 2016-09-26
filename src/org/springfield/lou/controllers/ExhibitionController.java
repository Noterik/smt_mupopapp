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
import org.springfield.lou.controllers.apps.photoinfospots.PhotoInfoSpotsController;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class ExhibitionController extends Html5Controller {
	
	private String path;

	public ExhibitionController() {
	}
	
	public void attach(String sel) {
		selector = sel;

		JSONObject data = new JSONObject();
		path = model.getProperty("/screen/exhibitionpath");
		String[] parts = path.split("/");
		String userid=parts[4];
		String exhibitionid=parts[6];
		String stationid=parts[8];
		model.setProperty("@username", userid);
		model.setProperty("@exhibitionid", exhibitionid);
		model.setProperty("@stationid", stationid);
		
		data.put("path",path);
		//data.put("element",model.getProperty("/screen/element"));
		System.out.println("exhibition controller called");
 		screen.get(selector).parsehtml(data);
		fillExhibition();
	}
	
	private void fillExhibition() {
		FsNode stationnode = model.getNode(path);
		
		if (stationnode!=null) {
			String app =  stationnode.getProperty("app"); // get the app name
			if (app!=null) {
				//TODO: should be a case or loaded system
				if (app.equals("photoexplore")) {
    				screen.get("#exhibition").append("div","photoexplore_app",new PhotoExploreController());
				} else if (app.equals("photoinfospots")) {
					screen.get("#exhibition").append("div","photoinfospots_app", new PhotoInfoSpotsController());
				}
			} else {
				// should display error that no app was selected and curator should set it
			}
		} else {
			// should show some illegal station controller with urls to all the valid ones?
		}
	}
 	 
}

