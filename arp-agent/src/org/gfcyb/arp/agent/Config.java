package org.gfcyb.arp.agent;

import java.net.*;
import java.io.*;
import java.util.*;

// http://www.mkyong.com/java/java-properties-file-examples/

public class Config {
	public static String agentId;
	public static String iface;
	public static List<String> macAddrs = null;
	public static List<String> ipAddrs = null;
	public final Properties props;
	private InputStream input = null;
	private OutputStream output = null;
	private final StringXOR xor;
	
	Config(String cfgPath) { // must have read + write access to config-file
		initNetCfg();
		props = new Properties();
		xor = new StringXOR();
		try {
			input = new FileInputStream(cfgPath);
			props.load(input);
			iface = props.getProperty("interface");
			String os = ""+System.getProperty("os.name").toLowerCase();
			// agentid MUST be hostname ! side-effects of fixing agentid: resetting a few things, and
			// encrypting localpwd (initially assumed correct, cleartext)
			agentId = ""+InetAddress.getLocalHost().getHostName().toLowerCase();
			if( ! agentId.equals(props.getProperty("agentid").toLowerCase()) ) {
				System.out.println("Fixing config-file ...");
				props.setProperty("agentid", agentId);
				props.setProperty("os", os);
				props.setProperty("interface", "eno0"); // new name for "eth0"
				props.setProperty("keepalive", ""+30 ); // default to 30 sec
				props.setProperty("log", "arp-agent.log");
				// #################
				props.setProperty( "localpwd", scramble(props.getProperty("localpwd")) );
				//props.setProperty( "remotepwd", ""+agentId.length() );
				// #################
				output = new FileOutputStream(cfgPath);
				props.store(output, " fixed config: (Hxk\\=)" ); // save properties
			}
			/*
			for (Object o : props.keySet() ) {
				String k= (String)o;		//explicit cast is dangerous
				System.out.println("("+k+" = "+props.getProperty(k)+")");
			}
			*/
		}
		catch (FileNotFoundException ex) { ex.printStackTrace(); }
		catch (IOException ex) { ex.printStackTrace(); }
		finally {
			if (input!=null) {try{input.close();} catch(IOException e){e.printStackTrace();}}
			if(output!=null) {try{output.close();} catch(IOException e){e.printStackTrace();}
			}
		}
		props.setProperty("localpwd", unscramble(props.getProperty("localpwd")) );
	}
	
	private String scramble(String s) { return xor.encode(s, ""+42); }
	
	private String unscramble(String s) { return xor.decode(s, ""+42); }
	
	private void initNetCfg() {
		macAddrs = new ArrayList<String>();
		ipAddrs = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> allNifs= NetworkInterface.getNetworkInterfaces();
			while( allNifs.hasMoreElements() ) {
			    NetworkInterface nif = allNifs.nextElement();
			    byte[] mac = nif.getHardwareAddress();
			    if (mac!=null) { // mac is null for loopback i/f, OR for security reasons
			    	System.out.print( "i/f: "+nif.getDisplayName()+"; " );
			    	String macc = SystemHelper.parseMAC(mac);
			    	System.out.print( "MAC: "+macc+"; " );
			    	macAddrs.add( macc );
			    	Enumeration<InetAddress> allIps = nif.getInetAddresses();
				    while (allIps.hasMoreElements()) { // every real NIC can have many IP-addrs
				    	InetAddress ip = allIps.nextElement();
				    	if( Inet4Address.class == ip.getClass() ) { // we only accept IPv4
				    		System.out.print( "IP: "+ ip.getHostAddress()+"; " );
				    		ipAddrs.add(ip.getHostAddress());
				    	}
				    }
				    System.out.println("");
				}
			}
		} catch (SocketException e1) {e1.printStackTrace();}
	}
	
}