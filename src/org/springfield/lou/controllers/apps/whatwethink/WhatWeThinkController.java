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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.controllers.apps.trivia.TriviaPlayer;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;


public class WhatWeThinkController extends Html5Controller {
	
	private int timeout = 0;
	private int itemcounter=1;
	private int questioncounter=1;
	private FsNode item = null;
	private FsNode question = null;
	
	private boolean feedback = true;
	private HashMap<String,WhatWeThinkPlayer> activePlayers;
	
	public WhatWeThinkController() { }
	
	public void attach(String sel) {	
		selector = sel;
		model.setProperty("@contentrole", "mainapp");
		System.out.println("WHAT WE THINK STARTED !");
		activePlayers = new HashMap<String, WhatWeThinkPlayer>();
		fillPage();
		model.onNotify("/shared[timers]/1second", "on1SecondTimer", this);
	}
	
	public void on1SecondTimer(ModelEvent e) {
		if (timeout > 0) {
			timeout = timeout - 1;

		} else {
			if (activePlayers.size() > 0) {
				feedback = !feedback;
				if (feedback) {
					timeout = 5;
					//handleAnswers();	
					fillPage();
					//FsNode msgnode = new FsNode("msgnode", "1");
					//msgnode.setProperty("command","feedbackstate");
					//msgnode.setProperty("feedback", ""+feedback);
					//model.notify("@appstate", msgnode);
				} else {
					timeout = 10;
					getNextStatement();
					fillPage();
					//sendRandomQuestions();
				}
			} else {	
				feedback = !feedback;
				if (feedback) {
					timeout = 10;	
					fillPage();
					//FsNode msgnode = new FsNode("msgnode", "1");
					//msgnode.setProperty("command","feedbackstate");
					//msgnode.setProperty("feedback", ""+feedback);
					//model.notify("@appstate", msgnode);
				} else {
					timeout = 5;
					getNextStatement();
					fillPage();
					//sendRandomQuestions();
				}
			}
		}
		/*
		FsNode msgnode = new FsNode("msgnode", "1");
		msgnode.setProperty("itemid", model.getProperty("@itemid"));
		msgnode.setProperty("command","timer");
		msgnode.setProperty("timer", ""+timeout);
		model.notify("@appstate", msgnode);
		screen.get("#trivia-nexttimer").html(""+timeout);
		screen.get("#trivia-answertimer").html(""+timeout);
		*/
		
		System.out.println("WWT: timeout "+timeout+" players="+activePlayers.size()+" feedback="+feedback);
		screen.get("#whatwethink-timer-one").html(""+timeout+" sec");
		screen.get("#whatwethink-timer-two").html("next statement in "+timeout+" sec");
	}

	
	private void fillPage() {
		System.out.println("WHAT WE THINK : Fill page");
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
		System.out.println("WWT: get next statement");
		model.setProperty("@itemid",""+itemcounter);
		item = model.getNode("@item");
		if (item!=null) {
			System.out.println("ITEM="+item);
		} else {
			// load the next item !
		}
		
		// ok lets now get the question
		model.setProperty("@itemquestionid",""+questioncounter);
		question = model.getNode("@itemquestion");
		if (question!=null) {
			System.out.println("QUESTION="+item.asXML());	
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
		
	}

	
}
