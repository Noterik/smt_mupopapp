/* 
* StaticEntryScreenController.java
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
package org.springfield.lou.controllers.apps.entryscreen;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;

/**
 * StaticEntryScreenController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.entryscreen
 * 
 */
public class StaticEntryScreenController extends Html5Controller {

    public StaticEntryScreenController() { }
	
    public void attach(String sel) {
    	String selector = sel;

    	String path = model.getProperty("/screen/exhibitionpath");
		
    	FsNode stationnode = model.getNode(path);
    	FsNode exhibitionnode = model.getNode("@exhibition");
	
    	if (stationnode!=null) {
    		JSONObject data = new JSONObject();
	    
    		//check if a specific entry screen image is configured
    		String entryScreen = null;
    		//if (entryScreen == null || entryScreen.equals("")) {
    			//load first image
    			model.setDebug(true);
    			model.setProperty("@contentrole",model.getProperty("@station/waitscreen_content"));
    			FSList imagesList = model.getList("@images");
    			model.setDebug(false);
		    
    			if (imagesList.size() > 0) {
    				FsNode first = imagesList.getNodes().get(0);
    				entryScreen = first.getProperty("url");
    			}
    		//}
	    
    		data.put("entryimageurl", entryScreen);
    		//TODO: get a default language for the mainscreen?
    		data.put("title", stationnode.getSmartProperty("en", "title"));
    		data.put("jumper", exhibitionnode.getProperty("jumper"));
    		screen.get(selector).render(data);
    		screen.get(selector).loadScript(this);
	    
    		JSONObject d = new JSONObject();	
    		d.put("command","init");
    		screen.get(selector).update(d);
    	}
    }
}
