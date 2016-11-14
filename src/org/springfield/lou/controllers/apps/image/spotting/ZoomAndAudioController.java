/* 
* ZoomAndAudioController.java
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
package org.springfield.lou.controllers.apps.image.spotting;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;

/**
 * ZoomAndAudioController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.image.spotting
 * 
 */
public class ZoomAndAudioController extends Html5Controller {

    private Map<String, HashMap<String, Double>> spots = new HashMap<String, HashMap<String, Double>>();
    private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
    List<FsNode> nodes;
    private Map<String, FsNode> selecteditems = new HashMap<String, FsNode>();

    public ZoomAndAudioController() {}

    public void attach(String sel) {
	selector = sel;
		
	String path = model.getProperty("/screen/exhibitionpath");
		
	FsNode stationnode = model.getNode(path);
	if (stationnode!=null) {
	    FSList fslist = FSListManager.get("/domain/mecanex/app/sceneplayer/scene/blue/element/screen5/sounds",false);
	    nodes = fslist.getNodes();
	    loadImages();
	    JSONObject data = FSList.ArrayToJSONObject(nodes,screen.getLanguageCode(),"mask,url"); 
			
	    String imageurl = model.getProperty("/screen/imageurl");
	    if (imageurl!=null && !imageurl.equals("")) {
		data.put("url",imageurl);
	    } else {
		data.put("url",stationnode.getProperty("url"));
	    }
				
	    screen.get(selector).parsehtml(data);
	}
	model.onPropertiesUpdate("/shared['mupop']/station/"+model.getProperty("@stationid"),"onPositionChange",this);		
    }
	
    public void loadImages() {
	for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
	    FsNode node = (FsNode)iter.next();	
	    String mask = node.getProperty("mask");
	    try {
		URL url = new URL(mask);		
		BufferedImage nimg = ImageIO.read(url);
		images.put(node.getId(), nimg);
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}		
    }

    public void checkOverlays() {
	//loop over every layer
	for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
	    FsNode node = (FsNode)iter.next();	
	    BufferedImage cimg = images.get(node.getId());
	    if (cimg!=null) {
		try {	    	
		    int width = cimg.getWidth();
		    int height = cimg.getHeight();
		    
		    boolean selected = false;
		    Iterator it = spots.entrySet().iterator();
		    //loop over every connected client to allow multi highlighting
		    while (it.hasNext()) {
			Map.Entry<String, HashMap<String, Double>> pair = (Map.Entry<String, HashMap<String, Double>>) it.next();
			double xp = pair.getValue().get("x");
			double yp = pair.getValue().get("y");
			
			//make sure to divide by a double, otherwise you will get a
			// int value when dividing two ints
			double x = (width/100.0)*xp;
			double y = (height/100.0)*yp;
        	        
			int p = cimg.getRGB((int)x,(int)y);
			int a = (p>>24) & 0xff;
			int r = (p>>16) & 0xff;
			int g = (p>>8) & 0xff;
			int b = p & 0xff;

			if (a>200 && g>100) {
			    selecteditems.put(pair.getKey(), node);
			    selected = true;
			    break;
			}
		    }
			
		    if (selected) {
    		    	screen.get("#zoomandaudio_layer"+node.getId()).css("opacity","0.3");
		    } else {
			screen.get("#zoomandaudio_layer"+node.getId()).css("opacity","0");
		    }   
		} catch(Exception e){
		    //e.printStackTrace();
		}
	    }
	}
    }

    public void onPositionChange(ModelEvent e) {
	FsPropertySet set = (FsPropertySet)e.target;
		
	try {
	    double x  = Double.parseDouble(set.getProperty("x"));
	    double y  = Double.parseDouble(set.getProperty("y"));
	    String color = set.getProperty("color");
			
	    String action = set.getProperty("action");
	    String deviceid = set.getProperty("deviceid");
	    
	    long currentTime = new Date().getTime();
	    
	    //check for a new spot
	    if (!spots.containsKey(deviceid)) {
		screen.get("#zoomandaudio_holder").append("<div class='zoomandaudio_spot' id='zoomandaudio_spot_"+deviceid+"'></div>");
	    }
	    
	    //update last seen
	    HashMap<String, Double> spot = new HashMap<String, Double>();
	    spot.put("lastseen", (double) new Date().getTime());
	    spot.put("x", x);
	    spot.put("y", y);
	    spots.put(deviceid, spot);
	    
	    //TODO: better to to this timeout based instead upon a move
	    Iterator it = spots.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry<String, HashMap<String, Double>> pair = (Map.Entry<String, HashMap<String, Double>>) it.next();
		if (pair.getValue().get("lastseen") + 60000 < currentTime) {
		    screen.get("#zoomandaudio_spot_"+pair.getKey()).remove();
		    it.remove();
		    selecteditems.remove(deviceid);
		}
	    }
	    
	    //check overlays after updating connected client list
	    checkOverlays();
	    
	    if (action.equals("move")) { // its a move event so lets just move the dot
		screen.get("#zoomandaudio_spot_"+deviceid).css("background-color", "#"+color);
		screen.get("#zoomandaudio_spot_"+deviceid).translate(""+x+"%",""+y+"%");
	    } else if (action.equals("up")) {
		if (selecteditems.get(deviceid) != null) {
		    System.out.println("Item is selected notify");
		    FsNode message = new FsNode("message","1");
		    message.setProperty("action", "startaudio");
		    message.setProperty("url", selecteditems.get(deviceid).getProperty("url"));
		    message.setProperty("deviceid", deviceid);
		    model.notify("/shared['mupop']/station/"+model.getProperty("@stationid"),message);
		}
	    }
	} catch(Exception error) {
	    System.out.println("PhotoInfoSpots - count not move stop of play sound");
	}	
    }
}
