package org.springfield.lou.controllers.apps.interactivevideo;

import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.controllers.apps.interactivevideo.clocksync.MasterClockManager;
import org.springfield.lou.controllers.apps.interactivevideo.clocksync.MasterClockThread;
import org.springfield.lou.model.ModelBindEvent;
import org.springfield.lou.model.ModelEvent;

public class CommunicationController extends Html5Controller {
	
	MasterClockThread t;
	String exhibitionid;
	String stationid;
	
	public CommunicationController(MasterClockThread t){
		this.t = t;
		String[] parts = t.getName().split("/");
		exhibitionid = parts[6];
		stationid = parts[8];
		
		System.out.println("COMMUNICATIONCONTROLLER - EXHIBITION::: " + exhibitionid);
		System.out.println("COMMUNICATIONCONTROLLER - STATION::: " + stationid);
	}
	
	public void followTimeline(String path, String timerPath, String s, String d){
		model.onTimeLineNotify(path,timerPath,s,d, "onTimelineEvent",this);
		
	}
	
	
	public void listenForEvent(String path){
		System.out.println("COMMUNICATION CONTROLLER subscribing to event:::: " + path);
		model.onNotify(path, "onEvent", this);
	}
	
	public void event(String event){
		if(event.equals("pause"))
			model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "false");
		else if(event.equals("play")){
			model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "true");

		}
	}
	
	public void onEvent(ModelEvent e){
		String eventName = e.path.substring(e.path.lastIndexOf("/", e.path.length()));
		System.out.println("COMMUNICATION CONTROLLER:::: " +eventName);
		
		if (eventName.equals("userJoined")){
			System.out.println("User Joined");
			String stationid = model.getProperty("@stationid");
			String exhibitionid = model.getProperty("@exhibitionid");
			if (t.running()) return;
			
			System.out.println("Sending start signal to thread");
			t.start();
		}
	}
	
	public void onTimeLineEvent(ModelEvent e) {
		if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_ENTER) {
			System.out.println("COMMUNICATIONCONTROLLER:: GOT TIME EVENT ENTER");
			if(e.getTargetFsNode().getName().equals("question")){
				t.freezeFor(Long.parseLong(e.getTargetFsNode().getProperty("duration")));
			}
		} else if (e.eventtype==ModelBindEvent.TIMELINENOTIFY_LEAVE) {
			System.out.println("COMMUNICATIONCONTROLLER:: GOT TIME EVENT END");
		}
	}

}
