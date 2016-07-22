package org.gfcyb.arp.agent;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

class SystemHelper {
	
	static String parseMAC(byte[] mac) {
		String s="";
		StringBuilder sb = new StringBuilder();
		if (mac != null) {
            for (int i=0; i<mac.length; i++) { sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "")); }
            s=sb.toString();
        }
	    return s;
	}
	
	static Hashtable<Inet4Address,String> getCurrentGwMacs(String os) {			// this may return an empty table (not null) in case of incomplete arp-table
		Hashtable<Inet4Address,String> result = new Hashtable<Inet4Address,String>();
		String[] routeCmd;
		int headerLinesToSkip = 0;
		switch (os) {
		case "linux":
			routeCmd = new String[] {"route","-n"}; // Destination, Gateway, Genmask, Flags, Metric, Ref, Use, Iface
			headerLinesToSkip = 2;
			break;
		case "windows":
			routeCmd = new String[] {"route", "print"};
			headerLinesToSkip = 1;						// TODO: check if correct
			break;
		default:
			routeCmd = new String[] {""};
			break;
		}
		ProcessBuilder pb = new ProcessBuilder( routeCmd ).redirectErrorStream(true);
		InputStream out = null;
		try {
			out = pb.start().getInputStream();
			BufferedReader stdout = new BufferedReader( new InputStreamReader(out) );
			for (int i=0; i<headerLinesToSkip; i++) {stdout.readLine();} // just consume useless headers
			String line = null;
			//System.out.println("Printing stdout of: "+routePrintCmd[0] );
			while ((line = stdout.readLine()) != null) {
				//System.out.println(line);
				SimpleEntry<Inet4Address,String> pair = selectGwDataFromRouteOutput(os,line);;
				if (	pair!=null 
						&& pair.getKey()!=null
						&& pair.getValue()!=null		// incomplete arp-table produces null mac
				) {	result.put(
						pair.getKey(),
						pair.getValue()
					);
				}
			}
		} catch (IOException e) {
		} finally { try{if(out!=null){out.close();}}catch(IOException e){} }
		return result;
	}
	
	private static SimpleEntry<Inet4Address,String> selectGwDataFromRouteOutput(String os, String line) { // returning null if line is not a gateway
		SimpleEntry<Inet4Address, String> result = null;
		String[] tkz = line.split("\\s+"); // any whitespace-char group produce a split
		switch (os) {
		case "linux":
			String dst = tkz[0];
			String gw = tkz[1];
			if( dst.equals("0.0.0.0") ) {
				Inet4Address ip = null;
				try { ip=(Inet4Address)InetAddress.getByName(gw); } catch (UnknownHostException e) {}
				String mac = getMacOfGw(os,gw);
				if(mac!=null){		// incomplete arp-table produces null mac
					result = new SimpleEntry<Inet4Address, String>(ip, mac);
				}
			}
			break;
		case "windows":
			break;
		default:
			break;
		}
		return result;
	}

	private static String getMacOfGw(String os, String gw) {		// incomplete arp-table produces null mac
		String result = null;
		String[] arpCmd;
		int headerLinesToSkip = 0;
		switch (os) {
		case "linux":
			arpCmd = new String[] {"arp","-n"}; // Indirizzo, TipoHW, IndirizzoHW, Flag, Maschera, Interfaccia
			headerLinesToSkip = 1;
			break;
		case "windows":
			arpCmd = new String[] {"arp", "-a"};
			headerLinesToSkip = 1;						// TODO: check if correct
			break;
		default:
			arpCmd = new String[] {""};
			break;
		}
		ProcessBuilder pb = new ProcessBuilder( arpCmd ).redirectErrorStream(true);
		InputStream out = null;
		try {
			out = pb.start().getInputStream();
			BufferedReader stdout = new BufferedReader( new InputStreamReader(out) );
			for (int i=0; i<headerLinesToSkip; i++) {stdout.readLine();} // just consume useless headers
			String line = null;
			//System.out.println("Printing stdout of: "+routePrintCmd[0] );
			while ((line = stdout.readLine()) != null) {
				//System.out.println(line);
				if (line.contains(gw)) {
					String[] tkz = line.split("\\s+"); // any whitespace-char group produce a split
					switch (os) {
					case "linux":
						result = ( tkz.length<4? null : tkz[2] ); // TODO: better handling of (incomplete) case (typically right after boot)
						break;
					case "windows":
						break;
					default:
						break;
					}
				}
			}
		} catch (IOException e) {
		} finally { try{if(out!=null){out.close();}}catch(IOException e){} }
		return result;
	}
	
	static boolean conflictsWith(									// conflicts only if a baseGw changed its MAC (newGws are not checked)
			Hashtable<Inet4Address,String> base ,					// an empty base (e.g.: incomplete arp-table) never detects arp-poisoning
			Hashtable<Inet4Address,String> neww
	) {
		boolean result = true;
		for( Inet4Address baseGw : base.keySet() ) {
			/*String gwMac = ( this.get(baseGw)==null ? "null" : this.get(baseGw) );
			System.out.println( "Base gw ("+baseGw.getHostAddress()+") was ("+baseMap.get(baseGw)+"); now is ("+ gwMac +")" );*/
			if(
				neww.get(baseGw) == null 							// this baseGw is not currently active (i.e: iface down)
				||													//  OR
				neww.get(baseGw).equals( base.get(baseGw) )		// this baseGw is still active (i.e: up) and is not changed 
			) {
				result = false;										// some baseGw is up and unchanged: OK (no conflict)
			}
		}
		return result;
	}

	static String[] getAdministrativeCmdline(String pwd, String cmd) {
		return new String[] {"/bin/bash","-c","echo "+pwd+"| sudo -S "+cmd}; // TODO: find a better way to root exec: it exposes root-pwd in (ps auxww)
	}

	public static String expand(Hashtable<Inet4Address,String> table) {
		String result = null;
		StringBuilder sb = new StringBuilder();
		if (table!=null) {
			for(Inet4Address ip : table.keySet()) {
				sb.append( "("+ip.getHostAddress()+")=MAC("+table.get(ip)+"); ");
			}
            result=sb.toString();
        }
		return result;
	}
}