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
public class PhotoInfoSpotsController extends Html5Controller {

    String state = "";
	
    public PhotoInfoSpotsController() { }

    public void attach(String sel) {
	selector = sel;
		
	String path = model.getProperty("/screen/exhibitionpath");

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
		screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
	    } else if (waitscreenmode.equals("imagerotation")) {
		//image rotation entry screen
		state = "imagerotationentryscreen";
		screen.get("#exhibition").append("div","imagerotationentryscreen", new ImageRotationEntryScreenController());
	    }
		
	    model.onNotify("/shared/photoinfospots/device/connected", "onDeviceConnected", this);
	    model.onNotify("/shared/photoinfospots/image/selected", "onImageSelected", this);
	    model.onNotify("/shared/photoinfospots/image/spotting", "onCoverflowRequested", this);
	} else {
	    loadImageSelection();
	}
    }
	
    public void loadImageSelection() {
	FSList imagesList = model.getList("@images");
	    
	if (imagesList.size() > 1) {
	    state = "coverflow";
	    screen.get("#exhibition").append("div", "coverflow", new CoverFlowController());
	} else {
	    FsNode node = imagesList.getNodes().get(0);
	    model.setProperty("@imageid", node.getId());
	    loadZoomAndAudio();
	}
    }
	
    public void loadZoomAndAudio() {
	state = "zoomandaudio";
	screen.get("#exhibition").append("div", "zoomandaudio", new ZoomAndAudioController());
    }

    //Mobile device connected
    public void onDeviceConnected(ModelEvent e) {
	System.out.println("Received device is connected!");
	    
	if (state.equals("staticentryscreen")) {
	    screen.get("#staticentryscreen").remove();
	} else if (state.equals("imagerotationentryscreen")) {
	    screen.get("#imagerotationentryscreen").remove();
	}
	loadImageSelection();
    }

    //Coverflow requested returning from zoomandaudio
    public void onCoverflowRequested(ModelEvent e) {
	screen.get("#zoomandaudio").remove();
	loadImageSelection();
    }
	
    public void onImageSelected(ModelEvent e) {
	screen.get("#coverflow").remove();
	FsNode target = e.getTargetFsNode();
	System.out.println("e="+target.asXML());
	model.setProperty("@imageid", target.getId());
	//model.setProperty("/screen/imageurl","http://images1.noterik.com/mupop/"+target.getId()+"-500px.jpg");
	screen.get("#exhibition").append("div", "zoomandaudio", new ZoomAndAudioController());
    }
}
