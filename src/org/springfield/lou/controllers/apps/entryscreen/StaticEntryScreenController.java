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
package org.springfield.lou.controllers.apps.entryscreen;

import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;

/**
 * StaticEntryScreenController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.controllers.apps.entryscreen
 * 
 */
public class StaticEntryScreenController extends Html5Controller {
	

    public StaticEntryScreenController() { }
	
    public void attach(String sel) {
    	String selector = sel;

    	String path = model.getProperty("/screen/exhibitionpath");
		
    	FsNode stationnode = model.getNode(path);
    	FsNode exhibitionnode = model.getNode("@exhibition");
    	if (stationnode!=null) {
    	    JSONObject data = new JSONObject();
	    
    	    //check if a specific entry screen image is configured
    	    String entryScreen = null;
    	    //load first image
    	    model.setDebug(true);
    	    model.setProperty("@contentrole",model.getProperty("@station/waitscreen_content"));
    	    FSList imagesList = model.getList("@images");
    	    model.setDebug(false);
		    
    	    if (imagesList.size() > 0) {
    		FsNode first = imagesList.getNodes().get(0);
    		entryScreen = first.getProperty("url");
    	    }
    	    
    	    //language translations
    	    String language = model.getProperty("@exhibition/language");
    	    data.put("language", language);
    	    
    	    FsNode language_content = model.getNode("@language_static_entry_screen");
    	    System.out.println("LANGSEL="+language);
    	    System.out.println("MY NODE LANG="+language_content.asXML());
    	    data.put("goto", language_content.getSmartProperty(language, "goto"));
    	    data.put("andenter", language_content.getSmartProperty(language, "andenter"));
    	    data.put("tocontrolthescreen", language_content.getSmartProperty(language, "tocontrolthescreen"));
    	    data.put("tocontrolthescreen2", language_content.getSmartProperty(language, "tocontrolthescreen2"));
    	    data.put("entercode", language_content.getSmartProperty(language, "entercode"));
    	    data.put("andselect", language_content.getSmartProperty(language, "andselect"));
    	    data.put("selectstation", language_content.getSmartProperty(language, "selectstation"));
    	    
    	    data.put("entryimageurl", entryScreen);
    	    //TODO: get a default language for the mainscreen?
    	    data.put("title", stationnode.getSmartProperty("en", "title"));
    	    data.put("jumper", exhibitionnode.getProperty("jumper"));
    	    data.put("domain", LazyHomer.getExternalIpNumber());
    	    data.put("name", model.getProperty("@station/name"));
    	    data.put("labelid", model.getProperty("@station/labelid"));
    	    String showurl = model.getProperty("@exhibition/showurl");
    	    if (showurl!=null && showurl.equals("true")) {
    		data.put("showurl","true");
    	    }
  
    	    String stationselect = model.getProperty("@exhibition/stationselect");
    	    if (stationselect!=null && !stationselect.equals("")&& !stationselect.equals("none")) {
    	    	data.put("stationselect","true");
    	    }
    	    if (stationselect!=null && stationselect.equals("codeselect")) {
    	    	String fullcode =CodeSelector.getFreeRandomCode();
    	    	Boolean codeok =  checkCodeInUse(fullcode);
    	    	while (!codeok) {
        	    	fullcode =CodeSelector.getFreeRandomCode();
    	    		codeok = checkCodeInUse(fullcode);
    	    	}
    	    	
    	    	model.setProperty("@station/codeselect",fullcode);
    	    	
    	    	data.put("codeselect",fullcode);
    	    }
    	    
    	    screen.get(selector).render(data);
    	    screen.get(selector).loadScript(this);
	    
    	    JSONObject d = new JSONObject();	
    	    d.put("command","init");
    	    screen.get(selector).update(d);
    	}
    }
    
    private boolean checkCodeInUse(String newcode) {
    	FSList stations = model.getList("@stations"); // check all the active stations
    	List<FsNode> nodes = stations.getNodes();
    	if (nodes != null) {
    	    for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
    		FsNode node = (FsNode) iter.next();
    		String correctcode = node.getProperty("codeselect");
    		if (correctcode!=null && correctcode.equals(newcode)) {
    		    return false;
    		}
    	    }
    	}
    	return true;
    }
}
