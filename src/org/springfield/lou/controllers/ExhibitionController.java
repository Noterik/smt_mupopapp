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
import org.springfield.lou.controllers.apps.interactivevideo.WaitScreenController;
import org.springfield.lou.controllers.apps.photoexplore.PhotoExploreController;
import org.springfield.lou.controllers.apps.photoinfospots.PhotoInfoSpotsController;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class ExhibitionController extends Html5Controller {
	
    private String path;
    private String state="init";

    public ExhibitionController() { 
    	
    }
    
    // wait screen
	// content select
    // main app
    
    
    public void attach(String sel) {	
		selector = sel;
		model.onPropertyUpdate("/screen/state","onStateChange",this);
		model.setProperty("/screen/state","init"); // will trigger a event 
		model.onNotify("@station","onStationChange",this);
		model.onNotify("@exhibition","onStationChange",this);
    }
    
    public void onStationChange(ModelEvent event) {
    	resetScreen();
    	model.setProperty("/screen/state","init");
    	
    }
    
    public void onStateChange(ModelEvent event) {
		String state= event.getTargetFsNode().getProperty("state");
		System.out.println("MAINSCREEN STATE CHANGE="+state);
    	if (state.equals("init")) { // init the exhibition and probably show language selector
    		initStep(); 
    	} else if (state.equals("contentselect")) { // use a 'pre-app' to select the content we want to use (like coverflow)
    		screen.get("#staticentryscreen").remove();
    		contentSelectStep();
    	} else if (state.equals("mainapp")) { // check station selection method if more than one station
        	mainAppStep();
    	} else if (state.equals("apptimeout")) { // check station selection method if more than one station
        	appTimeoutStep();
    	}
    }
    
    private void initStep() {
    	String path = model.getProperty("/screen/exhibitionpath");
    	String[] parts = path.split("/");
    	String userid=parts[4];
    	String exhibitionid=parts[6];
    	String stationid=parts[8];
    	model.setProperty("@username", userid);
    	model.setProperty("@exhibitionid", exhibitionid);
    	model.setProperty("@stationid", stationid);
    	
    	String appname = model.getProperty("@station/app");
    	System.out.println("APPNAME="+appname);
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
    	
    	String style = model.getProperty("@exhibition/style");
    	if (style==null || style.equals("")) {
    		style="neutral";
    	}
    	screen.loadStyleSheet("styles/"+style+".css");
    
    	model.onNotify("@exhibitionevents/fromclient","onClientExhibitionEvent",this);
    	model.onNotify("@stationevents/fromclient","onClientStationEvent",this);
    	
    	String waitscreen = model.getProperty("@station/waitscreen");
    	if (waitscreen!=null && !waitscreen.equals("") && !waitscreen.equals("none")) {
        	String waitscreen_content = model.getProperty("@station/waitscreen_content");
    		System.out.println("STATIC ENTRY SCREEN WANTED ="+waitscreen+" set="+waitscreen_content);
    		if (waitscreen.equals("static")) {
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
    	//System.out.println("CLIENT Exhibition EVENT="+e);
    	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String request = message.getProperty("request");
    	//System.out.println("REQ="+request);
    	if (request!=null) { // so its a request for something !
    		if (request.equals("join")) {
    			// ok lets see what we need todo and reply back to client
    			System.out.println("FROM="+from);
    			Screen client = ApplicationManager.getScreenByFullid(from);
    			client.getModel().setProperty("/screen/state","stationselect");
    		}
    	}
    	
    }
  
    public void onClientStationEvent(ModelEvent e) {
    	//System.out.println("CLIENT station EVENT="+e);
    	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String request = message.getProperty("request");
    	//System.out.println("REQ="+request);
    	if (request!=null) { // so its a request for something !
    		if (request.equals("station")) {
    			// ok lets see what we need todo and reply back to client
    			System.out.println("FROM="+from);
    			Screen client = ApplicationManager.getScreenByFullid(from);
    			System.out.println("WHOOOuUUU="+model.getProperty("@station/contentselect"));
    			String contentselect = model.getProperty("@station/contentselect");
    			if (contentselect!=null && !contentselect.equals("none")) {
    				client.getModel().setProperty("/screen/state","contentselect");
    				model.setProperty("@fromid", from);
    				model.setProperty("/screen/state","contentselect");	
    			} else {
    				client.getModel().setProperty("/screen/state","mainapp");
    				model.setProperty("@fromid", from);
    				model.setProperty("/screen/state","mainapp");
    			}
    		}
    	}
    	
    }
  
    
    
    private void contentSelectStep() {
    	String type = model.getProperty("@station/contentselect");
    	System.out.println("MuPoP MAIN : content select step called ="+type);
    	if (type!=null && !type.equals("")) {
    		if (type.equals("coverflow")) {
    			screen.get("#exhibition").append("div", "coverflow", new CoverFlowController());
    			return;
    		}
    	}
		model.setProperty("/screen/state","mainapp"); 
    }
    
    private void appTimeoutStep() {
    	// so the app has timed out either by user doing nothing or using app too long
    	// tell client to react and reset outselves
    	System.out.println("APP RESET");
		model.setProperty("/screen/state","init"); 
		
		String userincontrol = model.getProperty("@fromid");
		Screen client = ApplicationManager.getScreenByFullid(userincontrol);
		client.getModel().setProperty("/screen/state","stationselect");
    }
    
    private void mainAppStep() {
    	JSONObject data = new JSONObject();
    	path = model.getProperty("/screen/exhibitionpath");
		FsNode stationnode = model.getNode(path);
    	System.out.println("MuPoP MAIN : mainapp step called ="+stationnode.asXML());
		
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
    	screen.get(selector).render(data);
    	model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "false");
    	
		if (stationnode!=null) {
		    String app =  stationnode.getProperty("app"); // get the app name
		    if (app!=null) {
		    	System.out.println("APP2="+app);
		    	//TODO: should be a case or loaded system
		    	if (app.equals("photoexplore")) {
	    			screen.get("#exhibition").append("div","photoexplore_app",new PhotoExploreController());
		    	} else if (app.equals("photoinfospots")) {
		    		screen.get("#exhibition").append("div","photoinfospots_app", new PhotoInfoSpotsController());
		    	} else if (app.equals("interactivevideo")) {
					screen.get("#exhibition").append("div","interactivevideo_app", new InteractiveVideoController());
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
    }

}

