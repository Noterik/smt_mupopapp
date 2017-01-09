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
    int timeoutcount = 0;
    int timeoutnoactioncount = 0;
    int maxtimeoutcount = 6; //(check every 10sec)
    int maxtnoactiontimeoutcount = 2; //(check every 10sec)
    int selectedItem = 0;
    int totalItems = 0;
    List<FsNode> nodes;
    
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
	    nodes = imagesList.getNodes();
	    JSONObject data = FSList.ArrayToJSONObject(nodes,"en","url"); 
	    
	    data.put("title", stationnode.getSmartProperty("en", "title"));
	    data.put("jumper", exhibitionnode.getProperty("jumper"));	
	    
	    FsNode language_content = model.getNode("@language_photoexplore_coverflow_screen");
	    data.put("logincode", language_content.getSmartProperty("en", "login_code"));
	    data.put("code", model.getProperty("@station/codeselect"));
	    
	    screen.get(selector).render(data);
	    screen.get(selector).loadScript(this);
			
	    JSONObject d = new JSONObject();	
	    d.put("command","numItems");
	    d.put("items", nodes.size());
	    screen.get(selector).update(d);
	    
	    totalItems = nodes.size();
	    selectedItem = (int) (Math.round(nodes.size() / 2.0) - 1);
	}
		
	model.onNotify("@stationevents/fromclient","onClientStationEvent",this);
	model.onNotify("/app['timers']", "onTimeoutChecks", this);
		
	screen.get("#coverflow").on("active","active", this);		
    }
	
    public void onClientStationEvent(ModelEvent e) {	    
    	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String command = message.getProperty("action");
    	
    	timeoutnoactioncount = 0;
    	
    	if (command.equals("left")) {
    	    selectedItem = selectedItem > 0 ? selectedItem-1 : 0;
    	    JSONObject d = new JSONObject();
    	    d.put("command", "prev");
    	    screen.get("#coverflow").update(d);
    	} else if (command.equals("right")) {
    	    selectedItem = selectedItem >= totalItems-1 ? totalItems-1 : selectedItem+1;
    	    JSONObject d = new JSONObject();
    	    d.put("command", "next");
    	    screen.get("#coverflow").update(d);
    	} else if (command.equals("enter")) {
    	    System.out.println("Selected item is "+selectedItem);
    	    Screen client = ApplicationManager.getScreenByFullid(from);
    	    client.getModel().setProperty("/screen/state","mainapp");
    	    model.setProperty("@fromid", from);

    	    FsNode item = nodes.get(selectedItem);
    	    model.setProperty("/screen/selecteditem", item.getProperty("wantedselect")); 
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
    
    public void onTimeoutChecks(ModelEvent e) {
	//System.out.println("TIME OUT CHECKS");
	if (timeoutcount!=-1) {
	    timeoutcount++;
	    timeoutnoactioncount++;
	}
	//System.out.println("TIME OUT CHECKS 2 : "+timeoutcount+" "+timeoutnoactioncount);
	if (timeoutcount > maxtimeoutcount || timeoutnoactioncount > maxtnoactiontimeoutcount) {
	    System.out.println("Coverflow time out reset because "+timeoutcount+" > "+maxtimeoutcount+" or "+timeoutnoactioncount+" > "+maxtnoactiontimeoutcount);
	    //model.setProperty("@fromid",userincontrol);
	    screen.get(selector).remove();
	    timeoutcount=-1; // how do the remove not remove the notify ?
	    timeoutnoactioncount=-1; // how do the remove not remove the notify ?
	    model.setProperty("/screen/state","apptimeout");
	}
    }
}
