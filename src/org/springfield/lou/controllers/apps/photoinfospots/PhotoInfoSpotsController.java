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
public class PhotoInfoSpotsController extends Html5Controller {

	//TODO: handle this better
	String state = "waiting";
	
	public PhotoInfoSpotsController() {
	}
	
	/*
	 * Attach the controller to the screen
	 * @see org.springfield.lou.controllers.Html5Controller#attach(java.lang.String)
	 */
	public void attach(String sel) {
		selector = sel;
		
		String path = model.getProperty("/screen/exhibitionpath");

		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			screen.get(selector).parsehtml(data);
		}
		
		System.out.println("Loading entry screen");
		
		System.out.println("MODE="+model.getProperty("@station/waitscreenmode"));
		String waitscreenmode = model.getProperty("@station/waitscreenmode");
		
		if (waitscreenmode!=null && !waitscreenmode.equals("off")) { // is there a intro screen?
			// Pieter you need to work with these values for waitscreens, like 'static' or 'animation'
			// as far as i can see they are not the same as having a 'selector' like a grid or image 
		
			//screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
		    	screen.get("#exhibition").append("div","imagerotationentryscreen", new ImageRotationEntryScreenController());
		    
			// so this is i think if you have a selector screen (like grid or imageGallery)
					
			model.onNotify("/shared/photoinfospots/device/connected", "onDeviceConnected", this);
			model.onNotify("/shared/photoinfospots/image/selected", "onImageSelected", this);
			model.onNotify("/shared/photoinfospots/image/spotting", "onCoverflowRequested", this);
		} else {
			screen.get("#exhibition").append("div", "zoomandaudio", new ZoomAndAudioController());
		}
	}
	
	public void onDeviceConnected(ModelEvent e) {
	    System.out.println("Received device is connected!");
	    
	    if (state.equals("waiting")) {
		//screen.get("#staticentryscreen").remove();
		screen.get("#imagerotationentryscreen").remove();
    			
    		String selectionmode = model.getProperty("@station/selectionmode");
    		if (selectionmode!=null && !selectionmode.equals("off")) { 
    		    state = "coverflow";
    		    screen.get("#exhibition").append("div", "coverflow", new CoverFlowController());
    		} else {
    			
    		}
	    }
	}
	
	public void onCoverflowRequested(ModelEvent e) {
	    screen.get("#zoomandaudio").remove();
	    
	    String selectionmode = model.getProperty("@station/selectionmode");
	    if (selectionmode!=null && !selectionmode.equals("off")) { 
		state = "coverflow";
		screen.get("#exhibition").append("div", "coverflow", new CoverFlowController());
	    } else {
		
	    }
	}
	
	public void onImageSelected(ModelEvent e) {
		screen.get("#coverflow").remove();
		FsNode target = e.getTargetFsNode();	
		System.out.println("e="+target.asXML());
		model.setProperty("/screen/imageurl","http://images1.noterik.com/mupop/"+target.getId()+"-500px.jpg");
		screen.get("#exhibition").append("div", "zoomandaudio", new ZoomAndAudioController());
	}
}
