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
