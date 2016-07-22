package org.gfcyb.arp.agent;

import java.util.Date;

public class ArpAgent {
	public static final String defaultCfg = "config.properties"; // TODO: find a place for this file
	public static String actualCfg;
		
	public static void main(String[] args) throws Exception {
		//System.out.println("Usage: java Main [custom-cfg-file]");
		//System.out.println("Current command-line args count: " + args.length );
		//for(String s : args) { System.out.println(s); }
		actualCfg = args.length>1 ? args[1] : defaultCfg;
		System.out.println("Using configuration: " + actualCfg + "; OS: " + System.getProperty("os.name").toUpperCase() + "; " + new Date() );
		new Engine(actualCfg);
	}
}