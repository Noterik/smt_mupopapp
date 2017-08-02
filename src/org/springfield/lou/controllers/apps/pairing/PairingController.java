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
package org.springfield.lou.controllers.apps.pairing;

import java.util.Date;
import java.util.Random;

import javax.jws.WebParam.Mode;

import org.json.simple.JSONObject;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.ExhibitionController;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;

/**
 * StaticEntryScreenController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.entryscreen
 * 
 */
public class PairingController extends Html5Controller {

	String code; 
	String hid;
	String si;
	String ei;
	String un;
	
	public PairingController() { 
		
	}
	
	public void attach(String sel) {
		String selector = sel;
		hid = model.getProperty("@pairingid");
		if (hid!=null && !hid.equals("")) {
			FsNode hidnode = model.getNode("/domain/mupop/config/hids/hid/"+hid);
			if (hidnode!=null) {		
					un = hidnode.getProperty("username");
					ei = hidnode.getProperty("exhibitionid");
					si = hidnode.getProperty("stationid");
					if (!un.equals("") && !ei.equals("") && !si.equals("")) {
						model.setProperty("@username", un);
						model.setProperty("@exhibitionid", ei);
						model.setProperty("@stationid", si);
						screen.get("#screen").append("div", "exhibition",new ExhibitionController());
					} else {
						JSONObject data = new JSONObject();
						data.put("unassignedmode","true");
						data.put("hid",hidnode.getId());
						screen.get(selector).render(data);	
					}
					model.onNotify("/shared['mupop']/hid['"+hid+"']","onDashboardMessage",this);
			} else {
				JSONObject data = new JSONObject();
				data.put("nohidnode","true");
				data.put("hid",hid);
				screen.get(selector).render(data);	
				System.out.println("NO HID NODE FOUND !");
			}
		} else {
			JSONObject data = new JSONObject();
			data.put("pairingmode","true");
			code = getRandomCode();
			data.put("pairingcode",code);
			screen.get(selector).render(data);	
			model.onPropertyUpdate("/shared/mupop/hidresponse"+code,"onHidResponse",this);
			model.setProperty("/shared/mupop/hidrequest",code);
		}
		model.onNotify("/app['timers']", "onAliveCheck", this);
	}
	
	public void onDashboardMessage(ModelEvent e) {
		System.out.println("DASHBOARDMESSAGE="+e.getTargetFsNode().asXML());
		screen.get("#screen").location("");
	}
	
	public void onHidResponse(ModelEvent e) {
		FsNode node = e.getTargetFsNode();
		String hid = node.getProperty("hidresponse"+code);
		model.setProperty("@pairingid",hid);
		screen.get("#screen").location("");
	}
	
	private String getRandomCode() {
		String options = "0123456789ABCDE";
	    int len = options.length();
	    Random r = new Random();
	    String result = "";
	    for (int i = 0; i < 4; i++) {
	       result+=options.charAt(r.nextInt(len));
	    }
	    return result;
	}
	
	public void onAliveCheck(ModelEvent e) {
		//System.out.println("ALIVE CHECK !!!"+e.getTargetFsNode().asXML());
		FsNode node = e.getTargetFsNode();
		String message = node.getProperty("message");
		if (message.equals("10") && hid!=null) {
			FsNode messagenode = new FsNode("notify","1");
			messagenode.setProperty("message", hid);
			if (un!=null) messagenode.setProperty("username", un);
			if (si!=null) messagenode.setProperty("stationid", si);
			if (ei!=null) messagenode.setProperty("exhibitionid", ei);
			//model.notify("/shared['mupop']/hids[alive]",messagenode);	
	    	FsNode hidalive = model.getNode("@hidsalive/hid/"+hid); // auto create because of bug !
	    	hidalive.setProperty("lastseen",""+new Date().getTime());
	    	hidalive.setProperty("stationid",si);
	    	hidalive.setProperty("exhibitionid",ei);
	    	hidalive.setProperty("username",un);
			//System.out.println("ALIVE="+hidalive.asXML());
	    	
			model.notify("/shared['mupop']/hids[alive]",messagenode);
		}
	}
}
