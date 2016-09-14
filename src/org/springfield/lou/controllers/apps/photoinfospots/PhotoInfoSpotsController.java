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

	String sharedspace;
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
		
		//TODO: load config for this exhibition from FS, is there an entry screen, etc
		screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
 		//screen.get("#exhibition").append("div","imagerotationentryscreen", new ImageRotationEntryScreenController());
		
		sharedspace = model.getProperty("/screen/sharedspace");

		model.onNotify("/screen/tst", "onDeviceConnected", this);
		model.onNotify("/screen/photoinfospots/image/selected", "onImageSelected", this);
	}
	
	public void onDeviceConnected(ModelEvent e) {
		if (state.equals("waiting")) {
			screen.get("#staticentryscreen").remove();

			//TODO: load from config what needs to be loaded
			state = "coverflow";
			screen.get("#exhibition").append("div", "coverflow", new CoverFlowController());
		} 
	}
	
	public void onImageSelected(ModelEvent e) {
		screen.get("#coverflow").remove();
		
		System.out.println("Adding image zoom and audio");
		
		FsNode target = e.getTargetFsNode();		
		screen.get("#exhibition").append("div", "zoomandaudio", new ZoomAndAudioController(target.getId()));
	}
}
