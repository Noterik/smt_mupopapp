package org.springfield.lou.controllers.apps.photoexplore;

import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;

public class PhotoExploreController extends Html5Controller {
	
    int timeoutcount = 0;
    int timeoutnoactioncount = 0;
    int maxtimeoutcount = 4; //(check every 10sec)
    int maxtnoactiontimeoutcount = 2; //(check every 10sec)
    String userincontrol;
	
    public PhotoExploreController() { }
	
    public void attach(String sel) {
	selector = sel;
	timeoutcount = 0;
	screen.loadStyleSheet("photoexplore/photoexplore.css");
		
	userincontrol = model.getProperty("@fromid");
		
	String path = model.getProperty("/screen/exhibitionpath");

	FsNode exhibitionnode = model.getNode("@exhibition");
	FsNode stationnode = model.getNode(path);
	    
	if (stationnode!=null) {
	    model.setProperty("@contentrole","mainapp");
	    model.setProperty("@itemid", model.getProperty("/screen/selecteditem"));

	    FSList imagesList = model.getList("@itemimages");
	    System.out.println("Found "+imagesList.size()+" images");

	    List<FsNode> nodes = imagesList.getNodes();
	    JSONObject data = FSList.ArrayToJSONObject(nodes,"en","url"); 
	    data.put("jumper", exhibitionnode.getProperty("jumper"));
	    
	    FsNode language_content = model.getNode("@language_photoexplore_coverflow_screen");
	    data.put("logincode", language_content.getSmartProperty("en", "login_code"));
	    data.put("code", model.getProperty("@station/codeselect"));
	    
	    screen.get(selector).parsehtml(data);
	    screen.get(selector).loadScript(this);
	    
	    // = new JSONObject();	
	    JSONObject d = FSList.ArrayToJSONObject(nodes,"en","url");
	    d.put("command","init");
	    screen.get(selector).update(d);
	    
	    // i think we should work with /shared/ name space, Pieter need to talk about this
	    model.onPropertiesUpdate("@stationevents","onViewPortChange",this);
	}
	
	model.onNotify("@stationevents/fromclient","onClientStationEvent",this);
	model.onNotify("/app['timers']", "onTimeoutChecks", this);
    }
    
    public void onClientStationEvent(ModelEvent e) {	    
	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String command = message.getProperty("action");
    	
    	timeoutnoactioncount = 0;
    
        if (command.equals("leftonzoom")) {
    	    JSONObject d = new JSONObject();
    	    d.put("command", "prev");
    	    screen.get(selector).update(d);
    	} else if (command.equals("rightonzoom")) {
    	    JSONObject d = new JSONObject();
    	    d.put("command", "next");
    	    screen.get(selector).update(d);
    	} else if (command.equals("scale")) {        	
            //String transformationCss = "scale("+(String) message.getProperty("value")+")";
            //String originCss = message.getProperty("originX") + "% " + message.getProperty("originY") + "%";
            
            JSONObject d = new JSONObject();
            d.put("command", "scale");
            d.put("value", message.getProperty("value"));
            d.put("originX", message.getProperty("originX"));
            d.put("originY", message.getProperty("originY"));  
            screen.get(selector).update(d);
            
           // screen.get("#presenterarea").css("transform", transformationCss);
           // screen.get("#presenterarea").css("transform-origin", originCss);
    	}
    }
	
    public void onViewPortChange(ModelEvent e) {
	FsPropertySet set = (FsPropertySet)e.target;
	screen.get("#photoexplore_image").css("height",set.getProperty("scale")+"%");
	screen.get("#photoexplore_image").css("left",set.getProperty("x")+"px");
	screen.get("#photoexplore_image").css("top",set.getProperty("y")+"px");
	timeoutnoactioncount=0;
    }
	
    public void onTimeoutChecks(ModelEvent e) {
	if (timeoutcount!=-1) {
	    timeoutcount++;
	    timeoutnoactioncount++;
	}

	if (timeoutcount>maxtimeoutcount || timeoutnoactioncount>maxtnoactiontimeoutcount) {
	    //System.out.println("APP TIMEOUT RESET WANTED");
	    model.setProperty("@fromid",userincontrol);
	    //screen.remove(selector);
	    screen.get(selector).remove();
	    timeoutcount=-1; // how do the remove not remove the notify ?
	    timeoutnoactioncount=-1; // how do the remove not remove the notify ?
	    model.setProperty("/screen/state","contentselectforce");
	    
	    FsNode message = new FsNode("message",screen.getId());
	    message.setProperty("request","contentselect");
	    model.notify("@stationevents/fromclient",message);
	}
    }	 
}
