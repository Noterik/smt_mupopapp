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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

	public ZoomAndAudioController() {
		
	}
	
	/*
	public ZoomAndAudioController(String item) {
		System.out.println("Loading item "+item);
	}
	*/
	
	public void attach(String sel) {
		selector = sel;
		
		String path = model.getProperty("/screen/exhibitionpath");
		
		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
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
	
	public void onPositionChange(ModelEvent e) {
		FsPropertySet set = (FsPropertySet)e.target;
		
		try {
		 float x  = Float.parseFloat(set.getProperty("x"));
		 float y  = Float.parseFloat(set.getProperty("y"));

		 String action = set.getProperty("action");
		 if (action.equals("move")) { // its a move event so lets just move the dot
			String url = getAudio(x, y);
			screen.get("#zoomandaudio_spot").css("background-color","purple");
			 if (url!=null) {
				screen.get("#zoomandaudio_spot").css("background-color","blue");
			 }
			// screen.get("#zoomandaudio_holder").html("<div id=\"zoomandaudio_outer\" style=\"top: "+(y-5)+"%;left:"+(x-5)+"%;"+backgr+"\"><div id=\"zoomandaudio_inner\" style=\"top: "+(y-5)+"%;left:"+(x-5)+"%;"+backgr+"\"></div></div>");
			//screen.get("#zoomandaudio_holder").html("<div id=\"zoomandaudio_spot\" style=\"top: "+(y-5)+"%;left:"+(x-5)+"%;"+backgr+"\"></div>");

			// screen.get("#zoomandaudio_spot").css("top",""+(y-5)+"%");
			//screen.get("#zoomandaudio_spot").css("left",""+(x-5)+"%");
			// screen.get("#zoomandaudio_spot").css("transform","translateX("+(x-5)+"px)");
			// screen.get("#zoomandaudio_spot").css("transform","translate("+(x-5)+"px,200px)");
			screen.get("#zoomandaudio_spot").translate(""+x+"%",""+y+"%");
		 } else if (action.equals("up")) {
				String url = getAudio(x, y);
				 if (url!=null) {
						FsNode message = new FsNode("message","1");
						message.setProperty("action", "startaudio");
						message.setProperty("url", url);
						model.notify("/shared['mupop']/station/"+model.getProperty("@stationid"),message);
				}
		 }

		 
		} catch(Exception error) {
			System.out.println("PhotoInfoSpots - count not move stop of play sound");
		}
	}
	
	private String getAudio(float x,float y) {
		FSList fslist = FSListManager.get("/domain/mecanex/app/sceneplayer/scene/blue/element/screen5/sounds",true);
		List<FsNode> nodes = fslist.getNodes();
		if (nodes!=null) {
			for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
				FsNode node = (FsNode)iter.next();	
				String url = node.getProperty("url");
				try {
					float ox = Float.parseFloat(node.getProperty("x"));
					float oy = Float.parseFloat(node.getProperty("y"));
					float ow = Float.parseFloat(node.getProperty("width"))/2; // 
					float oh = Float.parseFloat(node.getProperty("height"))/2;
					if (x>(ox-ow) && x<(ox+ow)) { // within x range
						if (y>(oy-oh) && y<(oy+oh)) { // within y range
							return url;
						}
					}	
				} catch(Exception e) {
					System.out.println("Error parsing audioimage data");
				}
			}
		}
		return null;
	}
}
