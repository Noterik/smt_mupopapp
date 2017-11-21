package org.springfield.lou.application.types.util;

import java.util.HashMap;
import java.util.Map;

public class Mask {
	private int width;
	private int height;
	
	private Map<Integer, Integer> pixels;
	
	public Mask(int width, int height){
		this.width = width;
		this.height = height;
		this.pixels = new HashMap<Integer, Integer>();
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void addPoint(int x, int y){
		this.pixels.put(x, y);
	}
 	
	public boolean checkHit(int x, int y){
		return this.pixels.containsKey(x) && this.pixels.containsValue(y);
	}
}
