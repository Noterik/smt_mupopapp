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
package org.springfield.lou.controllers.apps.quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;

public class QuizController extends Html5Controller {

	private int slidesdone = 0;
	private FsNode slidenode;
	private int random=0;
	private int gotovalue=0;
	private String slidetype;
	private int slidetimeout=-1;
	private List<FsNode> randomset;
	private Random rnd;
	
	public QuizController() { 
    	rnd = new Random(System.currentTimeMillis());
	}

	public void attach(String sel) {	
		selector = sel;
		model.setProperty("@contentrole", "mainapp");
		System.out.println("ITEM ID WANTED ="+model.getProperty("/screen/selecteditem"));
		FsNode item = model.getNode("@content/item/"+model.getProperty("/screen/selecteditem"));
		if (item!=null) {
			//System.out.println("QUIZ ITEM="+item.asXML());
			slidenode = getFirstSlideNode(item);
		}
		fillPage();
		//model.onNotify("@stationevents/totriviaserver", "onClientMsg", this);
		model.onNotify("/shared[timers]/1second", "on1SecondTimer", this);
	}

	private void fillPage() {
		JSONObject data = new JSONObject();
		FsNode exhibitionnode = model.getNode("@exhibition");
		data.put("slidetype",slidetype);
		data.put(slidetype,"true");
		data.put("slideid",slidenode.getId());
		if (slidetype.equals("image")) {
			data.put("imageurl",slidenode.getProperty("imageurl"));
		} else if (slidetype.equals("imagequestion")) {
			data.put("imageurl",slidenode.getProperty("imageurl"));
		} else if (slidetype.equals("videoquestion")) {
			System.out.println("VIDEO URL="+slidenode.getProperty("videourl"));
			data.put("videourl",slidenode.getProperty("videourl"));
		}
		data.put("domain", LazyHomer.getExternalIpNumber());
		data.put("jumper", exhibitionnode.getProperty("jumper"));	
		screen.get(selector).render(data);
	}

	private void getNextSlideNode() {
		System.out.println("CURRENT SLIDE="+slidenode.asXML());
		String next = slidenode.getProperty("next");
		System.out.println("NEXT="+next);
		if (next.equals("next")) {
			// ok we just need the next slide number
			try {
				int currentid = Integer.parseInt(slidenode.getId());
				currentid++;
				System.out.println("NEW ID="+currentid);
				FsNode node = model.getNode("@content/item/"+model.getProperty("/screen/selecteditem")+"/slide/"+currentid);
				if (node!=null) {
					System.out.println("SLIDENODE="+node.asXML());
					slidetype = node.getProperty("type");
					if (slidetype==null) slidetype="image";
					System.out.println("SLIDETYPE="+slidetype);
					slidetimeout =  Integer.parseInt(node.getProperty("timeout"));
					slidenode = node;
					fillPage();
				}
			} catch(Exception e) {				
			}
		} else if (next.equals("end")) {
			System.out.println("ITS A END POINT SO WHAT IS THE RANDOM VALUE NOW ? "+random);
			if (random>0) {
				// lets find our random set
				if (randomset==null) getRandomSet();
	    		int picked = rnd.nextInt(randomset.size());
	    		slidenode = randomset.get(picked);
	    		randomset.remove(picked);
	    		System.out.println("PICKED NODE="+slidenode.getId());
				slidetype = slidenode.getProperty("type");
				if (slidetype==null) slidetype="image";
				System.out.println("SLIDETYPE="+slidetype);
				slidetimeout =  Integer.parseInt(slidenode.getProperty("timeout"));
				random = random - 1;
				fillPage();
			} else {
				System.out.println("WE ARE DONE WE SHOULD JUMP BACK");
				screen.get(selector).remove();
				model.setProperty("/screen/state","contentselectforce");
			}
		}
	}
	
	private void getRandomSet() {
		randomset = new ArrayList<FsNode>();
		int currentid = Integer.parseInt(slidenode.getId());
		FSList slides = model.getList("@content/item/"+model.getProperty("/screen/selecteditem")+"/slide");
		if (slides!=null && slides.size()>0) { // if we have stations already lets find the highest number
			for(Iterator<FsNode> iter = slides.getNodes().iterator() ; iter.hasNext(); ) {
				FsNode node = (FsNode)iter.next();	
				System.out.println("NODE="+node.asXML());
				String set = node.getProperty("set");
				if (set!=null && set.equals("randomstart")) {
					randomset.add(node);	
				}
			}
		}
	}
	

	private FsNode getFirstSlideNode(FsNode item) {
		try {
			random = Integer.parseInt(item.getProperty("random"));
			gotovalue = Integer.parseInt(item.getProperty("goto"));
			System.out.println("random="+random+" goto="+gotovalue);			
			if (gotovalue!=0) {
				// ok lets get the first slidenode  
				FsNode node = model.getNode("@content/item/"+model.getProperty("/screen/selecteditem")+"/slide/"+gotovalue);
				if (node!=null) {
					System.out.println("SLIDENODE="+node.asXML());
					slidetype = node.getProperty("type");
					if (slidetype==null) slidetype="image";
					System.out.println("SLIDETYPE="+slidetype);
					slidetimeout =  Integer.parseInt(node.getProperty("timeout"));
					return node;
				}
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
	public void on1SecondTimer(ModelEvent e) {
		if (slidetimeout>0) {
			slidetimeout = slidetimeout - 1;
			System.out.println("SLIDE TIMEOUT="+slidetimeout);
			if (slidetimeout==0) {
				getNextSlideNode();
			}
		}
	//	System.out.println("SEC CALLED ON MUPOP");
	}

}
