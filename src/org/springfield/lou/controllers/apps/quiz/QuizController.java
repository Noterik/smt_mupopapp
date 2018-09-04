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
import org.springfield.lou.controllers.ExhibitionMemberManager;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class QuizController extends Html5Controller {

	private int slidesdone = 0;
	private FsNode slidenode;
	private int random=0;
	private int gotovalue=0;
	private int endgotovalue=0;
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
		if (slidetype.equals("imagequestion") || slidetype.equals("videoquestion")) {
			if (t>showtimer) {
				return t-showtimer;
			} else {
				return t;
			}
		} else {
			return t;
		}
	}

	private void fillPage() {
		JSONObject data = new JSONObject();
		FsNode exhibitionnode = model.getNode("@exhibition");
		String mstfile = "quiz/quiz.mst";
		data.put("slidetype",slidetype);
		data.put(slidetype,"true");
		data.put("slideid",slidenode.getId());
		if (slidetype.equals("image")) {
			data.put("imageurl",slidenode.getProperty("imageurl"));
			data.put("membercount",""+ExhibitionMemberManager.getMemberCount(screen));
			mstfile = "quiz/quiz_image.mst";
		} else if (slidetype.equals("video")) {
			data.put("videourl",slidenode.getProperty("videourl"));
			data.put("membercount",""+ExhibitionMemberManager.getMemberCount(screen));
			mstfile = "quiz/quiz_video.mst";
		} else if (slidetype.equals("imagequestion")) {
			data.put("image", "true");
			data.put("questions", "true");
			data.put("imageurl",slidenode.getProperty("imageurl"));
			data.put("slidequestion",slidenode.getProperty("question"));
			data.put("slideanswer1",slidenode.getProperty("answer1"));
			data.put("slideanswer2",slidenode.getProperty("answer2"));
			data.put("slideanswer3",slidenode.getProperty("answer3"));
			data.put("slideanswer4",slidenode.getProperty("answer4"));
			data.put("membercount",""+ExhibitionMemberManager.getMemberCount(screen));
			mstfile = "quiz/quiz_image.mst";
		} else if (slidetype.equals("videoquestion")) {
			data.put("video", "true");
			data.put("questions", "true");
			data.put("videourl",slidenode.getProperty("videourl"));
			data.put("slidequestion",slidenode.getProperty("question"));
			data.put("slideanswer1",slidenode.getProperty("answer1"));
			data.put("slideanswer2",slidenode.getProperty("answer2"));
			data.put("slideanswer3",slidenode.getProperty("answer3"));
			data.put("slideanswer4",slidenode.getProperty("answer4"));
			data.put("membercount",""+ExhibitionMemberManager.getMemberCount(screen));
			mstfile = "quiz/quiz_video.mst";
		} else if (slidetype.equals("highscore")) {
			data.put("highscore", "true");
			data.put("membercount",""+ExhibitionMemberManager.getMemberCount(screen));
			addHighScoreNodes(data);
			mstfile = "quiz/quiz_highscore.mst";
		}
		data.put("command", "timer");
		data.put("timeout", ""+calcTimer(slidetimeout));
		data.put("domain", LazyHomer.getExternalIpNumber());
		if (model.getProperty("@station/codeselect") != null) {
		    data.put("code", model.getProperty("@station/codeselect"));
		}
		
		data.put("nl","true");
		
		screen.get(selector).setViewProperty("template",mstfile);
		screen.get(selector).render(data);
		screen.get(selector).loadScript(this);
		
		//send timer time to javascript so we can dynamically create the css countdown spinner
		JSONObject d = new JSONObject();
		d.put("command", "timer");
		d.put("timeout", ""+calcTimer(slidetimeout));
		screen.get(selector).update(d);
		screen.get("#quiz-game-players_container").on("mouseup", "onMembersReset", this);
		
	}
	
	public void onMembersReset(Screen s, JSONObject data) {
		System.out.println("DELETE ALL MEMBER INFO");
		ExhibitionMemberManager.freeAllNames(screen);
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
				// do we have a endgoto ?
				if (endgotovalue!=0) {
					slidenode = getLastSlideNode();
					endgotovalue = 0;
					fillPage();
				} else {		
					screen.get(selector).remove();
					model.setProperty("/screen/state","contentselectforce");
					// Inform clients to switch
					FsNode message = new FsNode("message", screen.getId());
					message.setProperty("request", "contentselect");
					model.notify("@stationevents/fromclient", message);
				}
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

	private FsNode getLastSlideNode() {
		FsNode node = model.getNode("@content/item/"+model.getProperty("/screen/selecteditem")+"/slide/"+endgotovalue);
		if (node!=null) {
			slidetype = node.getProperty("type");
			slidetimeout =  Integer.parseInt(node.getProperty("timeout"));
			showtimer = 0;
			return node;
		}
		return null;
	}
	
	private FsNode getFirstSlideNode(FsNode item) {
		try {
			random = Integer.parseInt(item.getProperty("random"));
			endgotovalue = Integer.parseInt(item.getProperty("endgoto"));
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
					//update timer spinner to reflect correct answer time
					//send timer time to javascript so we can dynamically create the css countdown spinner
					JSONObject d = new JSONObject();
					d.put("command", "timer");
					d.put("timeout", ""+calcTimer(slidetimeout));
					screen.get(selector).update(d);
					
					// lets set the anser (little hack until later)
					System.out.println("SLIDENODE="+slidenode);
					if (slidenode!=null) {
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
			}
			if (slidetimeout==0) {
				getNextSlideNode();
			}
		}
		slidenode.setProperty("showanswer",showanswer);
		model.notify("@quizslide", slidenode);
 	}
	
	private void addHighScoreNodes(JSONObject data) {
		FSList list = ExhibitionMemberManager.getActiveMembers(screen,43200);
		List<FsNode> nodes = list.getNodes();
		FSList results = new FSList();
		if (nodes != null) {
			for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
				FsNode node = (FsNode) iter.next();
				FsNode nnode = new FsNode("member",node.getId());
				String name = node.getProperty("name");
				String score = node.getProperty("score");
				
				if (name!=null && !name.equals("")) {
					nnode.setProperty("name",name);
					if (score!=null && !score.equals("")) {
						nnode.setProperty("score",score);
					}
				}
				results.addNode(nnode);
			}
			List<FsNode> scores = results.getNodesSorted("score", "up");
			data.put("members",FSList.ArrayToJSONObject(scores, "en", "name,score"));
			
			//data.put("members",results.toJSONObject("en","name,score"));
		}
	}
}
