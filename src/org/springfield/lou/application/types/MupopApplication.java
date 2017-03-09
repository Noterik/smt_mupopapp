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
package org.springfield.lou.application.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.*;
import org.springfield.lou.controllers.*;
import org.springfield.lou.controllers.apps.pairing.PairingController;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.*;
import org.springfield.lou.servlet.LouServlet;

public class MupopApplication extends Html5Application {

	public MupopApplication(String id) {
		super(id);
		this.setSessionRecovery(true);
		this.setSessionRecovery(true);
		this.addToRecoveryList("vars/pairingid");
	}

	public void onNewScreen(Screen s) {
		s.setLanguageCode("en");
		s.get("#screen").attach(new ScreenController());


		loadStyleSheet(s, "bootstrap.min");
		loadStyleSheet(s, "bootstrap-theme");
		loadStyleSheet(s, "font-awesome.min");

		// start the pairing controller its job is to 
		s.get("#screen").append("div", "pairing",new PairingController());
	}
	
	public void maintainanceRun() {
		super.maintainanceRun();
		Iterator<Screen> iter = getScreenManager().getScreens().values().iterator();
		if (iter.hasNext()) {
			Screen scr = iter.next();
			scr.getModel().notify("/app['timers']","10");
		}
	
	}
	
}
