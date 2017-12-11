/* 
* 
* Copyright (c) 2017 Noterik B.V.
* 
* This file is part of MuPoP framework
*
* MuPoP framework is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* MuPoP framework is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with MuPoP framework .  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.controllers;


import java.util.Iterator;

import org.json.simple.JSONObject;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.controllers.apps.entryscreen.ImageRotationEntryScreenController;
import org.springfield.lou.controllers.apps.entryscreen.StaticEntryScreenController;
import org.springfield.lou.controllers.apps.image.selection.CoverFlowController;
import org.springfield.lou.controllers.apps.interactivevideo.InteractiveVideoController;
import org.springfield.lou.controllers.apps.photoexplore.PhotoExploreController;
import org.springfield.lou.controllers.apps.photoinfospots.PhotoInfoSpotsController;
import org.springfield.lou.controllers.apps.trivia.TriviaController;
import org.springfield.lou.controllers.apps.whatwethink.WhatWeThinkController;
import org.springfield.lou.controllers.apps.photozoom.PhotoZoomController;

//import org.springfield.lou.controllers.apps.photoinfospots.PhotoInfoSpotsController_old;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class ExhibitionController extends Html5Controller {
	
    private String path;
    private String state="";

    public ExhibitionController() { 
    	
    }

    // wait screen
    // content select
    // main app
    
    
    public void attach(String sel) {	
	selector = sel;
	
	
    	model.onPropertyUpdate("/screen/state","onStateChange",this);
    	model.setProperty("@appstate", state);
    	model.setProperty("/screen/state","init"); // will trigger a event 
    	model.onNotify("@station","onStationChange",this);
    	model.onNotify("@exhibition","onStationChange",this);
    }
    
    public void onStationChange(ModelEvent event) {
    	resetScreen();
    	state="";
    	model.setProperty("@appstate", state);
    	model.setProperty("/screen/state","init");
    	
    }
    
    public void onStateChange(ModelEvent event) {
	String state= event.getTargetFsNode().getProperty("state");
	//boolean force = Boolean.parseBoolean(event.getTargetFsNode().getProperty("force"));
	
	String currentState = model.getProperty("@appstate");
	
	if (state.equals(currentState)) {
	    //already in the correct state, no need to change main screen
	    return;
	}

	if (state.equals("init")) { // init the exhibition and probably show language selector
	    initStep(); 
    	} else if (state.equals("contentselect") && (!currentState.equals("mainapp"))) { // && !force)) { // use a 'pre-app' to select the content we want to use (like coverflow)
    	    resetScreen();
    	    contentSelectStep();
    	} else if (state.equals("contentselectforce")) { //force contentselect due to timeout (better to fix this using a property in the modelevent, currently not possible due to single property that is set, should be a set)
    	    state = "contentselect";
    	    resetScreen();
    	    contentSelectStep();
       	} else if (state.equals("mainapp")) { // check station selection method if more than one station
    	    resetScreen();
    	    mainAppStep();
    	} else if (state.equals("apptimeout")) { // check station selection method if more than one station
    	    appTimeoutStep();
    	} else {
    	    //no state change
    	    return;
    	}
		model.setProperty("@appstate", state);
    }
    
    private void initStep() {    	
    	String appname = model.getProperty("@station/app");
    	if (appname==null || appname.equals("") || appname.equals("none")) {
    		// no app selected, set html to reflect this
    		screen.get("#exhibition").html("<div id=\"noappselected\">No App Selected</div>");
    		return;
    	}
    	
    	String roomstate = model.getProperty("@station/room");
    	if (roomstate==null || roomstate.equals("") || roomstate.equals("offline")) {
    		// no app selected, set html to reflect this
    		screen.get("#exhibition").html("<div id=\"noappselected\">Station is offline</div>");
    		return;
    	}
    	
    	
    	String style = model.getProperty("@station/style");
    	if (style==null || style.equals("")) {
        	style = model.getProperty("@exhibition/style");
        	if (style==null || style.equals("")) {
        		style="neutral";
        	}
    	}
    	screen.loadStyleSheet("styles/"+style+".css");
    
    	model.onNotify("@exhibitionevents/fromclient","onClientExhibitionEvent",this);
    	model.onNotify("@stationevents/fromclient","onClientStationEvent",this);
    	
    	String waitscreen = model.getProperty("@station/waitscreen");
    	if (waitscreen!=null && !waitscreen.equals("") && !waitscreen.equals("none")) {
        	String waitscreen_content = model.getProperty("@station/waitscreen_content");
    		if (waitscreen.equals("static")) {
        		screen.get("#staticentryscreen").remove(); // extra checks daniel
        		screen.get("#staticentryscreen").remove(); // extra checks daniel
    			screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
    		} else if (waitscreen.equals("kenburn")) {
        		screen.get("#exhibition").append("div","imagerotationentryscreen", new ImageRotationEntryScreenController());
    		}	
    		//model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/userJoined", "startExhibition", this);
    	} else {
    		// move to the next logical state
    		model.setProperty("/screen/state","contentselect");
    	}
    }
    
    public void onClientExhibitionEvent(ModelEvent e) {
    	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String request = message.getProperty("request");
    	if (request!=null) { // so its a request for something !
    		if (request.equals("join")) {
    			// ok lets see what we need todo and reply back to client
    			Screen client = ApplicationManager.getScreenByFullid(from);
    			client.getModel().setProperty("/screen/state","stationselect");
    		}
    	}
    	
    }
  
    public void onClientStationEvent(ModelEvent e) {
    	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String request = message.getProperty("request");
    	if (request!=null) { // so its a request for something !
    		if (request.equals("station")) {
    			// ok lets see what we need todo and reply back to client
    			Screen client = ApplicationManager.getScreenByFullid(from);
    			String contentselect = model.getProperty("@station/contentselect");
    			if (contentselect!=null && !contentselect.equals("none")) {
        			client.getModel().setProperty("@exhibitionid",model.getProperty("@exhibitionid"));
        			client.getModel().setProperty("@stationid",model.getProperty("@stationid"));
        			client.getModel().setProperty("@username",model.getProperty("@username"));
    				client.getModel().setProperty("/screen/state","contentselect");
    				model.setProperty("@fromid", from);
    				model.setProperty("/screen/state","contentselect");	
    			} else {
        			client.getModel().setProperty("@exhibitionid",model.getProperty("@exhibitionid"));
        			client.getModel().setProperty("@stationid",model.getProperty("@stationid"));
    				client.getModel().setProperty("/screen/state","mainapp");
    				model.setProperty("@fromid", from);
    				model.setProperty("/screen/state","mainapp");
    			}
    		} else if (request.equals("contentselectforce")) {
    		    model.setProperty("/screen/state", request);
    		}
    	}
    }
  
    
    
    private void contentSelectStep() {
    	String type = model.getProperty("@station/contentselect");
    	if (type!=null && !type.equals("")) {
    		if (type.equals("coverflow")) {
        		screen.get("#coverflow").remove(); // extra checks daniel
        		screen.get("#coverflow").remove(); // extra checks daniel
    			screen.get("#exhibition").append("div", "coverflow", new CoverFlowController());
    			return;
    		}
    	}
		model.setProperty("/screen/state","mainapp"); 
    }
    
    private void appTimeoutStep() {
    	// so the app has timed out either by user doing nothing or using app too long
    	// tell client to react and reset outselves
    	resetScreen();
    	model.setProperty("/screen/state","init"); 
    	
    	//We need to inform all screens on this station connected
    	FsNode message = new FsNode("message",screen.getId());
    	message.setProperty("request","init");
    	model.notify("@stationevents/fromclient",message);
    }
    
    private void mainAppStep() {
    	
    	
    	JSONObject data = new JSONObject();
		FsNode stationnode = model.getNode("@station");
		
		
    	data.put("path",path);
    	//data.put("element",model.getProperty("/screen/element"));
    	screen.get(selector).render(data);
    	//model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "false");
    	
		if (stationnode!=null) {
		    String app =  stationnode.getProperty("app"); // get the app name
		    if (app!=null) {
		    	//TODO: should be a case or loaded system
		    	if (app.equals("photoexplore")) {
	        		screen.get("#photoexplore_app").remove(); // extra checks daniel
	        		screen.get("#photoexplore_app").remove(); // extra checks daniel
	    			screen.get("#exhibition").append("div","photoexplore_app",new PhotoExploreController());
		    	} else if (app.equals("photoinfospots")) {
	        		screen.get("#photoinfospots_app").remove(); // extra checks daniel
	        		screen.get("#photoinfospots_app").remove(); // extra checks daniel
		    		screen.get("#exhibition").append("div","photoinfospots_app", new PhotoInfoSpotsController());
		    	} else if (app.equals("interactivevideo")) {
	        		screen.get("#interactivevideo_app").remove(); // extra checks daniel
	        		screen.get("#interactivevideo_app").remove(); // extra checks daniel
					screen.get("#exhibition").append("div","interactivevideo_app", new InteractiveVideoController());
		    	} else if (app.equals("trivia")) {
	        		screen.get("#trivia_app").remove(); // extra checks daniel
	        		screen.get("#trivia_app").remove(); // extra checks daniel
					screen.get("#exhibition").append("div","trivia_app", new TriviaController());
		    	} else if (app.equals("whatwethink")) {
	        		screen.get("#whatwethink_app").remove(); // extra checks daniel
	        		screen.get("#whatwethink_app").remove(); // extra checks daniel
					screen.get("#exhibition").append("div","whatwethink_app", new WhatWeThinkController());
		    	} else if (app.equals("photozoom")) {
		    		//TODO: We have to do this twice? (David)
		    		screen.get("#photozoom_app").remove(); 
	        		screen.get("#photozoom_app").remove(); 
	        		
					screen.get("#exhibition").append("div","photozoom_app", new PhotoZoomController());
		    	} else {
		    		// should display error that no app was selected and curator should set it
		    	}
		    }
		} else {
		    // should show some illegal station controller with urls to all the valid ones?
		}
    }
    
    private void resetScreen() {
    	screen.get("#staticentryscreen").remove();	
    	screen.get("#imagerotationentryscreen").remove();
    	screen.get("#coverflow").remove();
    	screen.get("#photoexplore_app").remove();
    	screen.get("#photoinfospots_app").remove();
    	screen.get("#photozoom_app").remove();
    }
}
