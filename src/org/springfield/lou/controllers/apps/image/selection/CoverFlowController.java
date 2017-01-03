/* 
* CoverFlowController.java
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
package org.springfield.lou.controllers.apps.image.selection;

import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

/**
 * CoverFlowController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.image.selection
 * 
 */
public class CoverFlowController extends Html5Controller {

    private long activeItem;
	
    public CoverFlowController() { }
	
    public void attach(String sel) {
	selector = sel;
	
	String path = model.getProperty("/screen/exhibitionpath");
		
	FsNode exhibitionnode = model.getNode("@exhibition");
	FsNode stationnode = model.getNode(path);
	
	if (stationnode!=null) {
		model.setProperty("@contentrole",model.getProperty("@station/contentselect_content"));
		FSList imagesList = model.getList("@images");
		System.out.println("LEN="+imagesList.size());
	    List<FsNode> nodes = imagesList.getNodes();
	    JSONObject data = FSList.ArrayToJSONObject(nodes,"en","url"); 
	    
	    data.put("title", stationnode.getSmartProperty("en", "title"));
	    data.put("jumper", exhibitionnode.getProperty("jumper"));	
	    
	    screen.get(selector).render(data);
	    screen.get(selector).loadScript(this);
			
	    JSONObject d = new JSONObject();	
	    d.put("command","numItems");
	    d.put("items", nodes.size());
	    screen.get(selector).update(d);
	}
		
	model.onNotify("@stationevents/fromclient","onClientStationEvent",this);
	//model.onNotify("@photoinfospots", "coverFlow", this);
		
	screen.get("#coverflow").on("active","active", this);		
    }
	
    public void onClientStationEvent(ModelEvent e) {	    
    	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String command = message.getProperty("action");
    	
    	if (command.equals("left")) {
    		JSONObject d = new JSONObject();
    		d.put("command", "prev");
    		screen.get("#coverflow").update(d);
    	} else if (command.equals("right")) {
    		JSONObject d = new JSONObject();
    		d.put("command", "next");
    		screen.get("#coverflow").update(d);
    	} else if (command.equals("enter")) {
			Screen client = ApplicationManager.getScreenByFullid(from);
			client.getModel().setProperty("/screen/state","mainapp");
			model.setProperty("@fromid", from);
			model.setProperty("/screen/state","mainapp");
			
    		if (!model.getProperty("@photoinfospots/vars/state").equals("zoomandaudio")) {
    			//model.notify("@photoinfospots/image/selected", new FsNode("item", String.valueOf(activeItem)));
    		}
    	}
    }

    public void active(Screen s, JSONObject data) {
	activeItem = (Long) data.get("item");
	model.setProperty("@imageid", String.valueOf(activeItem));
    }
}
