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

import org.json.simple.JSONObject;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.ModelEvent;

public class QuestionScreenController extends Html5Controller  {
	
	public QuestionScreenController() {
	}
	
	public void attach(String sel) {
		selector = sel; 		
		String stationid = model.getProperty("@stationid");
		String exhibitionid = model.getProperty("@exhibitionid");
		JSONObject data= new JSONObject(); 
 		data.put("question_image", "http://images3.noterik.com/mupop/questionscreen_image.png");
 		data.put("question_text", "");
 		screen.get(selector).render(data);
	}
	
}
