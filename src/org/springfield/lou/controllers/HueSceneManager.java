package org.springfield.lou.controllers;

import org.springfield.fs.FsNode;
import org.springfield.lou.model.*;
import org.springfield.lou.screen.Capabilities;
import org.springfield.lou.screen.Screen;

public class HueSceneManager {

	public static HueSceneManager instance = null;
	public static Model model;
	
	public static HueSceneManager getInstance(Model m) {
		if (instance==null) {
			model = m;
			instance = new HueSceneManager();

		}
		return instance;
	}
	
	public HueSceneManager() {
	}
	
	public void onHueCommand(ModelEvent e) {
		FsNode msg = e.getTargetFsNode();
		//System.out.println("HUENODE="+msg.asXML());
		String name = msg.getProperty("name");
		String state = msg.getProperty("state");
		System.out.println("SCENE MANAGER NAME="+name);
		if (name.equals("Bigsensor")) {
			if (state.indexOf("\"presence\":false")!=-1) {
				//System.out.println("Scene off signal?  "+name);
				FsNode ev = new FsNode("msg","1");
				ev.setProperty("scenename","daniel/HUEDEMO1/WAITROOM");
				ev.setProperty("state","false");
				ev.setProperty("oldstate","true");
				model.notify("/shared[app]/huescene/change",ev);
			} else {
				//System.out.println("Scene ON "+name);
				FsNode ev = new FsNode("msg","1");
				ev.setProperty("scenename","daniel/HUEDEMO1/WAITROOM");
				ev.setProperty("state","true");
				ev.setProperty("oldstate","false");
				model.notify("/shared[app]/huescene/change",ev);
			}
		} else if (name.equals("Keuken sensor")) {
			if (state.indexOf("\"presence\":false")!=-1) {
				//System.out.println("Scene off signal?  "+name);
				FsNode ev = new FsNode("msg","1");
				ev.setProperty("scenename","daniel/HUEDEMO1/ATSCREEN");
				ev.setProperty("state","false");
				ev.setProperty("oldstate","true");
				model.notify("/shared[app]/huescene/change",ev);
			} else {
				//System.out.println("Scene ON "+name);
				FsNode ev = new FsNode("msg","1");
				ev.setProperty("scenename","daniel/HUEDEMO1/ATSCREEN");
				ev.setProperty("state","true");
				ev.setProperty("oldstate","false");
				model.notify("/shared[app]/huescene/change",ev);
			}
		}
	}
	
}
