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

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;
import org.springfield.lou.controllers.ExhibitionMemberManager;

/**
 * CoverFlowController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.image.selection
 * 
 */
public class SelectionMapController extends Html5Controller {

	private long activeItem;
	Random rnd = new Random(System.currentTimeMillis());
	
	int timeout_toselect = 0;
	int timeout_tomove = 8;
	String master = null;

	
	int maxtimeoutcount = Integer.MAX_VALUE; //(check every 1sec)
	int maxtnoactiontimeoutcount = 1045; //(check every 1sec)
	FsNode selectedItem = null;
	List<FsNode> nodes;
	private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private Map<String, FsNode> selecteditems = new HashMap<String, FsNode>();
	double spotx = -1;
	double spoty = -1;

	public SelectionMapController() { }

	public void attach(String sel) {
		selector = sel;
		FsNode exhibitionnode = model.getNode("@exhibition");
		FsNode stationnode = model.getNode("@station");

		if (stationnode!=null) {
			model.setProperty("@contentrole",model.getProperty("@station/contentselect_content"));
			
			nodes = model.getList("@station/content/mainapp/item/one/mask").getNodes();
			JSONObject data = FSList.ArrayToJSONObject(nodes,screen.getLanguageCode(), "maskurl,audiourl");
			loadMasks();
			
			//List<FsNode> imagesNode = model.getList("@station/content/mainapp/item/one/image").getNodes();
			
			data.put("url",model.getProperty("@station/content/mainapp/item/one/url"));
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
			
    		data.put("x", "50");
    		data.put("y","50");
    		data.put("color","red");
    			
			screen.get(selector).render(data);			
			screen.get(selector).loadScript(this);

			JSONObject d = new JSONObject();
			d.put("command", "init");
			screen.get(selector).update(d);
		}
		pickMaster();
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

	//	timeoutnoactioncount = 0;

		if (command.equals("spotmove")) {
			//System.out.println("MOVE "+e.getTargetFsNode().asXML());
			onPositionChange(e.getTargetFsNode());
			
			
		} else if (command.equals("enter")) {
		}
	}

	public void active(Screen s, JSONObject data) {
		activeItem = (Long) data.get("item");
		model.setProperty("@imageid", String.valueOf(activeItem));
	}

	public void onTimeoutChecks(ModelEvent e) {
		pickMaster();
		// time to move we give the user, 5 seconds to start picking if not we pick
		if (timeout_toselect!=0) {
			timeout_toselect=timeout_toselect-1;
			if (selectedItem!=null && timeout_toselect==0) {
	    		screen.get("#selectionmapspot").css("background-color","blue");	
	    		
				//Screen client = ApplicationManager.getScreenByFullid(from);
				//client.getModel().setProperty("/screen/state","mainapp");
				//model.setProperty("@fromid", from);

				resetScreen();

				//Inform app to switch to main app
				//FsNode item = nodes.get(selectedItem);
				model.setProperty("/screen/selecteditem", selectedItem.getId()); 
				model.setProperty("/screen/state","mainapp");

				//Inform clients to switch to main app view
				FsNode m = new FsNode("message",screen.getId());
				m.setProperty("request","mainapp");
				m.setProperty("itemid", selectedItem.getId());
				model.notify("@stationevents/fromclient",m);
				
				screen.get(selector).remove();
			}
		}
		if (timeout_tomove!=0 && selectedItem==null) {
			timeout_tomove=timeout_tomove-1;
			if (timeout_tomove==0) {
				selectedItem = moveToRandom();
				// move the pointer to it
	    		screen.get("#selectionmapspot").css("left",""+(spotx)+"%");
	    		screen.get("#selectionmapspot").css("top",""+(spoty)+"%");
		    	screen.get("#selectionmapmask_"+selectedItem.getId()).css("opacity","0.3");
				timeout_toselect = 5;
			}

		}
	}

	private void resetScreen() {
		screen.get("#staticentryscreen").remove();	
		screen.get("#imagerotationentryscreen").remove();
		screen.get("#coverflow").remove();
		screen.get("#photoexplore_app").remove();
	}
	
	public void loadMasks() {
		for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
			FsNode node = (FsNode) iter.next();
			String mask = node.getProperty("maskurl");
			try {
				URL url = new URL(mask);
				BufferedImage nimg = ImageIO.read(url);
				images.put(node.getId(), nimg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onPositionChange(FsNode msg) {
		try {
			spotx = Double.parseDouble(msg.getProperty("x"));
			spoty = Double.parseDouble(msg.getProperty("y"));
			String color = msg.getProperty("color");
			
    		screen.get("#selectionmapspot").css("left",""+(spotx)+"%");
    		screen.get("#selectionmapspot").css("top",""+(spoty)+"%");
    		screen.get("#selectionmapspot").css("background-color","red");	
			
			
			String action = msg.getProperty("action");
			String deviceid = msg.getProperty("deviceid");
			String language = msg.getProperty("language");

			long currentTime = new Date().getTime();

			// check overlays after updating connected client list
			FsNode selectednode = checkOverlays();
			if (selectednode!=null) {
				if (selectedItem==null) {
					selectedItem = selectednode;
					// lets turn it on for sure
		    		screen.get("#selectionmapmask_"+selectednode.getId()).css("opacity","0.3");
					timeout_toselect = 5;
				} else if (selectedItem==selectednode) {
					// lets do nothing but in future we might make  'lope' move
				} else {
					// lets turn old one off and new one on.
		    		screen.get("#selectionmapmask_"+selectedItem.getId()).css("opacity","0");
		    		screen.get("#selectionmapmask_"+selectednode.getId()).css("opacity","0.3");
		    		selectedItem=selectednode;
					timeout_toselect = 5;
				}
			} else {
				if (selectedItem!=null) {
					screen.get("#selectionmapmask_"+selectedItem.getId()).css("opacity","0");
				}
				selectedItem = null;
				timeout_tomove  = 8;
			}
		} catch(Exception er1) {
			
		}

	}
	

	public FsNode checkOverlays() {
		// new check, so clear old cache
		selecteditems.clear();
		// loop over every layer
		for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
			FsNode node = (FsNode) iter.next();
			BufferedImage cimg = images.get(node.getId());
			if (cimg != null) {
				try {
					int width = cimg.getWidth();
					int height = cimg.getHeight();
					double x = (width / 100.0) * spotx;
					double y = (height / 100.0) * spoty;

					int p = cimg.getRGB((int) x, (int) y);
					int a = (p >> 24) & 0xff;
					int r = (p >> 16) & 0xff;
					int g = (p >> 8) & 0xff;
					int b = p & 0xff;

					if (a > 200 && g > 100) {
						return node;
					}
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private FsNode moveToRandom() {
		int rx = rnd.nextInt(100);
		int ry = rnd.nextInt(100);
		int maxtry = 200;
		spotx = rx;
		spoty = ry;
		FsNode selectednode=null;
		while (selectednode==null) {
			maxtry = maxtry - 1;
			if (maxtry<1) return null;
			//System.out.println("X="+spotx+" Y="+spoty);
			selectednode = checkOverlays();
			if (selectednode!=null) {
				//System.out.println("RANDOM SELECTED NODE !!");
				return selectednode;
			}
			
			rx = rnd.nextInt(100);
			ry = rnd.nextInt(100);
			spotx = rx;
			spoty = ry;
		}
		return null;
	}
	
	private void pickMaster() {
    	FsNode message = new FsNode("message",screen.getId());
    	if (master!=null) {
			message.setProperty("master",master);
    		model.notify("@selectionmapevent",message);
    	} else {
    		FSList list = ExhibitionMemberManager.getActiveMembers(screen,300);
    		if (list!=null && list.size()>0) {
    			List<FsNode> nodes = list.getNodes();
    			int rp = rnd.nextInt(list.size());
    			FsNode node = nodes.get(rp);
    			message.setProperty("master",node.getId());
    			master = node.getId();
        		model.notify("@selectionmapevent",message);
    		}

    	}
	}
	

}
