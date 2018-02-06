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
	private int questioncounter=0;
	//private int realquestioncounter=1;
	private FsNode item = null;
	private FsNode question = null;
	private String lastchanged;
	private static ArrayList<String> colorbucket;
	private FSList axis;
	private FSList allquestions;
	//private int colorcounter=0;
	
	private boolean feedback = true;
	
	public WhatWeThinkController() { }
	
	public void attach(String sel) {	
		selector = sel;
		colorbucket = fillColorBucket();
		
		
		model.setProperty("@contentrole", "mainapp");
		axis = model.getList("@itemaxis");
		allquestions = loadAllQuestions();
		System.out.println("ALLQCOUNT="+allquestions.size());
		fillPage();
		model.onNotify("/shared[timers]/1second", "on1SecondTimer", this);
		model.onNotify("/shared[timers]/10second", "on10SecondTimer", this);
		model.onNotify("/shared[timers]/50ms","on50MillisSecondsTimer",this); 
		model.onNotify("@stationevents/towhatwethinkserver", "onClientMsg", this);
	}
	
	public void on10SecondTimer(ModelEvent e) {
		long starttime = new Date().getTime();
		System.out.println("RECALC ALL WANTED");
		// first reset the axis
		
		HashMap<String,Integer> results = new HashMap<String, Integer>();
		for (Iterator<FsNode> iter = axis.getNodes().iterator(); iter.hasNext();) {
			FsNode ax = (FsNode) iter.next();
			results.put(ax.getId(),0);
		}
		
		FSList players = model.getList("@station/player");
		if (players!=null) {
			int hm = 0;
			double cf = 1;
			List<FsNode> nodes = players.getNodes();
			if (nodes != null) {
				for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
					FsNode node = (FsNode) iter.next();
					//System.out.println("PLAYER="+node.asXML());
					for (Iterator<FsNode> iter2 = axis.getNodes().iterator(); iter2.hasNext();) {
						FsNode ax = (FsNode) iter2.next();
						int curscore = results.get(ax.getId());
						try {
							String newval = node.getProperty("axis_"+ax.getId());
							//System.out.println("newval="+newval);
							curscore += Integer.parseInt(newval);
							results.put(ax.getId(), curscore);
							if (curscore>hm) {
								hm=curscore;
								cf = 50.0/hm;
							}
						} catch(Exception e2) {
						//	e2.printStackTrace();
						}
						if (cf==0) cf=1;
						//System.out.println("score "+ax.getId()+" "+curscore+" hm="+hm+" cf="+cf);
					}
				}
			}
			for (Iterator<FsNode> iter = axis.getNodes().iterator(); iter.hasNext();) {
				FsNode ax = (FsNode) iter.next();
				int mi = results.get(ax.getId());
				ax.setProperty("size", ""+((mi*cf)+1));
			}
			
		}
		long endtime = new Date().getTime();
		System.out.println("all calc time="+(endtime-starttime));
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
					//msgnode.setProperty("itemid", model.getProperty("@itemid"));
					//msgnode.setProperty("itemquestionid", model.getProperty("@itemquestionid"));
					msgnode.setProperty("question", question.getProperty("question"));
					msgnode.setProperty("answer1", question.getProperty("answer1"));
					msgnode.setProperty("answer2", question.getProperty("answer2"));
					msgnode.setProperty("feedback",""+feedback);
					msgnode.setProperty("questioncount",""+allquestions.size());
					msgnode.setProperty("questionnumber",""+questioncounter);
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
		//msgnode.setProperty("itemid", model.getProperty("@itemid"));
		msgnode.setProperty("command","timer");
		msgnode.setProperty("timer", ""+timeout);
		model.notify("@appstate", msgnode);
		
		screen.get("#whatwethink-timer-one").html(""+timeout+" sec");
		screen.get("#whatwethink-timer-two").html("volgende keuze in "+timeout+" sec");
	}
	
	private void loadAnswers(String questionid) {
		FSList list = model.getList("@station/player");
		if (list!=null) {
			List<FsNode> nodes = list.getNodes();
			if (nodes != null) {
				for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
					FsNode node = (FsNode) iter.next();
					FsNode pnode = model.getNode("@whatwethinkdots/results/"+node.getId()); // based on a bug needs fixing
					
					String dataline = node.getProperty("pos_"+questionid);
					if (dataline!=null && !dataline.equals("") && dataline.indexOf("-1")==-1) {
						String[] parts = dataline.split(",");
						pnode.setProperty("x",parts[0]);
						pnode.setProperty("y",parts[1]);
					} else {
						pnode.setProperty("x","-100");
						pnode.setProperty("y","-100");
					}
				}
			}
		}
		
	}
	
	private void handleAnswers() {
		FSList list = model.getList("@whatwethinkdots/results");
		Long now = new Date().getTime();
		if (list!=null) {
			List<FsNode> nodes = list.getNodes();
			if (nodes != null) {
				for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
					FsNode node = (FsNode) iter.next();
					String ls = node.getProperty("lastseen");
					if (ls!=null && !ls.equals("")) {
						long lsl = Long.parseLong(ls);
						if (now-lsl>(10*60*1000)) {
							node.setProperty("c","#666666");
						}
						if (now-lsl<(20*1000)) {
							String id  = node.getId();
							updateProfile(id);
							String dataline = node.getProperty("x")+","+node.getProperty("y");
							model.setProperty("@station/player['"+id+"']/pos_"+questioncounter,dataline);
						}
					} else {
						node.setProperty("c","#666666");
					}
					//String id  = node.getId();
					//String qid = model.getProperty("@itemquestionid");
					//String dataline = node.getProperty("x")+","+node.getProperty("y");
					//model.setProperty("@station/player['"+id+"']/pos_"+questioncounter,dataline);

				}	

			}
		}
	}
	
	private void updateProfile(String id) {
		long starttime = new Date().getTime();
		System.out.println("RECALC FOR USER="+id);
		FsNode player = model.getNode("@station/player['"+id+"']");
		// limit to users that changed something in last 15seconds !

		
		// create the result table
		HashMap results = new HashMap<String, Integer>();
		if (axis!=null) {
			List<FsNode> nodes = axis.getNodes();
			if (nodes != null) {
				for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
					FsNode node = (FsNode) iter.next();
					results.put(node.getId(),0);
				}
			}
		}
		
		for (Iterator<FsNode> iter = allquestions.getNodes().iterator(); iter.hasNext();) {
			FsNode qnode = (FsNode) iter.next();
					
			String calcline = qnode.getProperty("calc");
			//System.out.println("CALCLINE="+calcline);
			calcline=calcline+",";
			String[] cparts = calcline.split(",");
			String acalc=null;
			String bcalc=null;
			
			if (cparts[0].startsWith("A")) {
				acalc = cparts[0].split("=")[1];
			}
			if (cparts[1].startsWith("A")) {
				acalc = cparts[1].split("=")[1];
			}
			if (cparts[0].startsWith("B")) {
				bcalc = cparts[0].split("=")[1];
			}
			if (cparts[1].startsWith("B")) {
				bcalc = cparts[1].split("=")[1];
			}
			
			
			String answer = player.getProperty("pos_"+qnode.getId());

			if (answer!=null && !answer.equals("") && !answer.contains("null")) {
				//System.out.println("POS="+answer);
				String[] parts = answer.split(",");
				try {
					double x = Double.parseDouble(parts[0]);
					double y = Double.parseDouble(parts[1]);
					if (y>0 && y<100) { // valid check
						if (x>0 && x<50) {
							String code = acalc.substring(0, 1);
							int value = 0;
							//try {
							//	value = Integer.parseInt(acalc.substring(1));
							//} catch(Exception e) {}
							value = 1;
							//System.out.println("A CODE="+code+" VALUE="+value);
							int curvalue = (Integer)results.get(code);
							curvalue += value;
							results.put(code, curvalue);
						} else if (x>50 && x<100) {
							String code = bcalc.substring(0, 1);
							int value = 0;
							//try {
							//	value = Integer.parseInt(acalc.substring(1));
							//} catch(Exception e) {}
							value = 1;
							//System.out.println("A CODE="+code+" VALUE="+value);
							int curvalue = (Integer)results.get(code);
							curvalue += value;
							results.put(code, curvalue);
							
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}

			}
			if (axis!=null) {
				List<FsNode> nodes = axis.getNodes();
				if (nodes != null) {
					for (Iterator<FsNode> iter2 = nodes.iterator(); iter2.hasNext();) {
						FsNode node = (FsNode) iter2.next();
						//System.out.println("ID="+id+" CODE="+node.getId()+" V="+results.get(node.getId()));
						model.setProperty("@station/player['"+id+"']/axis_"+node.getId(),""+results.get(node.getId()));
					}
				}
			}
			
		}
		long endtime = new Date().getTime();
		System.out.println("calc time="+(endtime-starttime));
	}

	
	private void fillPage() {
		JSONObject data = new JSONObject();
		if (feedback) {
			data.put("timeout-msg",""+timeout+" sec");
			data.put("feedback", "true");
			int size=1;
			data.put("axis",axis.toJSONObject("en","name,color,size"));
		}
	
		
		if (question!=null) { // just to make sure
			data.put("question",question.getProperty("question"));	
			data.put("answer1",question.getProperty("answer1"));
			data.put("answer2",question.getProperty("answer2"));
			String feedbackmain = question.getProperty("feedbackmain");
			
			if (feedbackmain!=null) {
				if (feedbackmain.equals("overview")) {
					data.put("overview","true");
				} else 	if (feedbackmain.equals("ad")) {
					data.put("ad","true");
				}
			}
		}
		
		String applogoleft = model.getProperty("@station/content['contentselect']/applogoright");
		if (applogoleft!=null && !applogoleft.equals("")) {
			data.put("applogoleft",applogoleft);
		}
		String applogoright = model.getProperty("@station/content['contentselect']/applogoright");
		if (applogoright!=null && !applogoright.equals("")) {
			data.put("applogoright",applogoright);
		}
		
		screen.get(selector).render(data);
	}
	
	
	/*
	private void getNextStatement() {
		model.setProperty("@itemid",""+itemcounter);
		item = model.getNode("@item");
		
		// ok lets now get the question
		model.setProperty("@itemquestionid",""+questioncounter);
		question = model.getNode("@itemquestion");
		realquestioncounter++;
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
				realquestioncounter = 1;
				model.setProperty("@itemid",""+itemcounter);
				model.setProperty("@itemquestionid",""+questioncounter);
				question = model.getNode("@itemquestion");
			}
		}
		
		loadAnswers(question.getId());
	}
	*/
	
	
	
	private void getNextStatement() {
		//model.setProperty("@itemquestionid",""+questioncounter);
		if (questioncounter<allquestions.size()) {
			questioncounter++;
		} else {
			questioncounter=1;
		}	
		question = allquestions.getNodesById(""+questioncounter).get(0);
		loadAnswers(""+questioncounter);
	}

	
	public void onClientMsg(ModelEvent e) {
		FsNode message = e.getTargetFsNode();
		String clientScreenId = message.getId();
		String command = message.getProperty("command");
		if (command.equals("join")) {
			
			String playername = message.getProperty("username");
			FsNode player = model.getNode("@station/player['"+playername+"']");
			
	
			// should we not ceheck for the old one?
			//FsNode player  = activePlayers.get(playername);
			System.out.println("PLAYER="+playername+" "+player+" "+model.getProperty("@stationid"));
			//System.out.println("PL="+player.getPath());
			if (player==null) {
				// get tge current number of players to find the color 
				FSList players = model.getList("@station/player");
				String newcolor = colorbucket.get(players.size());
				player =  new FsNode("player",playername);
				player.setProperty("playername", playername);
				player.setProperty("color", newcolor);
				player.setProperty("lastseen",""+new Date().getTime());
				player.setProperty("clientScreenId", clientScreenId);
				//player = new WhatWeThinkPlayer(model, playername, clientScreenId,newcolor);
				System.out.println("WANT A INSERT OF GUEST NODE");
				//activePlayers.put(playername, player);
				model.putNode("@station", player);
			} else {
				// update its screenid 
				player.setProperty("clientScreenId", clientScreenId);
				player.setProperty("lastseen",""+new Date().getTime());
			}
			
			//send questionId to players screen id  (daniel should not be a message!)
			FsNode msgnode = new FsNode("msgnode", "1");
			//msgnode.setProperty("itemid", model.getProperty("@itemid"));
			msgnode.setProperty("color", player.getProperty("color"));
			//msgnode.setProperty("itemquestionid", model.getProperty("@itemquestionid"));
			msgnode.setProperty("question", question.getProperty("question"));
			msgnode.setProperty("answer1", question.getProperty("answer1"));
			msgnode.setProperty("answer2", question.getProperty("answer2"));
			msgnode.setProperty("feedback",""+feedback);
			msgnode.setProperty("command","newquestion");
			msgnode.setProperty("questioncount",""+allquestions.size());
			msgnode.setProperty("questionnumber",""+questioncounter);
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
		int inactivecount = 0;
		FSList dots=new FSList();
		FSList fslist = model.getList("@whatwethinkdots");
		if (fslist!=null) {
			for (Iterator<FsNode> iter = fslist.getNodes().iterator(); iter.hasNext();) {
				FsNode qnode = (FsNode) iter.next();
				FsNode nnode = new FsNode("dot",qnode.getId());
				String x = qnode.getProperty("x");
				String y = qnode.getProperty("y");
				String c = qnode.getProperty("c");
				
				if (c!=null) {
					if (!c.equals("#666666")) {
						nnode.setProperty("active","true");
					} else {
						inactivecount++;
					}
				}
				nnode.setProperty("x", x);
				nnode.setProperty("y", y);
				nnode.setProperty("c", c);
				if (inactivecount<50) dots.addNode(nnode);
			}
		}
		JSONObject data = dots.toJSONObject("en","x,y,c,active"); // turn the lists into a json object
		screen.get(selector).render(data,"whatwethink-statements-dots");
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

	private FSList loadAllQuestions() { // not happy about this
		FSList results =new FSList();
		FSList items= model.getList("@items");
		int counter = 1;
		List<FsNode> nodes = items.getNodes();
		if (nodes != null) {
			for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
				FsNode node = (FsNode) iter.next();
				FSList questions= model.getList("@items/"+node.getId()+"/question");
				for (Iterator<FsNode> iter2 = questions.getNodes().iterator(); iter2.hasNext();) {
					FsNode qnode = (FsNode) iter2.next();
					FsNode nnode = new FsNode("question",""+(counter++));
					nnode.setProperty("question",qnode.getProperty("question"));
					nnode.setProperty("answer1",qnode.getProperty("answer1"));
					nnode.setProperty("answer2",qnode.getProperty("answer2"));
					nnode.setProperty("calc",qnode.getProperty("calc"));
					nnode.setProperty("feedbackmain",qnode.getProperty("feedbackmain"));					
					results.addNode(nnode);
				}
			}
		}		
		return results;
	}


	
}
