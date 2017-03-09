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
package org.springfield.lou.controllers.apps.entryscreen;

import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;

/**
 * ImageRotationEntryScreenController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.entryscreen
 * 
 */
public class ImageRotationEntryScreenController extends Html5Controller{

    public ImageRotationEntryScreenController() { }
	
    public void attach(String sel) {
	String selector = sel;

		
	FsNode stationnode = model.getNode("@station");
	FsNode exhibitionnode = model.getNode("@exhibition");
	
	if (stationnode!=null) {
		model.setProperty("@contentrole",model.getProperty("@station/waitscreen_content"));
		FSList imagesList = model.getList("@images");
		System.out.println("LEN="+imagesList.size());
	  //  FSList imagesList = model.getList("@images");
	    List<FsNode> nodes = imagesList.getNodes();
	    JSONObject data = FSList.ArrayToJSONObject(nodes,"en","url"); 
	    
	    //TODO: get a default language for the mainscreen?
	    data.put("title", stationnode.getSmartProperty("en", "title"));
	    data.put("jumper", exhibitionnode.getProperty("jumper"));
		data.put("domain", LazyHomer.getExternalIpNumber());
		data.put("name", model.getProperty("@station/name"));
		data.put("labelid", model.getProperty("@station/labelid"));
	    String stationselect = model.getProperty("@exhibition/stationselect");
	    if (stationselect!=null && !stationselect.equals("")&& !stationselect.equals("none")) {
	    	data.put("stationselect","true");
	    }
	    screen.get(selector).render(data);
	    screen.get(selector).loadScript(this);
	    
	    JSONObject d = new JSONObject();	
	    d.put("command","init");
	    screen.get(selector).update(d);
	}
    }
	
}
