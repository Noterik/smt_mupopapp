package org.springfield.lou.controllers;

import java.util.Iterator;
import java.util.List;

import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.model.*;
import org.springfield.lou.screen.Capabilities;
import org.springfield.lou.screen.Screen;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;

public class HueSceneManager {

	public static HueSceneManager instance = null;
	public static Model model;
	public static ServiceInterface jimmy;
	
	public static HueSceneManager getInstance(Model m) {
		if (instance==null) {
			model = m;
			instance = new HueSceneManager();
			if (jimmy==null) {
				jimmy = ServiceManager.getService("jimmy");
			}

		}
		return instance;
	}
	
	public HueSceneManager() {
	}
	
	public void onHueCommand(ModelEvent e) {

		
		FsNode msg = e.getTargetFsNode();
		String name = msg.getProperty("name");
		String state = msg.getProperty("state");
		FSList scenes = model.getList("/domain/mupop/location/utrecht/hue/bridge1");
		List<FsNode> nodes = scenes.getNodes();
		if (nodes != null) {
			int pos = 0;
			for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
				FsNode node = (FsNode) iter.next();
				String scenestate=null;
				
				// lets first check if sensors hit somehow if not lights stay the same
				FSList types = model.getList("/domain/mupop/location/utrecht/hue/bridge1/scene/"+node.getId()+"/sensor");
				List<FsNode> nodes2 = types.getNodes();
				if (nodes2 != null) {
					for (Iterator<FsNode> iter2 = nodes2.iterator(); iter2.hasNext();) {
						FsNode typenode = (FsNode) iter2.next();
						//System.out.println("SSSSS="+typenode.asXML());
						String sname = typenode.getProperty("name");
						if (sname!=null) {
							if (sname.equals(name)) {
								String stype = typenode.getName();
							    scenestate = checkSensor(scenestate,name,state,node.getId(),typenode);
							}
						}
					}
				}
				
				// if sensor or switch did something we for the lights
				if (scenestate!=null) {
					types = model.getList("/domain/mupop/location/utrecht/hue/bridge1/scene/"+node.getId()+"/light");
					nodes2 = types.getNodes();
					if (nodes2 != null) {
						for (Iterator<FsNode> iter2 = nodes2.iterator(); iter2.hasNext();) {
							FsNode typenode = (FsNode) iter2.next();
							String sname = typenode.getProperty("name");
							if (sname!=null) {
								String stype = typenode.getName();
								checkLight(scenestate, sname, scenestate, node.getId(), typenode);
							}
						}
					}
				}

				
			}
		}
	}
	
	
	private void checkLight(String incomingstate,String name,String state,String scenename,FsNode typenode) {
		//System.out.println("CHECK LIGHT="+name+" STATE="+incomingstate);
		if (incomingstate.equals("true")) {		
			// what do we do when scene becomes active ?
			String scenetrue =  typenode.getProperty("scenetrue");
			if (scenetrue!=null) {
				System.out.println("LIGHT ACTION SCENE TRUE="+scenetrue+" FOR="+name);
				jimmy.put("NTK-RAS1/light/"+name+",state=true",null, null);
			}
		} else if (incomingstate.equals("false")) {
			String scenefalse =  typenode.getProperty("scenetrue");
			if (scenefalse!=null) {
				System.out.println("LIGHT ACTION SCENE FALSE="+scenefalse+" FOR="+name);
				jimmy.put("NTK-RAS1/light/"+name+",state=false",null, null);
			}	
		}
		try {
			Thread.sleep(200);
		} catch(Exception e) {
			
		}
		//System.out.println("LIGHT="+typenode.asXML());
	}
	
	private String checkSensor(String incomingstate,String name,String state,String scenename,FsNode typenode) {
			// so we know what to look for
			String pt = typenode.getProperty("presence_true");
			if (pt!=null && pt.equals("true")) {
				// ok so this needs todo something on presence
				if (state.indexOf("\"presence\":true")!=-1) {
					//System.out.println("SEND OOOOOOOON");
					FsNode ev = new FsNode("msg","1");
					ev.setProperty("scenename","utrecht/bridge1/"+scenename);
					ev.setProperty("state","true");
					ev.setProperty("oldstate","false");
					model.notify("/shared[app]/huescene/change",ev);
					return "true";
				}
			}									
			String pf = typenode.getProperty("presence_false");
			if (pf!=null && pf.equals("false")) {
				if (state.indexOf("\"presence\":false")!=-1) {
					//System.out.println("SEND OFFFFFFFF");
					FsNode ev = new FsNode("msg","1");
					ev.setProperty("scenename","utrecht/bridge1/"+scenename);
					ev.setProperty("state","false");
					ev.setProperty("oldstate","true");
					model.notify("/shared[app]/huescene/change",ev);
					return "false";
				}
			}
		return incomingstate;
	}
	
}


//System.out.println("HUENODE="+msg.asXML());
//String name = msg.getProperty("name");
//System.out.println("SCENE MANAGER NAME="+name);
/*
if (name.equals("Bigsensor")) {
	if (state.indexOf("\"presence\":false")!=-1) {
		FsNode ev = new FsNode("msg","1");
		ev.setProperty("scenename","daniel/HUEDEMO1/WAITROOM");
		ev.setProperty("state","false");
		ev.setProperty("oldstate","true");
		model.notify("/shared[app]/huescene/change",ev);
	} else {
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
		FsNode ev = new FsNode("msg","1");
		ev.setProperty("scenename","daniel/HUEDEMO1/ATSCREEN");
		ev.setProperty("state","true");
		ev.setProperty("oldstate","false");
		model.notify("/shared[app]/huescene/change",ev);
	}
} else if (name.equals("Keuken dimmer")) {
	if (state.indexOf("\"buttonevent\":1002")!=-1) {
		//System.out.println("Scene off signal?  "+name);
		FsNode ev = new FsNode("msg","1");
		ev.setProperty("scenename","daniel/HUEDEMO1/DRUKKER1");
		ev.setProperty("state","true");
		ev.setProperty("oldstate","false");
		model.notify("/shared[app]/huescene/change",ev);
	} else if (state.indexOf("\"buttonevent\":4002")!=-1) {
		FsNode ev = new FsNode("msg","1");
		ev.setProperty("scenename","daniel/HUEDEMO1/DRUKKER1");
		ev.setProperty("state","false");
		ev.setProperty("oldstate","true");
		model.notify("/shared[app]/huescene/change",ev);
	}
}
*/

