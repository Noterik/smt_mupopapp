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
package org.springfield.lou.controllers.apps.trivia;

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
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;

public class TriviaController extends Html5Controller {
	public TriviaController() {}
	private int timeout=0;


	public void attach(String sel) {
		model.setProperty("@contentrole","mainapp");
		selector = sel;
		fillPage();
		model.onNotify("/shared[timers]/1second","on1SecondTimer",this); 
	}
	
	private void fillPage() {
		JSONObject data = new JSONObject();
		FsNode item = model.getNode("@item");
		if (item==null) {
			item=getNextItem();
		}
		
		String timeouts = item.getProperty("timeout");
		timeout = 10;
		try {
			timeout = Integer.parseInt(timeouts);
		} catch(Exception e) {
		}
		System.out.println("TIMEOUT="+timeout);
		data.put("imageurl",item.getProperty("imageurl"));
		screen.get(selector).render(data);
		FsNode msgnode=new FsNode("msgnode","1");
		msgnode.setProperty("itemid",model.getProperty("@itemid"));
		model.notify("@appstate",msgnode);
	}

	public void on1SecondTimer(ModelEvent e) {
		if (timeout>0) {
			timeout = timeout -1;
		} else {
			getNextItem();
			fillPage();
		}
		System.out.println("1 SEC TIMER "+timeout);
	}
	
	private FsNode getNextItem() {
		System.out.println("GET NEXT ITEM CALLED");
		String selecteditem = model.getProperty("@selecteditem");
		FsNode item = model.getNode("@item");
		if (item==null) {
			model.setProperty("@itemid","1");
			item = model.getNode("@item");
		} else {
			// should pick a random one for now ill feed it number 2 !
			model.setProperty("@itemid","2");
			item = model.getNode("@item");
		}
		return item;
	}


}
