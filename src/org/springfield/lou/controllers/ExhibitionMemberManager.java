package org.springfield.lou.controllers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.screen.Screen;

public class ExhibitionMemberManager {
	
    public static FsNode getMember(Screen s) {
    	checkAvailableNameslist(s);
    	FsNode member = s.getModel().getNode("/shared/exhibition/member/"+s.getModel().getProperty("@exhibitionid")+"/"+s.getBrowserId());
    	String name = member.getProperty("name");
    	if (name==null || name.equals("")) {
    		return null;
    	}
    	return member;
    }
    
    public static void freeAllNames(Screen s) {
       	List<FsNode> nodes = s.getModel().getList("/shared/exhibition/availablenames/"+s.getModel().getProperty("@exhibitionid")+"/").getNodes();
    		if (nodes != null) {
    			for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
    				FsNode namenode = iter.next();
    				namenode.setProperty("used","false");
    			}
    		}
    }
    
    public static String getNextFreeName(Screen s) {
    	List<FsNode> nodes = s.getModel().getList("/shared/exhibition/availablenames/"+s.getModel().getProperty("@exhibitionid")+"/").getNodes();
		if (nodes != null) {
			for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
				FsNode namenode = iter.next();
				System.out.println("NODE="+namenode);
				String used = namenode.getProperty("used");
				if (used==null || !used.equals("true")) {
					namenode.setProperty("used","true");
					return namenode.getId();
				}
			}
		}
		return null;	
    }
    
    public static void freeName(Screen s,String username) {
    	FsNode member = s.getModel().getNode("/shared/exhibition/availablenames/"+s.getModel().getProperty("@exhibitionid")+"/name/"+username);
    	member.setProperty("used", "false");
    }
    
    public static FsNode claimMember(Screen s,String username) { 
    	FsNode member = s.getModel().getNode("/shared/exhibition/members/"+s.getModel().getProperty("@exhibitionid")+"/"+s.getBrowserId());
    	String name = member.getProperty("name");
    	if (name==null || name.equals("")) {
    		member.setProperty("name",username);
    		return member;
    	} else {
    		return null;
    	}
    }
    
	public static void checkAvailableNameslist(Screen s) {
		int size=0;
		FSList l = s.getModel().getList("/shared/exhibition/availablenames/"+s.getModel().getProperty("@exhibitionid")+"/");
		if (l!=null) size=l.size();
		System.out.println("AVAIL NAMES LIST SIZE="+size);
		if (size==0) {
			String names = "Bert,Ernie,Elmo,Grover,Pino,Mumford,Koekiemonster,Troel,Purk,Tommie";
			String[] list = names.split(",");
			for (int i=0;i<list.length;i++) {
				FsNode namenode = new FsNode("name",list[i]);
				s.getModel().putNode("/shared/exhibition/availablenames/"+s.getModel().getProperty("@exhibitionid"),namenode);
			}
		}
		
		
	}
	
    public static int getMemberCount(Screen s) {
    	FSList members = s.getModel().getList("/shared/exhibition/member/"+s.getModel().getProperty("@exhibitionid"));
    	return members.size();
    }
    
}
