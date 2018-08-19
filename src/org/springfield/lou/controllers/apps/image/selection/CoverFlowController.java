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
package org.springfield.lou.controllers.apps.image.selection;

import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;
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
	int maxtimeoutcount = Integer.MAX_VALUE; //(check every 1sec)
	int maxtnoactiontimeoutcount = 45; //(check every 1sec)
	int selectedItem = 0;
	int totalItems = 0;
	List<FsNode> nodes;

	public CoverFlowController() { }

	public void attach(String sel) {
		selector = sel;

		FsNode exhibitionnode = model.getNode("@exhibition");
		FsNode stationnode = model.getNode("@station");

		if (stationnode!=null) {
			model.setProperty("@contentrole",model.getProperty("@station/contentselect_content"));
			FSList imagesList = model.getList("@images");
			System.out.println("LEN="+imagesList.size());
			nodes = imagesList.getNodes();
			JSONObject data = FSList.ArrayToJSONObject(nodes,"en","url"); 

			data.put("title", stationnode.getSmartProperty("en", "title"));
			data.put("domain", LazyHomer.getExternalIpNumber());
			data.put("jumper", exhibitionnode.getProperty("jumper"));
			data.put("footer_logo", stationnode.getProperty("footer_logo"));

			FsNode language_content = model.getNode("@language_photoexplore_coverflow_screen");
			data.put("logincode", language_content.getSmartProperty("en", "login_code"));
			if (model.getProperty("@station/codeselect") != null) {
			    data.put("code", model.getProperty("@station/codeselect"));
			}
			
    			String applogoleft = model.getProperty("@station/content['contentselect']/applogoright");
    			if (applogoleft!=null && !applogoleft.equals("")) {
    				data.put("applogoleft",applogoleft);
    			}
    			String applogoright = model.getProperty("@station/content['contentselect']/applogoright");
    			if (applogoright!=null && !applogoright.equals("")) {
    				data.put("applogoright",applogoright);
    			}
			
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
		//model.onNotify("/app['timers']", "onTimeoutChecks", this);
		model.onNotify("/shared[timers]/1second","onTimeoutChecks",this); 

		screen.get("#coverflow").on("active","active", this);		
	}

	public void onClientStationEvent(ModelEvent e) {	    
		FsNode message = e.getTargetFsNode();
		String from = message.getId();
		String command = message.getProperty("action");

		if (command == null) {
			return;
		}

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

			resetScreen();

			//Inform app to switch to main app
			FsNode item = nodes.get(selectedItem);
			model.setProperty("/screen/selecteditem", item.getProperty("wantedselect")); 
			model.setProperty("/screen/state","mainapp");

			//Inform clients to switch to main app view
			FsNode m = new FsNode("message",screen.getId());
			m.setProperty("request","mainapp");
			m.setProperty("itemid", item.getProperty("wantedselect"));
			model.notify("@stationevents/fromclient",m);
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
			//screen.get(selector).remove();
			resetScreen();
			timeoutcount=-1; // how do the remove not remove the notify ?
			timeoutnoactioncount=-1; // how do the remove not remove the notify ?
			model.setProperty("/screen/state","apptimeout");
		}
	}

	private void resetScreen() {
		screen.get("#staticentryscreen").remove();	
		screen.get("#imagerotationentryscreen").remove();
		screen.get("#coverflow").remove();
		screen.get("#photoexplore_app").remove();
	}
}
