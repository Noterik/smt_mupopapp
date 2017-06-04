/* 
* TriviaPlayer.java
* 
* Copyright (c) 2017 Noterik B.V.
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
package org.springfield.lou.controllers.apps.whatwethink;

import java.util.Iterator;

import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.model.Model;

/**
 * TriviaPlayer.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2017
 * @package org.springfield.lou.controllers.apps.trivia
 * 
 */
public class WhatWeThinkPlayer {
    
    private String playername;
    private int score;
    private int highscore;
    private int questionId;
    private int answerId;
    private String clientId;
    private boolean answeredCorrect;
    
    private Model model;
    private FsNode playerDetails;
    
    public WhatWeThinkPlayer() { }
    
    public WhatWeThinkPlayer(Model model, String playername, String clientId) {
	this.model = model;
	this.playername = playername;
	this.clientId = clientId;
	score = 0;
	questionId = 0;
	answerId = 0;
	answeredCorrect = false;
	System.out.println("PLAYERNAME="+playername);
	
	model.setProperty("@whatwethinkplayerid", playername.toLowerCase());
	playerDetails = model.getNode("@whatwethinkplayer");
	
	//TODO: I would rather use model.getNode("@triviaplayerslist/player['"+playername.toLowerCase()+"']"); 
	//but this gives nullpointer
	//playerDetails = model.getNode("@triviaplayerslist/player['"+playername.toLowerCase()+"']");
	
	
	/*FSList players = model.getList("@triviaplayerslist/player");
	
	if (players.size() > 0) {
	    // loop over all players, sub ideal
	    for (Iterator<FsNode> iter = players.getNodes().iterator(); iter.hasNext();) {
		FsNode player = (FsNode) iter.next();
		String pname = player.getId();

		if (playername.toLowerCase().equals(pname.toLowerCase())) {
		    playerDetails = player;
		}
	    }
	}*/
    
	if (playerDetails != null) {
	    System.out.println(playerDetails.asXML());
	    this.highscore = Integer.parseInt(playerDetails.getProperty("highscore"));
	} else {
	    System.out.println("player not found");
	}
    }
    
    public String getPlayerName() {
    	return playername;
    }
    
    public int getScore() {
    	return score;
    }
    
    public int getHighScore() {
    	return highscore;
    }
    
    public int getQuestionId() {
    	return questionId;
    }
    
    public int getAnswerId() {
    	return answerId;
    }
    
    public String getClientId() {
    	return clientId;
    }
    
    public boolean getAnsweredCorrect() {
    	return answeredCorrect;
    }
    
    public void setScore(int score) {
	this.score = score;
	if (score > highscore) {
	    //store new highscore
	    System.out.println("Setting high score for "+playername+" to "+score);
	    
	    model.setProperty("@triviaplayerid", playername.toLowerCase());
	    model.setProperty("@triviaplayer/highscore", Integer.toString(score));
	}
    }

    public void setQuestionId(int questionId) {
	this.questionId = questionId;
    }
    
    public void setAnswerId(int answerId) {
	this.answerId = answerId;
    }
    
    public void setAnsweredCorrect(boolean b) {
	this.answeredCorrect = b;
    }
}
