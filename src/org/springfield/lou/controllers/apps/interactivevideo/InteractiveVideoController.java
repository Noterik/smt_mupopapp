package org.springfield.lou.controllers.apps.interactivevideo;


import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.controllers.apps.entryscreen.ImageRotationEntryScreenController;
import org.springfield.lou.controllers.apps.entryscreen.StaticEntryScreenController;
import org.springfield.lou.controllers.apps.interactivevideo.clocksync.MasterClockManager;
import org.springfield.lou.controllers.apps.interactivevideo.clocksync.MasterClockThread;
import org.springfield.lou.model.ModelBindEvent;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;
import org.springfield.marge.Marge;

public class InteractiveVideoController extends Html5Controller {
	
	String sharedspace;
	String audiourl;
	String timelineid;
	
	public InteractiveVideoController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		sharedspace = model.getProperty("/screen/sharedspace");
		screen.get(selector).loadScript(this);	
		System.out.println("CHECK1");
		//model.setProperty("@videoid", ""+1);
		
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		//String path = model.getProperty("/screen/exhibitionpath");	
		//String video = model.getProperty("@video");
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
		
		FsNode stationnode = model.getNode("@station");
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			data.put("url",itemnode.getProperty("videourl"));
			audiourl = itemnode.getProperty("audiourl");
			data.put("title", stationnode.getSmartProperty("en", "title"));
			screen.get(selector).render(data);
		}
		
			
		/*
		MasterClockManager.setApp(this);
		MasterClockThread c = MasterClockManager.getMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
		if(c == null) {
			c = MasterClockManager.addMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
			c.setVideoLength(313000);
			c.start();
		} else {
			c.restart();	
		}
		*/
		
		//model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/play", "onPlayEvent", this);
		//model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/pause", "onPauseEvent", this);
		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime", "onClockUpdate", this);
		//model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/exhibitionEnded", "onExhibitionEnd", this);
		//model.onNotify("@exhibition/entryscreen/requested", "onEntryScreenRequested", this);
		
		playVideo();
		timelineid = "/domain['mupop']/user['"+model.getProperty("@username")+"']/exhibition['"+model.getProperty("@exhibitionid")+"']/station['"+model.getProperty("@stationid")+"']/content['"+model.getProperty("@contentrole")+"']/item['"+model.getProperty("@itemid")+"']/question";
		 System.out.println("DEEEE="+timelineid);
		model.onTimeLineNotify(timelineid,"/shared/mupop/exhibition/"+exhibitionid+"/station/"+ stationid+"/currenttime","starttime","duration","onTimeLineEvent",this);
		model.onNotify("@stationevents/fromclient","onClientStationEvent",this);
		System.out.println("ADDING 1Sec timers");
		model.onNotify("/shared[timers]/1second","on1SecondTimer",this);
		model.onNotify("/shared[timers]/2second","on2SecondTimer",this);
		model.onNotify("/shared[timers]/5second","on5SecondTimer",this);
		model.onNotify("/shared[timers]/10second","on10SecondTimer",this);
	}
	
	public void on1SecondTimer(ModelEvent e) {
		System.out.println("1T="+e.getTargetFsNode().getProperty("value"));
	}
	
	public void on2SecondTimer(ModelEvent e) {
		System.out.println("2T="+e.getTargetFsNode().getProperty("value"));
	}
	
	public void on5SecondTimer(ModelEvent e) {
		System.out.println("5T="+e.getTargetFsNode().getProperty("value"));
	}
	
	public void on10SecondTimer(ModelEvent e) {
		System.out.println("10T="+e.getTargetFsNode().getProperty("value"));
	}
	
	public void onClientStationEvent(ModelEvent e) {
    	FsNode message = e.getTargetFsNode();
    	String from = message.getId();
    	String command = message.getProperty("action");
      	System.out.println("CLIENT REQUEST ACTION="+command);
    	if (command.equals("playrequest")) {
			Screen client = ApplicationManager.getScreenByFullid(from);
			 FsPropertySet ps = new FsPropertySet(); 
			 ps.setProperty("action","playaudio");  
			 ps.setProperty("audiourl",audiourl);  
			 ps.setProperty("timeline",timelineid);
			client.getModel().setProperties("/screen/audiocommand",ps);
    		
    	}
	}

	public void onTimeLineEvent(ModelEvent e) {
		if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_ENTER) {
			System.out.println("Station:: GOT TIME EVENT ENTER");
			if(e.getTargetFsNode().getName().equals("question")){
				//pauseVideo();
				System.out.println("GOT QUESTION EVENT!");
					System.out.println("SCREEN MANAGER HERE!!!");
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
			System.out.println("Station:: GOT TIME EVENT END");
			if(e.getTargetFsNode().getName().equals("question")){
				screen.get("#questionscreen").remove();
				//playVideo();

			}
		}
	}
	
	public void onExhibitionEnd(ModelEvent e){
		System.out.println("Station:: Exhibition Ended");
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		model.setProperty("/app/interactivevideo/exhibition/" +exhibitionid+ "/station/"+ stationid +"/vars/hasClockManager", "false");
		screen.get("#exhibition").append("div","staticentryscreen", new StaticEntryScreenController());
		screen.get(selector).remove();
		
	}
	
	/*
	public void onPlayEvent(ModelEvent e){
		System.out.println("Play Event");
		playVideo();
		
	}
	*/
	
	/*
	public void onPauseEvent(ModelEvent e){
		System.out.println("Pause Event");
		pauseVideo();
		
	}
	*/
	
	/*
	public void onUserJoined(ModelEvent e){
		System.out.println("User Joined");
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		
		if(model.getProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying").equals("false")) return;
		
		MasterClockManager.setApp(this);
		MasterClockThread c = MasterClockManager.getMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
		if (c == null) {
			c = MasterClockManager.addMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
			System.out.println("Sending start signal to thread");
			c.start();
		} else {
			c.restart();
		}
	
	}
	*/
	
	/*
	public void pauseClock(ModelEvent e){
		System.out.println("Pause Clock Event");
		pauseVideo();
		
		if (model.getProperty("/screen/isClockManager").equals("false")) return;
		
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "false");
		MasterClockManager.setApp(this);
		MasterClockThread c = MasterClockManager.getMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
		
		if (c == null )
			c = MasterClockManager.addMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
		if (c.running())
			c.pause();
	}
	*/
	
	public void playVideo(){
		JSONObject nd = new JSONObject();
		nd.put("action","play");
		nd.put("target", "interactivevideo_video_c");
		screen.get(selector).update(nd);
	}

	/*
	public void pauseVideo(){
		JSONObject nd = new JSONObject();
		nd.put("action","pause");
		nd.put("target", "interactivevideo_video_c");
		screen.get(selector).update(nd);
	}
	*/
	
	/*
	public void seekVideo(long milliseconds){
		JSONObject nd = new JSONObject();
		nd.put("action","seek");
		nd.put("target", "interactivevideo_video_c");
		nd.put("seekingvalue", milliseconds+"");
		screen.get(selector).update(nd);
	}
	*/
	
	public void onClockUpdate(ModelEvent e) {
		//System.out.println("ClockUpdate");
		JSONObject nd = new JSONObject();
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		nd.put("action","wantedtime");
		nd.put("target", "interactivevideo_video_c");
		nd.put("wantedtime", model.getProperty("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime"));
		screen.get(selector).update(nd);
	}
	
	/*
	public void destroyed() {
		super.destroyed();
		if (model.getProperty("/screen/isClockManager").equals("false")) return;
		System.out.println("Clock Master exiting..");
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");	
		model.setProperty("/app/interactivevideo/exhibition/" +exhibitionid+ "/station/"+ stationid +"/vars/hasClockManager", "false");	
	}
	*/
	
	/*
	public void startExhibition(ModelEvent e){
	    screen.get("#exhibition").append("div","interactivevideo_app", new InteractiveVideoController());
	    screen.get("#staticentryscreen").remove();
	}
	*/
}
