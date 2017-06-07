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
package org.springfield.lou.controllers.apps.whatwethink;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
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


public class WhatWeThinkController extends Html5Controller {
	
	private int timeout = 0;
	private int itemcounter=1;
	private int questioncounter=1;
	private FsNode item = null;
	private FsNode question = null;
	private String lastchanged;
	private static ArrayList<String> colorbucket;
	//private int colorcounter=0;
	
	private boolean feedback = true;
	
	public WhatWeThinkController() { }
	
	public void attach(String sel) {	
		selector = sel;
		colorbucket = fillColorBucket();
		model.setProperty("@contentrole", "mainapp");
		fillPage();
		model.onNotify("/shared[timers]/1second", "on1SecondTimer", this);
		model.onNotify("/shared[timers]/50ms","on50MillisSecondsTimer",this); 
		model.onNotify("@stationevents/towhatwethinkserver", "onClientMsg", this);
	}
	
	public void on50MillisSecondsTimer(ModelEvent e) {
		String changed = model.getProperty("@whatwethinkdots/changed");
		if (changed!=null && !changed.equals(lastchanged)) {
			fillDots();
			lastchanged = changed;
		}
	}
	
	private void toggleFeedBack() {
		feedback = !feedback;
		FsNode msgnode = new FsNode("msgnode", "1");
		msgnode.setProperty("command","feedbackstate");
		msgnode.setProperty("feedback", ""+feedback);
		model.notify("@appstate", msgnode);
	}
	
	public void on1SecondTimer(ModelEvent e) {
		if (timeout > 0) {
			timeout = timeout - 1;

		} else {
			if (1==1) { // need fix :)
				toggleFeedBack();
				if (feedback) {
					timeout = 5;
					handleAnswers();	
					fillPage();
					//FsNode msgnode = new FsNode("msgnode", "1");
					//msgnode.setProperty("command","feedbackstate");
					//msgnode.setProperty("feedback", ""+feedback);
					//model.notify("@appstate", msgnode);
				} else {
					timeout = 10;
					getNextStatement();
					fillPage();
					fillDots();
					//send questionId to players screen id  (daniel should not be a message!)
					FsNode msgnode = new FsNode("msgnode", "1");
					msgnode.setProperty("itemid", model.getProperty("@itemid"));
					msgnode.setProperty("itemquestionid", model.getProperty("@itemquestionid"));
					msgnode.setProperty("feedback",""+feedback);
					msgnode.setProperty("command","newquestion");
					msgnode.setProperty("screenid","all");
					model.notify("@appstate", msgnode);

				}
			} else {	
				toggleFeedBack();
				if (feedback) {
					timeout = 10;	
					fillPage();
					//FsNode msgnode = new FsNode("msgnode", "1");
					//msgnode.setProperty("command","feedbackstate");
					//msgnode.setProperty("feedback", ""+feedback);
					//model.notify("@appstate", msgnode);
				} else {
					//handleAnswers();
					timeout = 5;
					getNextStatement();
					fillPage();
					fillDots();
					//sendRandomQuestions();
				}
			}
		}

		FsNode msgnode = new FsNode("msgnode", "1");
		msgnode.setProperty("itemid", model.getProperty("@itemid"));
		msgnode.setProperty("command","timer");
		msgnode.setProperty("timer", ""+timeout);
		model.notify("@appstate", msgnode);
		/*
		screen.get("#trivia-nexttimer").html(""+timeout);
		screen.get("#trivia-answertimer").html(""+timeout);
		*/
		
		screen.get("#whatwethink-timer-one").html(""+timeout+" sec");
		screen.get("#whatwethink-timer-two").html("next statement in "+timeout+" sec");
	}
	
	private void loadAnswers(String questionid) {
		FSList list = model.getList("@station/player");
		if (list!=null) {
			List<FsNode> nodes = list.getNodes();
			if (nodes != null) {
				for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
					FsNode node = (FsNode) iter.next();
					System.out.println("DB PLAYER LOAD="+node.asXML());
					FsNode pnode = model.getNode("@whatwethinkdots/results/"+node.getId()); // based on a bug needs fixing
					System.out.println("MEMNODE="+pnode);
					
					String dataline = node.getProperty("pos_"+questionid);
					if (dataline!=null && !dataline.equals("")) {
						String[] parts = dataline.split(",");
						pnode.setProperty("x",parts[0]);
						pnode.setProperty("y",parts[1]);
						System.out.println("LX="+parts[0]);
						System.out.println("LY="+parts[1]);
					} else {
						pnode.setProperty("x","-1");
						pnode.setProperty("y","-1");
					}
				}
			}
		}
		
	}
	
	private void handleAnswers() {
		System.out.println("HANDLE ANSWERS");
		FSList list = model.getList("@whatwethinkdots/results");
		System.out.println("H1="+list);
		Long now = new Date().getTime();
		if (list!=null) {
			System.out.println("H2="+list.size());
			List<FsNode> nodes = list.getNodes();
			if (nodes != null) {
				for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
					FsNode node = (FsNode) iter.next();
					//System.out.println("H3="+node.asXML());
					String ls = node.getProperty("lastseen");
					if (ls!=null && !ls.equals("")) {
						long lsl = Long.parseLong(ls);
						if (now-lsl>(10*60*1000)) {
							node.setProperty("c","#666666");
						}
					} else {
						node.setProperty("c","#666666");
					}
					String id  = node.getId();
					//FsNode dbnode  = model.getNode("@station/player['"+id+"']");
					String qid = model.getProperty("@itemquestionid");
					String dataline = node.getProperty("x")+","+node.getProperty("y");
					model.setProperty("@station/player['"+id+"']/pos_"+qid,dataline);
					//node.setProperty("x","-1");
					//node.setProperty("y","-1");
				}	

			}
		}
	}

	
	private void fillPage() {
		JSONObject data = new JSONObject();
		if (feedback) {
			data.put("timeout-msg",""+timeout+" sec");
			data.put("feedback", "true");
		}
		
		if (question!=null) { // just to make sure
			data.put("question",question.getProperty("question"));	
			data.put("answer1",question.getProperty("answer1"));
			data.put("answer2",question.getProperty("answer2"));
		}
		screen.get(selector).render(data);
	}
	
	private void getNextStatement() {
		model.setProperty("@itemid",""+itemcounter);
		item = model.getNode("@item");
		
		// ok lets now get the question
		model.setProperty("@itemquestionid",""+questioncounter);
		question = model.getNode("@itemquestion");
		if (question!=null) {
			// move to the next one
			questioncounter++;
		} else {
			itemcounter++;
			questioncounter = 1;
			model.setProperty("@itemid",""+itemcounter);
			model.setProperty("@itemquestionid",""+questioncounter);
			question = model.getNode("@itemquestion");
			if (question==null) {
				// so we run out of all questions reset the whole loop
				itemcounter = 1;
				questioncounter = 1;
				model.setProperty("@itemid",""+itemcounter);
				model.setProperty("@itemquestionid",""+questioncounter);
				question = model.getNode("@itemquestion");
			}
		}
		
		loadAnswers(question.getId());
		
	}
	
	public void onClientMsg(ModelEvent e) {
		FsNode message = e.getTargetFsNode();
		String clientScreenId = message.getId();
		String command = message.getProperty("command");
		if (command.equals("join")) {
			
			String playername = message.getProperty("username");
			FsNode player = model.getNode("@station/player['"+playername+"']");
			System.out.println("DBPLAYER="+player);
			
	
			// should we not ceheck for the old one?
			//FsNode player  = activePlayers.get(playername);
			if (player==null) {
				// get tge current number of players to find the color 
				FSList players = model.getList("@station/player");
				System.out.println("SIZEEEEE="+players.size());
				String newcolor = colorbucket.get(players.size());
				player =  new FsNode("player",playername);
				player.setProperty("playername", playername);
				player.setProperty("color", newcolor);
				player.setProperty("lastseen",""+new Date().getTime());
				player.setProperty("clientScreenId", clientScreenId);
				//player = new WhatWeThinkPlayer(model, playername, clientScreenId,newcolor);
				
				//activePlayers.put(playername, player);
				model.putNode("@station", player);
			} else {
				// update its screenid 
				player.setProperty("clientScreenId", clientScreenId);
				player.setProperty("lastseen",""+new Date().getTime());
			}
			
			//send questionId to players screen id  (daniel should not be a message!)
			FsNode msgnode = new FsNode("msgnode", "1");
			msgnode.setProperty("itemid", model.getProperty("@itemid"));
			msgnode.setProperty("color", player.getProperty("color"));
			msgnode.setProperty("itemquestionid", model.getProperty("@itemquestionid"));
			
			msgnode.setProperty("feedback",""+feedback);
			msgnode.setProperty("command","newquestion");
			msgnode.setProperty("screenid", player.getProperty("clientScreenId"));
			model.notify("@appstate", msgnode);
			fillDots();
		} else if (command.equals("answer")) {
			/*
			String answerId = message.getProperty("value");
			String state = message.getProperty("answer");
			String playername = message.getProperty("playername");
			if (activePlayers.containsKey(playername)) {
				activePlayers.get(playername).setAnswerId(Integer.parseInt(answerId));
				if (state.equals("correct")) {
					activePlayers.get(playername).setAnsweredCorrect(true);
				} else {
					activePlayers.get(playername).setAnsweredCorrect(false);
				}
			} else {
				//Got answer from non active player,
				//refresh this clients screen
			}
			fillPage();
			*/
		}
	}
	
	private void fillDots() {
		FSList fslist = model.getList("@whatwethinkdots");
		if (fslist!=null) {
			JSONObject data = fslist.toJSONObject("en","x,y,c"); // turn the lists into a json object
			screen.get(selector).render(data,"whatwethink-statements-dots");
		}
	}
	
	private ArrayList<String> fillColorBucket() {
		ArrayList<String> bucket = new ArrayList<String>();
		// fixed colors
		bucket.add("#ff0000");
		bucket.add("#0ff000");
		bucket.add("#00ff00");
		bucket.add("#000ff0");
		bucket.add("#ffff00");
		bucket.add("#993399");
		bucket.add("#0000ff");
		bucket.add("#33ccff");
		bucket.add("#000f0f");
		bucket.add("#00f00f");
		bucket.add("#0f000f");
		bucket.add("#f0000f");
		for (int i=0;i<5000;i++) {
			String newcolor = generateColor();
			if (!bucket.contains(newcolor)) {
				bucket.add(newcolor);
			}
		}
		return bucket;
	}
	
	private String generateColor() {	    
		//to get rainbow, pastel colors
		Random random = new Random();
		final float hue = random.nextFloat();
		// Saturation between 0.6 and 0.8
		final float saturation = (random.nextInt(2000) + 6000) / 10000f;
		final float luminance = 0.9f;
		final Color color = Color.getHSBColor(hue, saturation, luminance);

		return "#"+Integer.toHexString(color.getRGB()).substring(2);
	}




	
}
