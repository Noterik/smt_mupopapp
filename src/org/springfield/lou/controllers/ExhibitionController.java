package org.springfield.lou.controllers;


import org.json.simple.JSONObject;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.apps.entryscreen.StaticEntryScreenController;
import org.springfield.lou.controllers.apps.interactivevideo.InteractiveVideoController;
import org.springfield.lou.controllers.apps.interactivevideo.WaitScreenController;
import org.springfield.lou.controllers.apps.photoexplore.PhotoExploreController;
import org.springfield.lou.controllers.apps.photoinfospots.PhotoInfoSpotsController;
import org.springfield.lou.model.ModelEvent;

public class ExhibitionController extends Html5Controller {
	
    private String path;

    public ExhibitionController() { }
	
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
	model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "false");
	fillExhibition();
    }
	
    private void fillExhibition() {
		FsNode stationnode = model.getNode(path);
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		
		String style = null;
		FsNode exhibition = model.getNode("@exhibition");
		if (exhibition != null) {
		    style = exhibition.getProperty("style");
		}
		
		if (style == null || style.equals("neutral")) {
		    screen.loadStyleSheet("styles/neutral.css");
		} else if (style.equals("leuven")) {
		    screen.loadStyleSheet("styles/leuven.css");
		} else if (style.equals("soundandvision")) {
			System.out.println("LOADING SOUND AND VISION STYLSHEET");
		    screen.loadStyleSheet("styles/soundandvision.css");
		}
		
		if (stationnode!=null) {
		    String app =  stationnode.getProperty("app"); // get the app name
		    if (app!=null) {
			System.out.println("APP="+app);
			//TODO: should be a case or loaded system
			if (app.equals("photoexplore")) {
	    			screen.get("#exhibition").append("div","photoexplore_app",new PhotoExploreController());
			} else if (app.equals("photoinfospots")) {
			    screen.get("#exhibition").append("div","photoinfospots_app", new PhotoInfoSpotsController());
			} else if (app.equals("interactivevideo")) {
				//screen.loadStyleSheet("interactivevideo/soundandvision.css");
				String isPlaying = model.getProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying");
				System.out.println("ISSSSSSPLAYING="+isPlaying);
				if(isPlaying != null && isPlaying.equals("true")){
					screen.get("#exhibition").append("div","interactivevideo_app", new InteractiveVideoController());
				}
				else{
					screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
					model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/userJoined", "startExhibition", this);
					//screen.get("#exhibition").append("div","interactivevideo_wait_screen", new WaitScreenController());
				}					
			}
		    } else {
			// should display error that no app was selected and curator should set it
		    }
		} else {
		    // should show some illegal station controller with urls to all the valid ones?
		}
    }
    
    public void startExhibition(ModelEvent e){
		screen.get("#exhibition").append("div","interactivevideo_app", new InteractiveVideoController());
		screen.get("#staticentryscreen").remove();
	}
}

