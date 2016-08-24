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
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.controllers.Html5Controller;
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
	
	public PhotoInfoSpotsController() {}
	
	public void attach(String sel) {
		System.out.println("Entering photoinfospots controller");
		
		selector = sel;
		sharedspace = model.getProperty("/screen/sharedspace");
		screen.loadStyleSheet("photoinfospots/photoinfospots.css");
		
		String path = model.getProperty("/screen/exhibitionpath");

		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			data.put("url",stationnode.getProperty("url"));
			screen.get(selector).parsehtml(data);
			
			// i think we should work with /shared/ name space, Pieter need to talk about this
			model.onPropertiesUpdate(sharedspace+"/station/2","onViewPortChange",this);
		}
	}
	
	public void onViewPortChange(ModelEvent e) {
		System.out.println("MAINSCREEN GOT VIEW EVENT !!!");
		FsPropertySet set = (FsPropertySet)e.target;
		screen.get("#photoinfospots_image").css("height",set.getProperty("scale")+"%");
		screen.get("#photoinfospots_image").css("left",set.getProperty("x")+"px");
		screen.get("#photoinfospots_image").css("top",set.getProperty("y")+"px");
	}
}
