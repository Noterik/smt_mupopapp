/* 
* PhotoInfoSpotsController.java
* 
* Copyright (c) 2016 Noterik B.V.
* 
* This file is part of smt_mupopapp, related to the Noterik Springfield project.
*
* smt_mupopapp is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* smt_mupopapp is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with smt_mupopapp.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.controllers.apps.photoinfospots;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.controllers.apps.entryscreen.ImageRotationEntryScreenController;
import org.springfield.lou.controllers.apps.entryscreen.StaticEntryScreenController;
import org.springfield.lou.controllers.apps.image.selection.CoverFlowController;
import org.springfield.lou.controllers.apps.image.spotting.ZoomAndAudioController;
import org.springfield.lou.model.ModelEvent;

/**
 * PhotoInfoSpotsController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.photoinfospots
 * 
 */
public class PhotoInfoSpotsController_old extends Html5Controller {

    String state = "";
	
    public PhotoInfoSpotsController_old() { 
    	
    }

    public void attach(String sel) {
    	selector = sel;
	
    	String path = model.getProperty("/screen/exhibitionpath");
    	System.out.println("EXHIPATH="+path);
    	FsNode stationnode = model.getNode(path);
    	if (stationnode!=null) {
    		JSONObject data = new JSONObject();
    		screen.get(selector).render(data);
    	}

    	String waitscreenmode = model.getProperty("@station/waitscreenmode");
	
    	//check if we need to load a waiting screen
    	if (waitscreenmode!=null && !waitscreenmode.equals("off")) { 
    		if (waitscreenmode.equals("static")) {
    			//static entry screen
    			state = "staticentryscreen";
    			model.setProperty("@photoinfospots/vars/state", state);
        		screen.get("#staticentryscreen").remove(); // extra checks daniel
        		screen.get("#staticentryscreen").remove(); // extra checks daniel.
    			screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
    			//notify all pending screens as this could be a reload
    			model.notify("@photoinfospots/entryscreen/loaded", new FsNode("entryscreen", "loaded"));
    		} else if (waitscreenmode.equals("imagerotation")) {
    			//image rotation entry screen
    			state = "imagerotationentryscreen";
    			model.setProperty("@photoinfospots/vars/state", state);
    			screen.get("#exhibition").append("div","imagerotationentryscreen", new ImageRotationEntryScreenController());
    			//notify all pending screens as this could be a reload
    			model.notify("@photoinfospots/entryscreen/loaded", new FsNode("entryscreen", "loaded"));
    		}

    		model.onNotify("@photoinfospots/device/connected", "onDeviceConnected", this);
    		model.onNotify("@photoinfospots/image/selected", "onImageSelected", this);
    		model.onNotify("@photoinfospots/image/spotting", "onCoverflowRequested", this);
    		model.onNotify("@exhibition/entryscreen/requested", "onEntryScreenRequested", this);
    	} else {
    		System.out.println("NO WAITSCREEN LOADING IMAGE");
    		loadImageSelection();
    	}
    }
	
    public void loadImageSelection() {
    	FSList imagesList = model.getList("@images");
	    System.out.println("imagesList size="+imagesList.size());
    	if (imagesList.size() > 1) {
	    
    		state = "coverflow";
    		model.setProperty("@photoinfospots/vars/state", state);
    		screen.get("#coverflow").remove(); // extra checks daniel
    		screen.get("#coverflow").remove(); // extra checks daniel
    		screen.get("#exhibition").append("div", "coverflow", new CoverFlowController());
    	} else {
    		FsNode node = imagesList.getNodes().get(0);
    		model.setProperty("@imageid", node.getId());
    		loadZoomAndAudio();
    	}
    }
	
    public void loadZoomAndAudio() {
	state = "zoomandaudio";
	String newstate = model.getProperty("@photoinfospots/vars/state");
	System.out.println("ZOOMSTATE="+newstate);
	if (model.getProperty("@photoinfospots/vars/state")!=null && !model.getProperty("@photoinfospots/vars/state").equals(state)) {
	    model.setProperty("@photoinfospots/vars/state", state);
	    screen.get("#exhibition").append("div", "zoomandaudio", new ZoomAndAudioController());
	}
    }

    //Mobile device connected
    public void onDeviceConnected(ModelEvent e) {
	System.out.println("Received device is connected!");
	    
	if (state.equals("staticentryscreen")) {
	    screen.get("#staticentryscreen").remove();
	    loadImageSelection();
	} else if (state.equals("imagerotationentryscreen")) {
	    screen.get("#imagerotationentryscreen").remove();
	    loadImageSelection();
	} else { /* nothing to change on the mainscreen */ }
    }

    //Coverflow requested returning from zoomandaudio
    public void onCoverflowRequested(ModelEvent e) {
	screen.get("#zoomandaudio").remove();
	loadImageSelection();
    }
	
    //Image selected in coverflow
    public void onImageSelected(ModelEvent e) {
	screen.get("#coverflow").remove();
	FsNode target = e.getTargetFsNode();

	model.setProperty("@imageid", target.getId());
	model.setProperty("@photoinfospots/vars/imageid", target.getId());
	loadZoomAndAudio();
    }
    
    public void onEntryScreenRequested(ModelEvent e) {
	FsNode target = (FsNode) e.target;	

	if (model.getProperty("@exhibitionid").equals(target.getProperty("exhibition"))) {
	    if (state.equals("coverflow")) {
		screen.get("#coverflow").remove();
	    } else if (state.equals("zoomandaudio")) {
		screen.get("#zoomandaudio").remove();
	    }
	    String waitscreenmode = model.getProperty("@station/waitscreenmode");
		
	    //check if we need to load a waiting screen
	    if (waitscreenmode!=null && !waitscreenmode.equals("off")) { 
		if (waitscreenmode.equals("static")) {
		    //static entry screen
		    state = "staticentryscreen";
		    model.setProperty("@photoinfospots/vars/state", state);
		    screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
		    //notify all pending screens as this could be a reload
		    model.notify("@photoinfospots/entryscreen/loaded", new FsNode("entryscreen", "loaded"));
		} else if (waitscreenmode.equals("imagerotation")) {
		    //image rotation entry screen
		    state = "imagerotationentryscreen";
		    model.setProperty("@photoinfospots/vars/state", state);
		    screen.get("#exhibition").append("div","imagerotationentryscreen", new ImageRotationEntryScreenController());
		    //notify all pending screens as this could be a reload
		    model.notify("@photoinfospots/entryscreen/loaded", new FsNode("entryscreen", "loaded"));
		}
	    } else {
		loadImageSelection();
	    }
	}
    }
}
