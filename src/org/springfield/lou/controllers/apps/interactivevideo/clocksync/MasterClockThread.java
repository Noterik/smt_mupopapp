package org.springfield.lou.controllers.apps.interactivevideo.clocksync;

import java.util.Date;

import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.screen.Screen;

public class MasterClockThread extends Html5Controller {
	private boolean running = false;
	private long streamtime = 0;
	private String name;
	private Html5Controller app;
	private boolean paused = true;
	private boolean frozen = false;
	private long freezetime = 0;
	long virtual_streamtime = 0;
	long videoLength = 10000;
	String stationid;
	String exhibitionid;
	int updateInterval = 1000;
	int timeToCatchUp = 5000;
	
    public MasterClockThread(Html5Controller a,String n) {
		//super("masterclockthread "+n);
		app = a;
		name = n;
		stationid = app.model.getProperty("@stationid");
		exhibitionid = app.model.getProperty("@exhibitionid");
	}
    
	public String getScreenId() {
		return "/masterclock/"+this.hashCode();
	}
	
	public int getApplicationHashCode() {
		return this.hashCode();
	}
    
	public void on1SecondTimer(ModelEvent e) {
		doWork();
	}
    
	public void doWork() {
				try {	
					long realtime = new Date().getTime()+5000; // 5 seconds from now I want you to be at xxxxx
					if (streamtime > videoLength){
						System.out.println("Reseting Video Clock.");
						reset();
						app.model.setProperty("/app/interactivevideo/exhibition/" +exhibitionid+ "/station/"+ stationid +"/vars/hasClockManager", "false");
						app.model.notify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/exhibitionEnded");
						return;
					}
					
					app.model.setProperty("/shared/mupop/exhibition/"+exhibitionid+"/station/"+ stationid+"/currenttime", ""+virtual_streamtime);
					
					
					//video playback has to catch up after exiting a freeze event
					if(virtual_streamtime == streamtime || frozen)
						virtual_streamtime += updateInterval;
					if(!frozen){
						app.model.setProperty("/shared/exhibition/"+exhibitionid+"/station/"+stationid+"/vars/wantedtime", realtime+","+(streamtime+ timeToCatchUp));
						app.model.setProperty("/shared/exhibition/"+exhibitionid+"/station/"+stationid+"/vars/curPlayTime", ""+streamtime);
						System.out.println("wantedtime : "+ realtime+","+streamtime);
						app.model.notify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/wantedtime", new FsNode("wantedtime", realtime+","+streamtime+ timeToCatchUp));
						streamtime += updateInterval;
					}
					else{
						freezetime -= updateInterval;
						if (freezetime <= 0){
							frozen = false;
//							app.model.notify("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/play");
						}
					}
				} catch(Exception e) {
						System.out.println("ERROR MasterClockThread "+name);
						e.printStackTrace();
					}
	}
	
	public void seek(long newtime) {
		streamtime = newtime;
		//this.interrupt(); // interupt to take new times
	}
	
	public long getStreamTime() {
		return streamtime;
	}
	
	public void freezeFor(long duration){
		//we pause the timer for the actual video, but the timer 
		//for the overall presentation continues. This allows for the video to pause
		//but time events still happen
		freezetime = duration;
		frozen = true;
		
	}
	
	public void pause() {
		//String stationid = app.model.getProperty("@stationid");
		//String exhibitionid = app.model.getProperty("@exhibitionid");
		app.model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "false");
		paused=true;
		//this.interrupt(); // interupt to take new times
	}
	
	public void reset() {
		// new master
		pause();
		app.model.setProperty("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/pause", "false");
		streamtime = 0;
		virtual_streamtime = 0;
	}
	
	public void resumeClock(){
		//String stationid = app.model.getProperty("@stationid");
		//String exhibitionid = app.model.getProperty("@exhibitionid");
		app.model.setProperty("/shared/app/interactivevideo/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/isplaying", "true");
		paused = false;
		
	}
	
	public void setVideoLength(long length){
		videoLength = length;
	}
	
	public String getClockName() {
		return name;
	}
	
	/*
	public boolean running() {
		return !paused;
	}
	*/
	
	public void start() {
		System.out.println("master clock start called");
		app.model.onNotify("/shared[timers]/1second","on1SecondTimer",this);	
	}
	
	public void restart() {
		// new master
		paused = false;
		System.out.println("RESTART CALLED");
	//	app.model.setProperty("/shared/exhibition/"+exhibitionid+"/station/"+ stationid +"/vars/pause", "false");
		streamtime = 0;
		virtual_streamtime = 0;
		freezetime = 0;
	}
	
	
    
    /**
     * Shutdown
     */
	public void destroy() {
		//running = false;
		//cannot resume same thread after interrupt
		//this.interrupt(); // signal we should stop;
	}
}