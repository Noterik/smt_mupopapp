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
package org.springfield.lou.controllers.apps.trivia;

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
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;

public class TriviaController extends Html5Controller {
    
    private static int LEVEL = 1; //for now hardcoded level 1 questions only 
    private static int POINTS = 10;
    
    private int timeout = 0;
    private HashMap<Integer, ArrayList<Integer>> triviaItems;
    private HashMap<String, TriviaPlayer> activePlayers;
    
    private ArrayList<Integer> alreadyAskedItems;
    
    public TriviaController() { }

    public void attach(String sel) {	
	selector = sel;
	model.setProperty("@contentrole", "mainapp");
	
	triviaItems = new HashMap<Integer, ArrayList<Integer>>();
	activePlayers = new HashMap<String, TriviaPlayer>();
	alreadyAskedItems = new ArrayList<Integer>();
	
	initializeQuestions();

	fillPage();
	
	model.onNotify("@stationevents/totriviaserver", "onClientJoin", this);
	model.onNotify("/shared[timers]/1second", "on1SecondTimer", this);
    }

    private void fillPage() {
	JSONObject data = new JSONObject();

	FsNode exhibitionnode = model.getNode("@exhibition");
	FsNode item = model.getNode("@item");

	if (item == null) {
	    item = getNextItem();
	}
	
	//placed here as itemid is dynamic!
	System.out.println(" itemid = "+model.getProperty("@itemid"));
	model.onNotify("@itemanwser", "onClientAnswer", this);

	String timeouts = item.getProperty("timeout");
	timeout = 10;
	
	try {
	    timeout = Integer.parseInt(timeouts);
	} catch (Exception e) { }
	
	System.out.println("TIMEOUT=" + timeout);

	data.put("imageurl", item.getProperty("imageurl"));
	data.put("type", "trivia-image-"+item.getProperty("type"));
	
	data.put("domain", LazyHomer.getExternalIpNumber());
	data.put("jumper", exhibitionnode.getProperty("jumper"));	
	
	//update scores
	int i = 1;
	
	ArrayList<JSONObject> scores = new ArrayList<JSONObject>();

	for (Map.Entry<String, TriviaPlayer> player : activePlayers.entrySet()) {
	    JSONObject score = new JSONObject();
	    
	    TriviaPlayer triviaPlayer = player.getValue();
	    
	    score.put("playername", triviaPlayer.getPlayerName());
	    score.put("score", triviaPlayer.getScore());
	    score.put("answeredcorrect", triviaPlayer.getAnsweredCorrect() == true ? "correct" : "incorrect");
	    score.put("rank", i);
	    i++;	    
	    
	    scores.add(score);
	}
	data.put("scores", scores);
	
	if (activePlayers.size() > 0) {
	    data.put("multiplier", activePlayers.size());
	}
	
	//update highscores
	i = 1;
	
	ArrayList<JSONObject> highscores = new ArrayList<JSONObject>();
	    
	NavigableMap<Integer, String> hscores = updateHighScoreList();
	
	for (Map.Entry<Integer, String> highscore : hscores.descendingMap().entrySet()) {
	    if (i > 10) {
		break;
	    }
	    
	    JSONObject score = new JSONObject();
	    
	    score.put("playername", highscore.getValue());
	    score.put("highscore", highscore.getKey());
	    score.put("rank", i);
	    i++;	    
	    
	    highscores.add(score);
	}
	data.put("highscores", highscores);
	
	screen.get(selector).render(data);
	screen.get(selector).loadScript(this);

	JSONObject d = new JSONObject();
	d.put("command", "init");
	screen.get(selector).update(d);

	//assign every player a random question
	sendRandomQuestions();
	
	d = new JSONObject();	
	d.put("command","timeupdate");
	d.put("time", timeout);
	screen.get(selector).update(d);
    }
    
    public void onClientJoin(ModelEvent e) {
	FsNode message = e.getTargetFsNode();
	String clientScreenId = message.getId();
	String playername = message.getProperty("playername");

	TriviaPlayer player = new TriviaPlayer(model, playername, clientScreenId);
	activePlayers.put(playername, player);	
    }

    public void onClientAnswer(ModelEvent e) {
	FsNode answer = e.getTargetFsNode();
	String answerId = answer.getProperty("value");
	String playername = answer.getProperty("playername");
	
	System.out.println("CLIENTS=" + answer.asXML());
	
	if (activePlayers.containsKey(playername)) {
	    activePlayers.get(playername).setAnswerId(Integer.parseInt(answerId));
	} else {
	    //Got answer from non active player,
	    //refresh this clients screen
	}
    }

    public void on1SecondTimer(ModelEvent e) {
	JSONObject d = new JSONObject();	
	d.put("command","timeupdate");
	d.put("time", timeout);
	screen.get(selector).update(d);
	
	if (timeout > 0) {
	    timeout = timeout - 1;
	    
	} else {
	    handleAnswers();
	    getNextItem();
	    fillPage();
	}
    
	System.out.println("1 SEC TIMER " + timeout);
    }

    private FsNode getNextItem() {	
	//get all available questions from a certain level
	//make shallow copy, otherwise we alter the original with the removeAll() action
	ArrayList<Integer> availableItems = new ArrayList(triviaItems.get(LEVEL));

	System.out.println("Already asked questions "+alreadyAskedItems.size());
	
	//get items not yet asked
	availableItems.removeAll(alreadyAskedItems);
	
	if (availableItems.size() <= 0) {
	    //all available questions asked, reset
	    System.out.println("All questions already asked, reset");
	    
	    //TODO: we could initialize again all questions here again, we might have new ones
	    
	    availableItems = new ArrayList(triviaItems.get(LEVEL));
	    alreadyAskedItems = new ArrayList<Integer>(availableItems.size());
	}
	
	System.out.println("Total not yet asked questions "+availableItems.size());
	
	int itemId = availableItems.get(new Random().nextInt(availableItems.size()));

	model.setProperty("@itemid", Integer.toString(itemId));	
	FsNode item = model.getNode("@item");
	
	//ok, asked this, so put it in the asked list
	alreadyAskedItems.add(itemId);
	
	return item;
    }
    
    //Initialize a list of all item ids per level
    private void initializeQuestions() {
	FSList items = model.getList("@items");
	
	if (items.size() > 0) {
	    // loop over all items and group them per level
	    for (Iterator<FsNode> iter = items.getNodes().iterator(); iter.hasNext();) {
		FsNode triviaItem = (FsNode) iter.next();
		int itemId = Integer.parseInt(triviaItem.getId());
		int itemLevel = Integer.parseInt(triviaItem.getProperty("level"));
		
		ArrayList<Integer> levelItems;
		
		if (triviaItems.containsKey(itemLevel)) {
		    levelItems = triviaItems.get(itemLevel);
		} else {
		    levelItems = new ArrayList<Integer>();
		}
		
		levelItems.add(itemId);
		triviaItems.put(itemLevel, levelItems);
	    }
	}
    }
    
    //Send all players a random question
    private void sendRandomQuestions() {
	FSList questions = model.getList("@itemquestions");
	
	ArrayList<Integer> allQuestions = new ArrayList<Integer>();
	
	if (questions.size() > 0) {	       
	    // loop over all items and group them per level
	    for (Iterator<FsNode> iter = questions.getNodes().iterator(); iter.hasNext();) {
		FsNode question = (FsNode) iter.next();		   
		int itemId = Integer.parseInt(question.getId());
		   
		allQuestions.add(itemId);
	    }
	} else {
	    //no questions, we can't do much now
	    return;
	}
	
	ArrayList<Integer> availableQuestions = new ArrayList(allQuestions);
	ArrayList<Integer> assignedQuestions = new ArrayList<Integer>();
	
	System.out.println("Total number of available questions = "+availableQuestions);
	
	//Make sure we first use all questions before we assign duplicates
	for (Map.Entry<String, TriviaPlayer> player : activePlayers.entrySet()) {
	    //get items not yet asked
	    availableQuestions.remove(assignedQuestions);
	    
	    if (availableQuestions.size() == 0) {
		System.out.println("All questions assigned");
		availableQuestions = new ArrayList(allQuestions);
		assignedQuestions = new ArrayList<Integer>();
	    }

	    int questionId = availableQuestions.get(new Random().nextInt(availableQuestions.size()));
	    
	    System.out.println("assign question "+questionId+" to screen "+player.getValue().getClientId());
	    
	    player.getValue().setQuestionId(questionId);
	    
	    //ok, assigned this question
	    assignedQuestions.add(questionId);
	    
	    //send questionId to players screen id
	    FsNode msgnode = new FsNode("msgnode", "1");
	    msgnode.setProperty("itemid", model.getProperty("@itemid"));
	    msgnode.setProperty("questionid", Integer.toString(questionId));
	    msgnode.setProperty("screenid", player.getValue().getClientId());
	    model.notify("@appstate", msgnode);
	}
    }
    
    //loop over all players to check there answers
    private void handleAnswers() {
	//First get all questions and answers
	FSList questions = model.getList("@itemquestions");
	HashMap<Integer, Integer> questionAndAnswersMapping = new HashMap<Integer, Integer>();
	
	if (questions.size() > 0) {	       
	    // loop over all items and group them per level
	    for (Iterator<FsNode> iter = questions.getNodes().iterator(); iter.hasNext();) {
		FsNode question = (FsNode) iter.next();		   
		int itemId = Integer.parseInt(question.getId());
		int answerId = Integer.parseInt(question.getProperty("correctanswer"));
		questionAndAnswersMapping.put(itemId, answerId);
	    }
	} else {
	    return;
	}
	
	boolean getPoints = true;
	int playersWithQuestions = 0;
	ArrayList<String> playersToBeRemoved = new ArrayList<String>();
	
	//Check if everybody answered correctly
	for (Map.Entry<String, TriviaPlayer> player : activePlayers.entrySet()) {
	    TriviaPlayer triviaPlayer = player.getValue();	    
	    
	    //check if player had a question assigned, otherwise it just joined
	    if (triviaPlayer.getQuestionId() > 0) {
		System.out.println("Player "+player.getKey()+" answered "+triviaPlayer.getAnswerId());
		System.out.println("Correct answer = "+questionAndAnswersMapping.get(triviaPlayer.getQuestionId()));
		playersWithQuestions++;
		
		if (triviaPlayer.getAnswerId() > 0) {
		    if (questionAndAnswersMapping.get(triviaPlayer.getQuestionId()) == triviaPlayer.getAnswerId()) {
			//Question answered correct!
			System.out.println("Answered correctly by "+player.getKey());
			player.getValue().setAnsweredCorrect(true);
		    } else {
			//Question answered incorrect
			getPoints = false;
			System.out.println("Answered incorrectly by "+player.getKey());
			player.getValue().setAnsweredCorrect(false);
		    }
		} else {
		    //player did not answer question, remove from active players and give notice
		    playersToBeRemoved.add(player.getKey());
		    
		    System.out.println("No answer from "+player.getKey());
		    
		    //I'm nice so other players still get their points
		    //getPoints = false;
		}
	    }
	}
	
	//remove players who didn't answer 
	for (String playerToBeRemoved : playersToBeRemoved) {
	    FsNode msgnode = new FsNode("msgnode", "1");
	    msgnode.setProperty("remove", "true");
	    msgnode.setProperty("screenid", activePlayers.get(playerToBeRemoved).getClientId());
	    model.notify("@appstate", msgnode);
	    
	    activePlayers.remove(playerToBeRemoved);
	}
	
	//Assign points to players
	for (Map.Entry<String, TriviaPlayer> player : activePlayers.entrySet()) {
	    TriviaPlayer triviaPlayer = player.getValue();	
	    triviaPlayer.setAnswerId(0);
	    
	    //only points for active player that got a question last round
	    if (triviaPlayer.getQuestionId() > 0) {
		if (getPoints) {
		    triviaPlayer.setScore(triviaPlayer.getScore()+POINTS*playersWithQuestions);
		} else {
		    triviaPlayer.setScore(0);
		}
	    }
	}
    }
    
    //update high scores
    private TreeMap<Integer, String> updateHighScoreList() {
	System.out.println("Checking high scores");
	
	Map<Integer, String> scores = new HashMap<Integer, String>();

	FSList players = model.getList("@triviaplayerslist/player");
	
	if (players.size() > 0) {
	    for (Iterator<FsNode> iter = players.getNodes().iterator(); iter.hasNext();) {
		FsNode player = (FsNode) iter.next();
		String name = player.getProperty("name");
		String hs = player.getProperty("highscore");
		
		System.out.println("player "+name+" highscore "+hs);
		
		if (hs != null && !hs.equals("")) {
		    scores.put(Integer.parseInt(hs), name);
		}
	    }
	    return new TreeMap<Integer, String>(scores);
	}
	
	return new TreeMap<Integer, String>();
    }
}
