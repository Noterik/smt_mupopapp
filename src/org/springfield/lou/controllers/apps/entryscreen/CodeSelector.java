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
package org.springfield.lou.controllers.apps.entryscreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springfield.lou.model.Model;

public class CodeSelector {
	
	private static Random rnd;
	private static Map<String, String> acodes;
	private static Map<String, String> ncodes;

	public static String getFreeRandomCode() {
		if (acodes==null) fillCodes();
		rnd = new Random();
		String acode  = acodes.get("P"+rnd.nextInt(11));	
		String ncode  = ncodes.get("P"+rnd.nextInt(9));	
		String fullcode = acode+ncode;
		return fullcode;
	}
	
	private static void fillCodes() {
		 acodes = new HashMap<String, String>();
		 ncodes = new HashMap<String, String>();
		 // ugly but flexible
		 acodes.put("P0","A");
		 acodes.put("P1","B");
		 acodes.put("P2","C");
		 acodes.put("P3","D");
		 acodes.put("P4","E");
		 acodes.put("P5","F");
		 acodes.put("P6","G");
		 acodes.put("P7","H");
		 acodes.put("P8","I");
		 acodes.put("P9","J");
		 acodes.put("P10","K");
		 acodes.put("P11","L");
		 ncodes.put("P0","1");
		 ncodes.put("P1","2");
		 ncodes.put("P2","3");
		 ncodes.put("P3","4");
		 ncodes.put("P4","5");
		 ncodes.put("P5","6");
		 ncodes.put("P6","7");
		 ncodes.put("P7","8");
		 ncodes.put("P8","9");
		 ncodes.put("P9","0");
		 
		 
		 
	}
}
