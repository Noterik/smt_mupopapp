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

import javax.jws.WebParam.Mode;

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
	private int showtimer=0;
	private List<FsNode> randomset;
	private Random rnd;
	private String showanswer = "false";
	
	public QuizController() { 
    	rnd = new Random(System.currentTimeMillis());
	}

	public void attach(String sel) {	
		selector = sel;
		model.setProperty("@contentrole", "mainapp");
		FsNode item = model.getNode("@content/item/"+model.getProperty("/screen/selecteditem"));
		if (item!=null) {
			slidenode = getFirstSlideNode(item);
		}
		fillPage();
		model.onNotify("/shared[timers]/1second", "on1SecondTimer", this);
	}
	
	private int calcTimer(int t) {
		if (t>showtimer) {
			return t-showtimer;
		} else {
			return t;
		}
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
			data.put("image", "true");
			data.put("questions", "true");
			data.put("imageurl",slidenode.getProperty("imageurl"));
			data.put("slidequestion",slidenode.getProperty("question"));
			data.put("slideanswer1",slidenode.getProperty("answer1"));
			data.put("slideanswer2",slidenode.getProperty("answer2"));
			data.put("slideanswer3",slidenode.getProperty("answer3"));
			data.put("slideanswer4",slidenode.getProperty("answer4"));
		} else if (slidetype.equals("videoquestion")) {
			data.put("video", "true");
			data.put("questions", "true");
			data.put("videourl",slidenode.getProperty("videourl"));
			data.put("slidequestion",slidenode.getProperty("question"));
			data.put("slideanswer1",slidenode.getProperty("answer1"));
			data.put("slideanswer2",slidenode.getProperty("answer2"));
			data.put("slideanswer3",slidenode.getProperty("answer3"));
			data.put("slideanswer4",slidenode.getProperty("answer4"));
		}
		data.put("timeout", ""+calcTimer(slidetimeout));
		data.put("domain", LazyHomer.getExternalIpNumber());
		if (model.getProperty("@station/codeselect") != null) {
		    data.put("code", model.getProperty("@station/codeselect"));
		}
		screen.get(selector).render(data);
	}

	private void getNextSlideNode() {
		showanswer = "false";
		String next = slidenode.getProperty("next");
		if (next.equals("next")) {
			// ok we just need the next slide number
			try {
				int currentid = Integer.parseInt(slidenode.getId());
				currentid++;
				FsNode node = model.getNode("@content/item/"+model.getProperty("/screen/selecteditem")+"/slide/"+currentid);
				if (node!=null) {
					slidetype = node.getProperty("type");
					if (slidetype==null) slidetype="image";
					slidetimeout =  Integer.parseInt(node.getProperty("timeout"));
					if (slidetype.equals("imagequestion") || slidetype.equals("videoquestion")) {
						showtimer = 3;
					}
					slidenode = node;
					fillPage();
				}
			} catch(Exception e) {				
			}
		} else if (next.equals("end")) {
			if (random>0) {
				// lets find our random set
				if (randomset==null) getRandomSet();
	    		int picked = rnd.nextInt(randomset.size());
	    		slidenode = randomset.get(picked);
	    		randomset.remove(picked);
				slidetype = slidenode.getProperty("type");
				if (slidetype==null) slidetype="image";
				slidetimeout =  Integer.parseInt(slidenode.getProperty("timeout"));
				if (slidetype.equals("imagequestion") || slidetype.equals("videoquestion")) {
					showtimer = 3;
				}
				random = random - 1;
				fillPage();
			} else {
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
			if (gotovalue!=0) {
				// ok lets get the first slidenode  
				FsNode node = model.getNode("@content/item/"+model.getProperty("/screen/selecteditem")+"/slide/"+gotovalue);
				if (node!=null) {
					slidetype = node.getProperty("type");
					if (slidetype==null) slidetype="image";
					slidetimeout =  Integer.parseInt(node.getProperty("timeout"));
					showtimer = 0;
					showanswer = "false";
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
			screen.get("#quiz-game-timer").html(""+calcTimer(slidetimeout));
			if (showtimer!=0 && (slidetype.equals("imagequestion") || slidetype.equals("videoquestion"))) {
				if (slidetimeout==showtimer) {
					showanswer = "true";
					System.out.println("MAKE SHOWANSWER TRUE");
					// lets set the anser (little hack until later)
					String correctanswer = slidenode.getProperty("correctanswer");
					if (correctanswer.equals("1")) {
						screen.get("#quiz-game-answer2_container").css("background-color","white");
						screen.get("#quiz-game-answer3_container").css("background-color","white");
						screen.get("#quiz-game-answer4_container").css("background-color","white");
						screen.get("#quiz-game-answer2_container").css("color","grey");
						screen.get("#quiz-game-answer3_container").css("color","grey");
						screen.get("#quiz-game-answer4_container").css("color","grey");
					} else if (correctanswer.equals("2")) {
						screen.get("#quiz-game-answer1_container").css("background-color","white");
						screen.get("#quiz-game-answer3_container").css("background-color","white");
						screen.get("#quiz-game-answer4_container").css("background-color","white");	
						screen.get("#quiz-game-answer1_container").css("color","grey");
						screen.get("#quiz-game-answer3_container").css("color","grey");
						screen.get("#quiz-game-answer4_container").css("color","grey");
					} else if (correctanswer.equals("3")) {
						screen.get("#quiz-game-answer1_container").css("background-color","white");
						screen.get("#quiz-game-answer2_container").css("background-color","white");
						screen.get("#quiz-game-answer4_container").css("background-color","white");	
						screen.get("#quiz-game-answer1_container").css("color","grey");
						screen.get("#quiz-game-answer2_container").css("color","grey");
						screen.get("#quiz-game-answer4_container").css("color","grey");
					} else if (correctanswer.equals("4")) {
						screen.get("#quiz-game-answer1_container").css("background-color","white");
						screen.get("#quiz-game-answer2_container").css("background-color","white");
						screen.get("#quiz-game-answer3_container").css("background-color","white");	
						screen.get("#quiz-game-answer1_container").css("color","grey");
						screen.get("#quiz-game-answer2_container").css("color","grey");
						screen.get("#quiz-game-answer3_container").css("color","grey");
					}
				}
			}
			if (slidetimeout==0) {
				getNextSlideNode();
			}
		}
		slidenode.setProperty("showanswer",showanswer);
		model.notify("@quizslide", slidenode);
 	}

}
