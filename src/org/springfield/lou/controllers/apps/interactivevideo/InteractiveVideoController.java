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
package org.springfield.lou.controllers.apps.interactivevideo;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.application.types.util.MasterClockManager;
import org.springfield.lou.application.types.util.MasterClockThread;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.controllers.apps.entryscreen.ImageRotationEntryScreenController;
import org.springfield.lou.controllers.apps.entryscreen.StaticEntryScreenController;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelBindEvent;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;
import org.springfield.marge.Marge;

public class InteractiveVideoController extends Html5Controller {

	String sharedspace;
	String audiourl;
	String timelineid;
	MasterClockThread c;
	Boolean feedback = false;
	int feedbackcounter = 8;
	
	static Random generator;

	public InteractiveVideoController() {
	}

	public void attach(String sel) {
    	generator = new Random(System.currentTimeMillis());
		selector = sel;
		sharedspace = model.getProperty("/screen/sharedspace");
		screen.get(selector).loadScript(this);	
		//model.setProperty("@videoid", ""+1);

		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");

		model.setProperty("@contentrole","mainapp");
		String selecteditem = model.getProperty("@selecteditem");
		System.out.println("CHECK2");
		FsNode itemnode = null;
		if (selecteditem!=null) {
			model.setProperty("@itemid",selecteditem);
			itemnode = model.getNode("@item");
		} else {
			FSList items = model.getList("@items");
			if (items.size()>0) {
				itemnode = items.getNodes().get(0);
				model.setProperty("@itemid",itemnode.getId());
			}

		}
		
		//getTicket("test");
		String videourl="/stream4/domain/euscreen/user/eu_memoriav_rts/video/156";
		String ticket  = sendTicket(videourl);

		FsNode stationnode = model.getNode("@station");
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			//data.put("url",itemnode.getProperty("videourl"));
			if (ticket!=null) {
				data.put("url","http://stream.noterik.com/progressive"+videourl+"/rawvideo/1/raw.mp4");
				data.put("ticket",ticket);
			} else {
				data.put("url",videourl);
			}
			audiourl = itemnode.getProperty("audiourl");
			System.out.println("VIDEO URL="+itemnode.getProperty("videourl"));
			System.out.println("AUDIO URL="+audiourl);
			data.put("title", stationnode.getSmartProperty("en", "title"));
			data = addMetaData(data);
			screen.get(selector).render(data);
		}



		MasterClockManager.setApp(this);
		c = MasterClockManager.getMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
		if(c == null) {
			c = MasterClockManager.addMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
			c.setVideoLength(517000);
			c.start();
		} else {
			System.out.println("RESTART VIDEOSTATION");
			c.restart();	
		}

		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime", "onClockUpdate", this);
		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/exhibitionEnded", "onVideoEnd", this);

		playVideo();

		timelineid = "/domain['mupop']/user['"+model.getProperty("@username")+"']/exhibition['"+model.getProperty("@exhibitionid")+"']/station['"+model.getProperty("@stationid")+"']/content['"+model.getProperty("@contentrole")+"']/item['"+model.getProperty("@itemid")+"']/question";
		model.onTimeLineNotify(timelineid,"/shared/mupop/exhibition/"+exhibitionid+"/station/"+ stationid+"/currenttime","starttime","duration","onTimeLineEvent",this);
		model.onNotify("@stationevents/fromclient","onClientStationEvent",this);

		model.onNotify("/shared[timers]/1second","on1SecondTimer",this); 
	}

	public void on1SecondTimer(ModelEvent e) {
		if (feedback) {
			feedbackcounter--;
			if (feedbackcounter<0) {
				feedbackcounter=8;
				feedback=false;
				JSONObject data = new JSONObject();
				data = addMetaData(data);
				screen.get("#interactivevideo_lowerthird").render(data);
			}
		}
	}


	public void onClientStationEvent(ModelEvent e) {
		FsNode message = e.getTargetFsNode();
		String from = message.getId();
		String command = message.getProperty("action");
		//System.out.println("CLIENT REQUEST ACTION="+command);
		if (command.equals("playrequest")) {
			Screen client = ApplicationManager.getScreenByFullid(from);
			FsPropertySet ps = new FsPropertySet(); 
			ps.setProperty("action","playaudio");  
			ps.setProperty("audiourl",audiourl);  
			ps.setProperty("timeline",timelineid);
			ps.setProperty("duration","472000");
			ps.setProperty("streamtime",""+c.getStreamTime());
			client.getModel().setProperties("/screen/audiocommand",ps);

		}
	}

	public void onTimeLineEvent(ModelEvent e) {
		if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_ENTER) {
			System.out.println("Station:: GOT TIME EVENT ENTER");
			if(e.getTargetFsNode().getName().equals("question")){
				//pauseVideo();
				screen.get("#lowerthird").hide();
				screen.get("#exhibition").append("div","questionscreen", new QuestionScreenController());
				String stationid = model.getProperty("@stationid");
				String exhibitionid = model.getProperty("@exhibitionid");
				//MasterClockManager.setApp(this);
				//MasterClockThread c = MasterClockManager.getMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
				//if (c != null) {
				//		c.freezeFor(Long.parseLong(e.getTargetFsNode().getProperty("duration")));
				//	}
			}
		} else if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_LEAVE) {
			//System.out.println("Station:: GOT TIME EVENT END");
			if(e.getTargetFsNode().getName().equals("question")){
				screen.get("#questionscreen").remove();
				screen.get("#interactivevideo_lowerthird").show();
				JSONObject data = new JSONObject();
				//data = addMetaData(data);
				//System.out.println("DATA="+data.toJSONString());
				data.put("feedback", "true");
				FsNode uncachednode = model.getNode(e.getTargetFsNode().getPath());
				String correctanswersstring =uncachednode.getProperty("correctanswers");
				String totalanswersstring =uncachednode.getProperty("totalanswers");
				System.out.println("TA="+correctanswersstring+" "+totalanswersstring);
				try {
					int correctanswers = Integer.parseInt(correctanswersstring);
					int totalanswers = Integer.parseInt(totalanswersstring);
					
					float result = (correctanswers/(float)totalanswers)*100;
					data.put("goodperc",Math.round(result));
					screen.get("#interactivevideo_lowerthird").render(data);
					feedback = true;
				} catch(Exception error) {}
			}
		}
	}
	


	public void onVideoEnd(ModelEvent e){
		System.out.println("Station:: VIDEO ENDED RESTART IT");
		playVideo();
	}



	public void playVideo(){
		JSONObject nd = new JSONObject();
		nd.put("action","play");
		nd.put("target", "interactivevideo_video_c");
		screen.get(selector).update(nd);
	}


	public void onClockUpdate(ModelEvent e) {
		System.out.println("ClockUpdate");
		JSONObject nd = new JSONObject();
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		nd.put("action","wantedtime");
		nd.put("target", "interactivevideo_video_c");
		nd.put("wantedtime", model.getProperty("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime"));
		System.out.println("wanted time:"+model.getProperty("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime")+" this="+this.hashCode());
		screen.get(selector).update(nd);
	}



	private JSONObject addMetaData(JSONObject data) {
		// language_interactivevideo_main_screen
		
		
		String language = model.getProperty("@exhibition/language");
		// fixed for now ?
		language = "nl";
		data.put("language", language);
		FsNode language_content = model.getNode("@language_static_entry_screen");
		System.out.println("LANGSEL="+language);
		System.out.println("MY NODE LANG="+language_content.asXML());
		data.put("goto", language_content.getSmartProperty(language, "goto"));
		data.put("andenter", language_content.getSmartProperty(language, "andenter"));
		data.put("tocontrolthescreen", language_content.getSmartProperty(language, "tocontrolthescreen"));
		data.put("tocontrolthescreen2", language_content.getSmartProperty(language, "tocontrolthescreen2"));
		data.put("entercode", language_content.getSmartProperty(language, "entercode"));
		data.put("andselect", language_content.getSmartProperty(language, "andselect"));
		data.put("selectstation", language_content.getSmartProperty(language, "selectstation"));
		data.put("jumper", model.getProperty("@exhibition/jumper"));
		data.put("domain", LazyHomer.getExternalIpNumber());
		data.put("name", model.getProperty("@station/name"));
		data.put("labelid", model.getProperty("@station/labelid"));
		String showurl = model.getProperty("@exhibition/showurl");
		if (showurl!=null && showurl.equals("true")) {
			data.put("showurl","true");
		}
		String fullcode = model.getProperty("@station/codeselect");
		data.put("codeselect",fullcode);
		return data;
	}
	
	private static String sendTicket(String videoFile) {
		String ipAddress = LazyHomer.getExternalIpNumber();
		String random = ""+generator.nextInt(999999999);
		String ticket = "mst_"+LazyHomer.getExternalIpNumber()+"_"+random;
		try {
		URL serverUrl = new URL("http://82.94.187.227:8001/acl/ticket");
		HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();
	
		Long Sytime = System.currentTimeMillis();
		Sytime = Sytime / 1000;
		String expiry = Long.toString(Sytime+(15*60));
		
		// Indicate that we want to write to the HTTP request body
		
		urlConnection.setDoOutput(true);
		urlConnection.setRequestMethod("POST");
		videoFile=videoFile.substring(1);
	
		System.out.println("I send this video address to the ticket server:"+videoFile);
		System.out.println("And this ticket:"+ticket);
		System.out.println("And this EXPIRY:"+expiry);
		
		// Writing the post data to the HTTP request body
		BufferedWriter httpRequestBodyWriter = 
		new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
			String content = "<fsxml><properties><ticket>"+ticket+"</ticket>"
			+ "<uri>/"+videoFile+"</uri><ip>"+ipAddress+"</ip> "
			+ "<role>user</role>"
			+ "<expiry>"+expiry+"</expiry><maxRequests>10</maxRequests></properties></fsxml>";
		httpRequestBodyWriter.write(content);
		httpRequestBodyWriter.close();
	
		// Reading from the HTTP response body
		Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
		while(httpResponseScanner.hasNextLine()) {
			System.out.println(httpResponseScanner.nextLine());
		}
		httpResponseScanner.close();			
		} catch(Exception e) {
			e.printStackTrace();	
		}
		return ticket;
	}

}
