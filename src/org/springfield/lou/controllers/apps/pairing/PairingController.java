/* 
* StaticEntryScreenController.java
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
package org.springfield.lou.controllers.apps.pairing;

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
	
	public PairingController() { 
		
	}
	
	public void attach(String sel) {
		String selector = sel;
		String hid = model.getProperty("@pairingid");
		if (hid!=null && !hid.equals("")) {
			FsNode hidnode = model.getNode("/domain/mupop/config/hids/hid/"+hid);
			if (hidnode!=null) {		
					String un = hidnode.getProperty("username");
					String ei = hidnode.getProperty("exhibitionid");
					String si = hidnode.getProperty("stationid");
					if (!un.equals("") && !ei.equals("") && !si.equals("")) {
						model.setProperty("/screen/exhibitionpath","/domain/mupop/user/"+un+"/exhibition/"+ei+"/station/"+si);
						model.setProperty("/screen/sharedspace", "/shared/test"); // what does this do should be removed
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
	}
	
	public void onDashboardMessage(ModelEvent e) {
//		System.out.println("DASHBOARDMESSAGE="+e.getTargetFsNode().asXML());
		screen.get("#screen").location("");
	}
	
	public void onHidResponse(ModelEvent e) {
		FsNode node = e.getTargetFsNode();
		String hid = node.getProperty("hidresponse"+code);
		model.setProperty("@pairingid",hid);
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
}
