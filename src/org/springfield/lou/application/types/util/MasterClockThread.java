package org.springfield.lou.application.types.util;

import java.util.Date;

import org.springfield.fs.Fs;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.screen.Screen;

public class MasterClockThread extends Thread {
	private boolean running = false;
	private long streamtime = 0;
	private String name;
//	private Screen master;
	private boolean paused = false;
	
    public MasterClockThread(String n) {
		super("masterclockthread "+n);
	//	master = s;
		//app = a;
		name = n;
	//	running = true;
	//	start();
	}
    
	public void run() {
		while (running) {
			try {	
				long realtime = new Date().getTime()+5000; // 5 seconds from now I want you to be at xxxxx
				if (!paused) streamtime += 5000;
				if (streamtime>(40*1000)) streamtime = 5000; // loop our test song ! (should be less because loop time)
			//	app.setProperty("/masterclock/"+name+"/wantedtime", ""+realtime+","+streamtime);
				sleep(5000);
				} catch(InterruptedException i) {
					if (!running) break;
				} catch(Exception e) {
					System.out.println("ERROR MasterClockThread "+name);
					e.printStackTrace();
				}
		}
	}
	
	public void seek(long newtime) {
		streamtime = newtime;
		this.interrupt(); // interupt to take new times
	}
	
	public void pause() {
		paused=true;
		this.interrupt(); // interupt to take new times
	}
	

	public void reset() {
//		master = s;
	}
	
	public String getClockName() {
		return name;
	}
	
	public boolean running() {
		return running;
	}
	
	public void start(long time) {
		running = true;
		start();
	}
    
    /**
     * Shutdown
     */
	public void destroy() {
		running = false;
		this.interrupt(); // signal we should stop;
	}
}