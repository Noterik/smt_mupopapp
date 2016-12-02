package org.springfield.lou.controllers.apps.interactivevideo;


import org.json.simple.JSONObject;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.controllers.apps.entryscreen.StaticEntryScreenController;
import org.springfield.lou.controllers.apps.interactivevideo.clocksync.MasterClockManager;
import org.springfield.lou.controllers.apps.interactivevideo.clocksync.MasterClockThread;
import org.springfield.lou.model.ModelBindEvent;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;
import org.springfield.marge.Marge;

public class InteractiveVideoController extends Html5Controller {
	
	String sharedspace;
	
	public InteractiveVideoController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		sharedspace = model.getProperty("/screen/sharedspace");
		screen.get(selector).loadScript(this);	
		//model.setProperty("@videoid", ""+1);
		
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		String path = model.getProperty("/screen/exhibitionpath");	
		String video = model.getProperty("@video");
		
		FsNode stationnode = model.getNode(path);
		if (stationnode!=null) {
			JSONObject data = new JSONObject();
			data.put("url","http://images3.noterik.com/mupop/test2.mp4");
			data.put("title", stationnode.getSmartProperty("en", "title"));
			screen.get(selector).render(data);
		}
		
	//	String hasClockManager = model.getProperty("/app/interactivevideo/exhibition/" +exhibitionid+ "/station/"+ stationid +"/vars/hasClockManager");
	//	if ( hasClockManager == null || hasClockManager.equals("false")){
			System.out.println("New/take over clock manager screen");
//			model.setProperty("/screen/isClockManager", "true");
//			model.setProperty("/app/interactivevideo/exhibition/" +exhibitionid+ "/station/"+ stationid +"/vars/hasClockManager", "true");
//			model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "false");
			
			MasterClockManager.setApp(this);
			MasterClockThread c = MasterClockManager.getMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
			if(c == null) {
				c = MasterClockManager.addMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
				c.setVideoLength(313000);
				c.start();
			} else {
				c.restart();	
			}
	//	}
	//	else{
	//		System.out.println("IS NOT CLOCK MANAGER FFS");
	//		model.setProperty("/screen/isClockManager", "false");
	//	}
		
		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/play", "onPlayEvent", this);
		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/pause", "onPauseEvent", this);
		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime", "onClockUpdate", this);
		model.onNotify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/exhibitionEnded", "onExhibitionEnd", this);
		
		playVideo();
//		System.out.println("video path:::: " + video);
		
		model.onTimeLineNotify("/domain/mupop/user/daniel/exhibition/"+exhibitionid+"/station/"+stationid+"/video/1","/shared/mupop/exhibition/"+exhibitionid+"/station/"+ stationid+"/currenttime","starttime","duration","onTimeLineEvent",this);
		//model.onTimeLineNotify("@video","/shared/mupop/exhibition/"+exhibitionid+"/station/"+ stationid+"/currenttime","starttime","duration","onTimeLineEvent",this);
		
	}
	
	

	public void onTimeLineEvent(ModelEvent e) {
		if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_ENTER) {
			System.out.println("Station:: GOT TIME EVENT ENTER");
			if(e.getTargetFsNode().getName().equals("question")){
				pauseVideo();
				System.out.println("GOT QUESTION EVENT!");
//				if(model.getProperty("/screen/isClockManager").equals("true")){
					System.out.println("SCREEN MANAGER HERE!!!");
					screen.get("#exhibition").append("div","questionscreen", new QuestionScreenController());
					String stationid = model.getProperty("@stationid");
					String exhibitionid = model.getProperty("@exhibitionid");
					MasterClockManager.setApp(this);
					MasterClockThread c = MasterClockManager.getMasterClock("/exhibition/"+exhibitionid+"/station/"+stationid);
					if (c != null)
						c.freezeFor(Long.parseLong(e.getTargetFsNode().getProperty("duration")));
//					else
//						System.out.println("CLOCK IS NULL");
//				}
			}
		} else if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_LEAVE) {
			System.out.println("Station:: GOT TIME EVENT END");
			if(e.getTargetFsNode().getName().equals("question")){
				screen.get("#questionscreen").remove();
				playVideo();

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
	
	public void onPlayEvent(ModelEvent e){
		System.out.println("Play Event");
		playVideo();
		
	}
	
	public void onPauseEvent(ModelEvent e){
		System.out.println("Pause Event");
		pauseVideo();
		
	}
	
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
	
	public void playVideo(){
		JSONObject nd = new JSONObject();
		nd.put("action","play");
		nd.put("target", "interactivevideo_video_c");
		screen.get(selector).update(nd);
	}

	public void pauseVideo(){
		JSONObject nd = new JSONObject();
		nd.put("action","pause");
		nd.put("target", "interactivevideo_video_c");
		screen.get(selector).update(nd);
	}
	
	public void seekVideo(long milliseconds){
		JSONObject nd = new JSONObject();
		nd.put("action","seek");
		nd.put("target", "interactivevideo_video_c");
		nd.put("seekingvalue", milliseconds+"");
		screen.get(selector).update(nd);
	}
	
	public void onClockUpdate(ModelEvent e) {
		JSONObject nd = new JSONObject();
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		nd.put("action","wantedtime");
		nd.put("target", "interactivevideo_video_c");
		nd.put("wantedtime", model.getProperty("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime"));
		screen.get(selector).update(nd);
	}
	
	public void destroyed() {
		super.destroyed();
		if (model.getProperty("/screen/isClockManager").equals("false")) return;
		System.out.println("Clock Master exiting..");
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");	
		model.setProperty("/app/interactivevideo/exhibition/" +exhibitionid+ "/station/"+ stationid +"/vars/hasClockManager", "false");
			
	}
}
