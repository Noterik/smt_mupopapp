package org.springfield.lou.application.types.util;

import java.util.HashMap;

import org.springfield.lou.application.Html5Application;
import org.springfield.lou.screen.Screen;
import org.springfield.lou.controllers.Html5Controller;



public class MasterClockManager {
	
	public static MasterClockManager instance = new MasterClockManager();
	
	private static Html5Controller app;
	private static HashMap<String,MasterClockThread> clocks = new HashMap<String,MasterClockThread>();
	
	public static void setApp(Html5Controller a) {
		app = a;
	}
	
	public static MasterClockThread addMasterClock(String name) {
		MasterClockThread clock  = clocks.get(name);
		if (clock!=null) {
			System.out.println("RESET THE THREAD !!!"+name);
			clock.reset();
		} else {
			System.out.println("CREATING NEW THREAD !!!"+name);
			clock = new MasterClockThread(app,name);
			clocks.put(name, clock);
		}
		return clock;
	}

	public static MasterClockThread getMasterClock(String name) {
		MasterClockThread clock  = clocks.get(name);
		if (clock!=null) {
			return clock;
		}
		return null;
	}
	
	public static void removeClock(String name){
		clocks.remove(name);
		
	}
}
