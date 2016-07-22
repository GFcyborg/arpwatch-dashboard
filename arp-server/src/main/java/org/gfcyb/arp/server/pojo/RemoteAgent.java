package org.gfcyb.arp.server.pojo;

import java.util.*;

public class RemoteAgent {
	//final
	String agentId;
	String mac;
	String ip;
	Status status;
	String lastActivity;
	List<Event> events;
	
	public String getAgentId() {return agentId;}
	public String getMac() {return mac;}
	public String getIp() {return ip;}
	public String getLastActivity() {return lastActivity;}
	public List<Event> getEvents() {return events;}
	public void setAgentId(String agentId) {this.agentId = agentId;}
	public void setLastActivity(String lastDate) {this.lastActivity = lastDate;}
	public void setEvents(List<Event> events) {this.events = events;}
	public void setMac(String mac) {this.mac = mac;}
	public void setIp(String ip) {this.ip = ip;}

	public RemoteAgent(String agentId, String mac, String ip) {
		this.agentId=""+agentId;
		this.mac=""+mac;
		this.ip=""+ip;
		events = new LinkedList<Event>();
	}

	@Override
	public boolean equals(Object o) {
		return ( o instanceof RemoteAgent && this.agentId.equals(((RemoteAgent)o).agentId) );
	}

	public void add(Event evt) { events.add(evt); }

	public int size() { return events.size(); }
}

enum Status {
	ALIVE,
	MISSING,
	DEAD
};