package org.springfield.lou.application.types.util;

import java.util.HashMap;

import org.springfield.lou.application.Html5Application;
import org.springfield.lou.screen.Screen;



public class MasterClockManager {
	
	//private static SviaApplication app; // temp needed until moved to global memory in mojo
	
	private static HashMap<String,MasterClockThread> clocks = new HashMap<String,MasterClockThread>();
	
	//public static void setApp(SviaApplication a) {
	//	app = a;
	//}
	
	public static MasterClockThread addMasterClock(String name) {
		MasterClockThread clock  = clocks.get(name);
		if (clock!=null) {
			clock.reset();
		} else {
			clock = new MasterClockThread(name);
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

}
