package org.gfcyb.arp.agent.pojo;

import java.text.SimpleDateFormat;
import java.util.*;

public class Event {
	// TODO: check if final is good here
	private int msgId;
	private String agentId = org.gfcyb.arp.agent.Config.agentId;
	private String mac = org.gfcyb.arp.agent.Config.macAddrs.get(0);
	private String ip = org.gfcyb.arp.agent.Config.ipAddrs.get(0);
	private String timestamp;
	private String origMsg;
	private Level lvl;
	private Type type;
	
	public Event() { /*
		agentId = org.gfcyb.arp.agent.ArpAgent.agentId;
		timestamp = dateToString( new Date(1970,1,1) );
		origMsg = "";
		lvl = Level.DEBUG;
		type = Type.OTHER; */
	}
	
	public Event(Date t, String msg) {
		this.msgId = UUID.randomUUID().hashCode();
		this.timestamp = dateToString(t);
		this.origMsg = msg;
		// ---- set type (based on msg), and level (based on type):
		if ( 			msg.toLowerCase().contains("Keepalive from: ".toLowerCase())
		) {					this.type=Type.KEEPALIVE;		this.lvl=Level._7_DEBUG;
		} else if(		msg.toLowerCase().contains("POISONED routing-table: ".toLowerCase())
		) {					this.type=Type.POISONED_DGW;	this.lvl=Level._0_EMERGENCY;
		} else if(		msg.toLowerCase().contains("arpwatch: bogon ".toLowerCase())
		) {					this.type=Type.BOGON;			this.lvl=Level._5_NOTIFY;
		} else if(		msg.toLowerCase().contains("Subject: new station ".toLowerCase())
		) {					this.type=Type.NEW_STATION;		this.lvl=Level._4_WARNING;
		} else if(		msg.toLowerCase().contains("Subject: changed ethernet address ".toLowerCase())
		) {					this.type=Type.CHANGED_MAC;		this.lvl=Level._2_CRITICAL;
		} else if(		msg.toLowerCase().contains("Subject: flip flop ".toLowerCase())
		) {					this.type=Type.FLIP_FLOP;		this.lvl=Level._2_CRITICAL;
		} else if(		msg.toLowerCase().contains("arpwatch: ethernet mismatch ".toLowerCase())
		) {					this.type=Type.MISMATCH;		this.lvl=Level._3_ERROR;
		} else if(		msg.toLowerCase().contains("arpwatch: new activity ".toLowerCase())
		) {					this.type=Type.NEW_ACTIVITY;	this.lvl=Level._4_WARNING;
		} else if(		msg.toLowerCase().contains("arpwatch: ethernet broadcast ".toLowerCase())
		) {					this.type=Type.BROADCAST_L2;	this.lvl=Level._3_ERROR;
		} else if(		msg.toLowerCase().contains("arpwatch: ip broadcast".toLowerCase())
		) {					this.type=Type.BROADCAST_L3;	this.lvl=Level._3_ERROR;
		} else if(		msg.toLowerCase().contains("arpwatch: ethernet broadcast".toLowerCase())		// same as BROADCAST_L2 !! TODO: find diffs
		) {					this.type=Type.BROADCAST_L2SRC;	this.lvl=Level._3_ERROR;						// TODO: unshadowing by BROADCAST_L2
		} else if(		msg.toLowerCase().contains("arpwatch: reused old ethernet address ".toLowerCase())
		) {					this.type=Type.REUSED_MAC;		this.lvl=Level._2_CRITICAL;
		} else if(		msg.toLowerCase().contains("arpwatch: suppressed DECnet flip flop ".toLowerCase())
		) {					this.type=Type.SUPPRESSED;		this.lvl=Level._6_INFO;
		} else {			this.type=Type.UNKNOWN;			this.lvl=Level._5_NOTIFY; }
	}
	
	public int getMsgId() {return msgId;}
	public String getAgentId() { return agentId; }
	public String getTimestamp() { return timestamp; }
	public String getOrigMsg() { return origMsg; }
	public String getMac() {return mac;}
	public String getIp() {return ip;}
	public Level getLvl() { return lvl; }
	public Type getType() { return type; }
	public void setMsgId(int msgId) {this.msgId = msgId;}
	public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
	public void setOrigMsg(String origMsg) { this.origMsg = origMsg; }
	public void setLvl(Level lvl) { this.lvl = lvl; }
	public void setType(Type type) { this.type = type; }
	public void setMac(String mac) {this.mac = mac;}
	public void setIp(String ip) {this.ip = ip;}
	
	@Override
	public String toString() { return ""+timestamp+" ; "+origMsg; }
	static String dateToString(Date d) { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d); }
}
