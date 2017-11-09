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
package org.springfield.lou.controllers.apps.photozoom;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.application.types.util.Mask;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEvent;

public class PhotoZoomController extends Html5Controller {
	private Map<String, HashMap<String, Double>> spots = new HashMap<String, HashMap<String, Double>>();
	private Map<String, Mask> masks = new HashMap<String, Mask>();
	private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	List<FsNode> nodes;
	private Map<String, FsNode> selecteditems = new HashMap<String, FsNode>();
	
	private Map<String, String> playingItems = new HashMap<String, String>();

	int timeoutcount = 0;
	int timeoutnoactioncount = 0;
	int maxtimeoutcount = 30 * 60; // (check every 1sec)
	int maxtnoactiontimeoutcount = 60; // (check every 1sec)
	String userincontrol;
	FsNode itemnode = null;

	public PhotoZoomController() {
	}

	public void attach(String sel) {
		selector = sel;

		model.setProperty("@contentrole", "mainapp");
		String selecteditem = model.getProperty("@selecteditem");
		itemnode = null;
		if (selecteditem != null) {
			model.setProperty("@itemid", selecteditem);
			itemnode = model.getNode("@item");
		} else {
			FSList items = model.getList("@items");
			if (items.size() > 0) {
				itemnode = items.getNodes().get(0);
				System.out.println("ITEMSET2" + itemnode.getId());
				model.setProperty("@itemid", itemnode.getId());
			}

		}

		FsNode exhibitionnode = model.getNode("@exhibition");
		FsNode imagenode = model.getNode("@image");

		if (itemnode != null) {
			nodes = model.getList("@item/mask").getNodes();
			loadMasks();

			JSONObject data = FSList.ArrayToJSONObject(nodes,
					screen.getLanguageCode(), "maskurl,audiourl");

			data.put("domain", LazyHomer.getExternalIpNumber());
			data.put("jumper", exhibitionnode.getProperty("jumper"));

			model.setProperty("@contentrole", "mainapp");
			System.out.println("ITEMSET3" + selecteditem);
			model.setProperty("@itemid", selecteditem);

			data.put("url", model.getProperty("@item/url"));

			if (model.getProperty("@station/codeselect") != null) {
				data.put("code", model.getProperty("@station/codeselect"));
			}

			screen.get(selector).render(data);
			screen.get(selector).loadScript(this);

			JSONObject d = new JSONObject();
			d.put("command", "init");
			screen.get(selector).update(d);

			String scale = model.getProperty("@item/scale");
			if (scale != null && !scale.equals("")) {
				screen.get("#image-scale-wrapper").css("transform",
						"scale(" + scale + ")");
			}
			String origin = model.getProperty("@item/origin");
			if (scale != null && !origin.equals("")) {
				screen.get("#image-scale-wrapper").css("transform-origin",
						origin);
			}
		}

		model.onPropertiesUpdate("@photozoom/spot/move",
				"onPositionChange", this);
		model.onPropertiesUpdate("@photozoom/state", "onStateChange", this);
		model.onNotify("@photozoom/spotting/player", "onAudioLoaded", this);
		model.onNotify("/shared[timers]/1second", "onTimeoutChecks", this);
	}

	public void loadImages() {
		for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
			FsNode node = (FsNode) iter.next();
			String mask = node.getProperty("mask");
			try {
				URL url = new URL(mask);
				BufferedImage nimg = ImageIO.read(url);
				images.put(node.getId(), nimg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void loadMasks() {
		for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
			FsNode node = (FsNode) iter.next();
			String mask = node.getProperty("maskurl");
			
			try {
				URL url = new URL(mask);
				BufferedImage nimg = ImageIO.read(url);
				
				Mask imageMask = new Mask(nimg.getWidth(), nimg.getHeight());
				
				for (int x = 0; x < nimg.getWidth(); x++) {
		            for (int y = 0; y < nimg.getHeight(); y++) {
		                final int clr = nimg.getRGB(x, y);
		                int a = (clr >> 24) & 0xff;
		                int g = (clr >> 8) & 0xff;
		                
		                // Color Red get cordinates
		                if (a > 200 && g > 100) {
							imageMask.addPoint(x, y);
							break;
						}
		            }
		        }
				masks.put(node.getId(), imageMask);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void checkOverlays() {
		
		// new check, so clear old cache
		selecteditems.clear();

		// loop over every layer
		for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
			FsNode node = (FsNode) iter.next();
			Mask mask = masks.get(node.getId());
			
			if (mask != null) {
				try {
					int width = mask.getWidth();
					int height = mask.getHeight();

					boolean selected = false;
					Iterator it = spots.entrySet().iterator();
					// loop over every connected client to allow multi
					// highlighting
					while (it.hasNext()) {
						Map.Entry<String, HashMap<String, Double>> pair = (Map.Entry<String, HashMap<String, Double>>) it
								.next();
						double xp = pair.getValue().get("x");
						double yp = pair.getValue().get("y");

						// make sure to divide by a double, otherwise you will
						// get a
						// int value when dividing two ints
						int x = (int)((width / 100.0) * xp);
						int y = (int)((height / 100.0) * yp);

						if (mask.checkHit(x, y)) {
							selecteditems.put(pair.getKey(), node);
							selected = true;
							break;
						}
					}
					
					if (selected) {
						String layerId = "#zoomandaudio_layer" + node.getId(); 
						screen.get(layerId).css(
								"opacity", "0.3");
					} else {
						screen.get("#zoomandaudio_layer" + node.getId()).css(
								"opacity", "0");
					}

				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public void onStateChange(ModelEvent e) {
		System.out.println("JOINED FOUND");
		FsPropertySet set = (FsPropertySet) e.target;
		String action = set.getProperty("action");
		System.out.println("COMMAND=" + action);
	}

	public void onPositionChange(ModelEvent e) {
		timeoutnoactioncount = 0;
		FsPropertySet set = (FsPropertySet) e.target;

		try {
			double x = Double.parseDouble(set.getProperty("x"));
			double y = Double.parseDouble(set.getProperty("y"));
			String color = set.getProperty("color");

			String action = set.getProperty("action");
			String deviceid = set.getProperty("deviceid");
			String language = set.getProperty("language");

			long currentTime = new Date().getTime();

			// update last seen
			HashMap<String, Double> spot = new HashMap<String, Double>();
			spot.put("lastseen", (double) new Date().getTime());
			spot.put("x", x);
			spot.put("y", y);
			spots.put(deviceid, spot);

			// TODO: better to to this timeout based instead upon a move
			Iterator it = spots.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, HashMap<String, Double>> pair = (Map.Entry<String, HashMap<String, Double>>) it
						.next();
				if (pair.getValue().get("lastseen") + 60000 < currentTime) {
					it.remove();
					selecteditems.remove(deviceid);
				}
			}

			// check overlays after updating connected client list
			checkOverlays();

			if (action.equals("move")) { // its a move event so lets just move
				JSONObject d = new JSONObject();
				d.put("command", "spot_move");
				d.put("spotid", "#zoomandaudio_spot_" + deviceid);
				d.put("x", x);
				d.put("y", y);
				screen.get(selector).update(d);
				
				if(selecteditems.get(deviceid) != null){
					FsNode node = selecteditems.get(deviceid);
					String id = node.getId();
					
					if(!playingItems.containsKey(screen.getId()) || !playingItems.get(screen.getId()).equals(id)){
						playingItems.put(screen.getId(), id);
						
						FsNode message = new FsNode("message", "1");
						message.setProperty("action", "startaudio");
						
						System.out.println("SENDING AUDIO="
								+ language
								+ " "
								+ selecteditems.get(deviceid).getSmartProperty(
										language, "audiourl"));
						
						message.setProperty("url", selecteditems.get(deviceid)
								.getSmartProperty(language, "audiourl"));

						String transcript = selecteditems.get(deviceid)
								.getSmartProperty(language, "transcript") == null ? ""
								: selecteditems.get(deviceid).getSmartProperty(
										language, "transcript");
						
						message.setProperty("text", transcript);

						message.setProperty("deviceid", deviceid);
						model.notify("@photozoom/spot/audio", message);

						String[] animation = new String[] {
								"border-top: 6px solid grey",
								"-webkit-animation: rotation .6s infinite linear",
								"-moz-animation: rotation .6s infinite linear",
								"-o-animation: rotation .6s infinite linear",
								"animation: rotation .6s infinite linear" };
						screen.get("#zoomandaudio_spot_outer_" + deviceid).css(
								animation);
					}
				}
			} 
		} catch (Exception error) {
			System.out
					.println("PhotoInfoSpots - count not move stop of play sound");
			System.out.println(error.getMessage());
			System.out.println(error.getStackTrace());
		}
	}

	private void sendVoiceOverAudio() {
		FsNode message = new FsNode("message", "1");
		message.setProperty("action", "startaudiovoiceover");

		String itempath = itemnode.getPath();
		itempath = itempath.substring(0, itempath.length() - 11);
		// System.out.println("SENDING VOICEOVER AUDIO="+itempath);
		message.setProperty("exhibitionpath", itempath);
		message.setProperty("deviceid", "all");
		model.notify("@photozoom/spot/audio", message);
	}

	public void onAudioLoaded(ModelEvent e) {
		timeoutnoactioncount = 0;
		FsNode target = e.getTargetFsNode();

		String deviceid = target.getProperty("deviceid");
		String[] animation = new String[] { "border-top: 6px solid white",
				"-webkit-animation: none !important",
				"-moz-animation: none !important",
				"-o-animation: none !important", "animation: none !important" };

		screen.get("#zoomandaudio_spot_outer_" + deviceid).css(animation);
	}

	public void onTimeoutChecks(ModelEvent e) {
		sendVoiceOverAudio(); // update signal, used for things like voice over
								// check

		if (timeoutcount != -1) {
			timeoutcount++;
			timeoutnoactioncount++;
		}

		if (timeoutcount > maxtimeoutcount
				|| timeoutnoactioncount > maxtnoactiontimeoutcount) {
			System.out.println("Photoinfospots timeout");

			model.setProperty("@fromid", userincontrol);

			timeoutcount = -1; // how do the remove not remove the notify ?
			timeoutnoactioncount = -1; // how do the remove not remove the
										// notify ?

			String waitscreen = model.getProperty("@station/waitscreen");
			String contentselect = model.getProperty("@station/contentselect");

			// if we have a contentselect screen configured get back to there
			if (contentselect != null && !contentselect.equals("none")) {
				System.out.println("Resetting back to contentselect");

				screen.get(selector).remove();
				model.setProperty("/screen/state", "contentselectforce");

				// Inform clients to switch
				FsNode message = new FsNode("message", screen.getId());
				message.setProperty("request", "contentselect");
				model.notify("@stationevents/fromclient", message);
			}
			// if we don't have a contentselect check if we have a waitscreen
			// configured
			else if (waitscreen != null && !waitscreen.equals("")
					&& !waitscreen.equals("none")) {
				System.out.println("Resetting back to waitscreen");

				screen.get(selector).remove();
				model.setProperty("/screen/state", "init");

				// Inform clients to switch
				FsNode message = new FsNode("message", screen.getId());
				message.setProperty("request", "init");
				model.notify("@stationevents/fromclient", message);
			}

			// else we don't need to do anything (not even a refresh!)
			// just keep this app forever

		}
	}
}