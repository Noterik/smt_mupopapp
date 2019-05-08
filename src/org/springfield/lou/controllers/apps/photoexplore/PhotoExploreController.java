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
package org.springfield.lou.controllers.apps.photoexplore;

import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;

public class PhotoExploreController extends Html5Controller {

	int timeoutcount = 0;
	int timeoutnoactioncount = 0;
	int maxtimeoutcount = 120; //(check every 1sec)
	int maxtnoactiontimeoutcount = 120; //(check every 1sec)
	String userincontrol;

	public PhotoExploreController() { }

	public void attach(String sel) {
		selector = sel;
		timeoutcount = 0;
		screen.loadStyleSheet("photoexplore/photoexplore.css");

		userincontrol = model.getProperty("@fromid");


		FsNode exhibitionnode = model.getNode("@exhibition");
		FsNode stationnode = model.getNode("@station");

		if (stationnode!=null) {
			model.setProperty("@contentrole","mainapp");
			String itemname=model.getProperty("/screen/selecteditem");
			if (itemname==null || itemname.equals("")) {
				itemname="one";
			}
			model.setProperty("@itemid",itemname);
			System.out.println("SELECTED ITEM="+itemname);
			FSList imagesList = model.getList("@itemimages");
			System.out.println("Found "+imagesList.size()+" images");
			String renderoption = model.getProperty("@item/renderoption");
			if (renderoption!=null && !renderoption.equals("") && !renderoption.equals("none")) {
				imagesList = rewriteUrls(imagesList,renderoption);
			}

			List<FsNode> nodes = imagesList.getNodes();
			JSONObject data = FSList.ArrayToJSONObject(nodes,"en","url"); 
			data.put("domain", LazyHomer.getExternalIpNumber());
			data.put("jumper", exhibitionnode.getProperty("jumper"));
			data.put("footer_logo", stationnode.getProperty("footer_logo"));
			
			FsNode language_content = model.getNode("@language_photoexplore_coverflow_screen");
			data.put("logincode", language_content.getSmartProperty("en", "login_code"));
			data.put("code", model.getProperty("@station/codeselect"));

			String applogoleft = model.getProperty("@station/content['contentselect']/applogoleft");
			if (applogoleft!=null && !applogoleft.equals("")) {
				data.put("applogoleft",applogoleft);
			}
			String applogoright = model.getProperty("@station/content['contentselect']/applogoright");
			if (applogoright!=null && !applogoright.equals("")) {
				data.put("applogoright",applogoright);
			}

			screen.get(selector).parsehtml(data);
			screen.get(selector).loadScript(this);
			
			// color hack
			String themecolor1 = model.getProperty("@station/content['contentselect']/themecolor1");
			if (themecolor1!=null && !themecolor1.equals("")) {
				screen.get("#bottom_left").css("background-color",themecolor1);
				screen.get("#bottom_right").css("background-color",themecolor1);
			}


			// = new JSONObject();	
			JSONObject d = FSList.ArrayToJSONObject(nodes,"en","url");
			d.put("command","init");
			screen.get(selector).update(d);

			// i think we should work with /shared/ name space, Pieter need to talk about this
			model.onPropertiesUpdate("@stationevents","onViewPortChange",this);
		}

		model.onNotify("@stationevents/fromclient","onClientStationEvent",this);
		//model.onNotify("/app['timers']", "onTimeoutChecks", this);
		model.onNotify("/shared[timers]/1second","onTimeoutChecks",this); 
	}
	
	public FSList rewriteUrls(FSList nodes,String renderoption) {
		for(Iterator<FsNode> iter = nodes.getNodes().iterator() ; iter.hasNext(); ) {
			FsNode node = (FsNode)iter.next();	
			String oldurl = node.getProperty("url");
			if (oldurl.startsWith("http://")) {
				node.setProperty("url","http://"+LazyHomer.getExternalIpNumber()+"/edna/external/"+oldurl.substring(7)+"?script="+renderoption);
			} else if (oldurl.startsWith("https://")) {
				node.setProperty("url","http://"+LazyHomer.getExternalIpNumber()+"/edna/external/"+oldurl.substring(8)+"?script="+renderoption);
			}
		//	System.out.println("URL REWRITE="+node.getProperty("url"));
		}
		return nodes;
	}

	public void onClientStationEvent(ModelEvent e) {	    
		FsNode message = e.getTargetFsNode();
		String from = message.getId();
		String command = message.getProperty("action");
		if (command==null) return;
		timeoutnoactioncount = 0;
		if (command.equals("leftonzoom")) {
			JSONObject d = new JSONObject();
			d.put("command", "prev");
			screen.get(selector).update(d);
		} else if (command.equals("rightonzoom")) {
			JSONObject d = new JSONObject();
			d.put("command", "next");
			screen.get(selector).update(d);
		} else if (command.equals("scale")) {        	
			//String transformationCss = "scale("+(String) message.getProperty("value")+")";
			//String originCss = message.getProperty("originX") + "% " + message.getProperty("originY") + "%";

			JSONObject d = new JSONObject();
			d.put("command", "scale");
			d.put("value", message.getProperty("value"));
			d.put("originX", message.getProperty("originX"));
			d.put("originY", message.getProperty("originY"));  
			screen.get(selector).update(d);

			// screen.get("#presenterarea").css("transform", transformationCss);
			// screen.get("#presenterarea").css("transform-origin", originCss);
		}
	}

	public void onViewPortChange(ModelEvent e) {
		FsPropertySet set = (FsPropertySet)e.target;
		screen.get("#photoexplore_image").css("height",set.getProperty("scale")+"%");
		screen.get("#photoexplore_image").css("left",set.getProperty("x")+"px");
		screen.get("#photoexplore_image").css("top",set.getProperty("y")+"px");
		timeoutnoactioncount=0;
	}

	public void onTimeoutChecks(ModelEvent e) {
		if (timeoutcount!=-1) {
			timeoutcount++;
			timeoutnoactioncount++;
		}

		if (timeoutcount>maxtimeoutcount || timeoutnoactioncount>maxtnoactiontimeoutcount) {
			System.out.println("APP TIMEOUT RESET WANTED");
			model.setProperty("@fromid",userincontrol);
			//screen.remove(selector);
			screen.get(selector).remove();
			timeoutcount=-1; // how do the remove not remove the notify ?
			timeoutnoactioncount=-1; // how do the remove not remove the notify ?
			
			String type = model.getProperty("@station/contentselect");
	    		if (type==null || type.equals("") || type.equals("none")) {
				model.setProperty("/screen/state","apptimeout");
				//Inform clients to switch to main app view
				FsNode m = new FsNode("message",screen.getId());
				m.setProperty("request","mainapp");
			//	m.setProperty("itemid", item.getProperty("wantedselect"));
				model.notify("@stationevents/fromclient",m);
			} else {
				model.setProperty("/screen/state","contentselectforce");
			}

			FsNode message = new FsNode("message",screen.getId());
			message.setProperty("request","contentselect");
			model.notify("@stationevents/fromclient",message);
		}
	}	 
}
